package gov.cms.esmd.rc.api.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import gov.cms.esmd.bean.response.EsmdStatusResponse;
import gov.cms.esmd.utility.ChecksumUtil;
import gov.cms.esmd.utility.PropertiesUtils;
import gov.cms.esmd.utility.SecurityUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Client to upload WISeR PA Results JSON to esMD real-time endpoint.
 *
 * <p>Notes:
 * <ul>
 *   <li>Computes checksum & size from UTF-8 bytes</li>
 *   <li>Never logs tokens or full response bodies (only snippets)</li>
 *   <li>Validates required properties and inputs early</li>
 * </ul>
 */
public class UploadRealtimeApiClient {

    private static final Logger log = LoggerFactory.getLogger(UploadRealtimeApiClient.class);

    // HTTP success codes accepted by the API
    private static final int HTTP_OK = 200;
    private static final int HTTP_CREATED = 201;
    private static final int HTTP_ACCEPTED = 202;

    // Header names
    private static final String HDR_CONTENT_TYPE = "Content-Type";
    private static final String HDR_AUTHORIZATION = "Authorization";
    private static final String HDR_CONTENT_CHECKSUM = "contentchecksum";
    private static final String HDR_UID = "uid";
    private static final String HDR_SIZE = "size";
    private static final String HDR_LETTER_ID = "letterid";
    private static final String HDR_CONTENT_TYPE_CODE = "contenttypecode";
    private static final String HDR_SENDER_ROUTING_ID = "senderroutingid";

    // Property keys
    private static final String KEY_ENV_PREFIX = "api.environment.";
    private static final String KEY_UPLOAD_ENDPOINT = "api.url.pa-results-realtime";
    private static final String KEY_MAILBOX_ID = "userinfo.mailboxid";
    private static final String KEY_LOB_ID = "application.linesofbusinessid";

    // Optional HTTP timeout properties (ms)
    private static final String KEY_HTTP_CONNECT_TIMEOUT = "http.connect.timeout.ms";
    private static final String KEY_HTTP_SOCKET_TIMEOUT = "http.socket.timeout.ms";
    private static final String KEY_HTTP_CONN_REQ_TIMEOUT = "http.connection.request.timeout.ms";

    // Timeout defaults
    private static final int DEF_CONNECT_TIMEOUT_MS = 10_000;
    private static final int DEF_SOCKET_TIMEOUT_MS = 60_000;
    private static final int DEF_CONN_REQ_TIMEOUT_MS = 10_000;

    // Configuration
    private final Properties apiProperties;
    private final CloseableHttpClient httpClient;
    private final String environment;
    private final String uploadRealtimeURL;
    private final String senderRoutingID;
    private final String contentTypeCode;

    // JSON
    private final Gson gson = new Gson();

    /**
     * Construct using properties from {@link PropertiesUtils#loadProperties()} and a default HttpClient with timeouts.
     *
     * @param env environment key (e.g., "UAT", "PROD")
     */
    public UploadRealtimeApiClient(String env) {
        SecurityUtils.validateAlphanumeric(env, "Environment");
        this.environment = env;

        try {
            this.apiProperties = PropertiesUtils.loadProperties();

            int connectTimeout = getIntProp(KEY_HTTP_CONNECT_TIMEOUT, DEF_CONNECT_TIMEOUT_MS);
            int socketTimeout  = getIntProp(KEY_HTTP_SOCKET_TIMEOUT,  DEF_SOCKET_TIMEOUT_MS);
            int connReqTimeout = getIntProp(KEY_HTTP_CONN_REQ_TIMEOUT, DEF_CONN_REQ_TIMEOUT_MS);

            RequestConfig rc = RequestConfig.custom()
                    .setConnectTimeout(connectTimeout)
                    .setSocketTimeout(socketTimeout)                  // <- 4.x uses socket timeout
                    .setConnectionRequestTimeout(connReqTimeout)
                    .build();

            this.httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(rc)
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                    .build();

            this.uploadRealtimeURL = buildUploadRealtimeURL(environment);
            this.senderRoutingID = getRequiredProp(KEY_MAILBOX_ID, "Missing sender routing ID configuration");
            this.contentTypeCode = getRequiredProp(KEY_LOB_ID, "Missing linesofbusinessid property in configuration file");

            log.info("UploadRealtimeApiClient initialized for env={} endpoint={}", environment, uploadRealtimeURL);
        } catch (RuntimeException e) {
            log.error("Failed to initialize UploadRealtimeApiClient for env={}: {}", env, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Uploads a WISeR PA Results JSON payload to the esMD real-time API.
     *
     * @param bodyJson JSON payload (UTF-8)
     * @param letterId Letter identifier header value (required)
     * @param token OAuth2 bearer token (required; never logged)
     * @param uid Caller/user id for header "uid" (required)
     * @return parsed EsmdStatusResponse from esMD
     * @throws IllegalArgumentException for invalid inputs
     * @throws IllegalStateException for transport/IO errors
     * @throws HttpResponseException for non-2xx status codes
     * @throws JsonSyntaxException if the response JSON cannot be parsed
     */
    public EsmdStatusResponse uploadJsonToEsmd(String bodyJson, String letterId, String token, String uid) {
        SecurityUtils.validateNotNullOrEmpty(bodyJson, "bodyJson");
        SecurityUtils.validateAlphanumeric(letterId, "letterId");
        SecurityUtils.validateNotNullOrEmpty(token, "token");
        SecurityUtils.validateAlphanumeric(uid, "uid");
        SecurityUtils.validateJsonSize(bodyJson, "bodyJson");

        final byte[] bytes = bodyJson.getBytes(StandardCharsets.UTF_8);
        final int byteSize = bytes.length; // accurate size in bytes
        final String checksumHex = ChecksumUtil.sha256Hex(bytes);

        if (log.isDebugEnabled()) {
            log.debug("Preparing real-time upload env={}, letterId={}, sizeBytes={}, checksum={}",
                    environment, letterId, byteSize, checksumHex);
        }

        HttpPost request = createUploadRealtimeRequest(token, uid, checksumHex, bodyJson, byteSize, letterId);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            return processUploadRealtimeResponse(response);
        } catch (IOException ioe) {
            log.error("I/O error during real-time upload (env={}, letterId={}): {}", environment, letterId, ioe.getMessage(), ioe);
            throw new IllegalStateException("I/O error during real-time upload", ioe);
        }
    }

    /** Build the HTTP request with headers and JSON body. */
    private HttpPost createUploadRealtimeRequest(String token, String uid, String checksumHex,
                                                 String bodyJson, int sizeBytes, String letterId) {
        HttpPost request = new HttpPost(uploadRealtimeURL);
        request.setHeader(HDR_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        request.setHeader(HDR_AUTHORIZATION, "Bearer " + token);
        request.setHeader(HDR_CONTENT_CHECKSUM, checksumHex);
        request.setHeader(HDR_UID, uid);
        request.setHeader(HDR_SIZE, String.valueOf(sizeBytes));
        request.setHeader(HDR_LETTER_ID, letterId);
        request.setHeader(HDR_CONTENT_TYPE_CODE, contentTypeCode);
        request.setHeader(HDR_SENDER_ROUTING_ID, senderRoutingID);
        
        // Add security headers
        request.setHeader("User-Agent", "esmd-wiser-utility/1.0");
        request.setHeader("Accept", "application/json");
        
        request.setEntity(new StringEntity(bodyJson, ContentType.APPLICATION_JSON));

        if (log.isDebugEnabled()) {
            log.debug("Created UploadRealtime request url={} uid={} senderRoutingId={} contentTypeCode={} letterId={}",
                    uploadRealtimeURL, mask(uid), senderRoutingID, contentTypeCode, letterId);
        }
        return request;
    }

    /** Parse and validate the HTTP response; throw on non-2xx or malformed JSON. */
    private EsmdStatusResponse processUploadRealtimeResponse(CloseableHttpResponse response) throws IOException {
        final int statusCode = response.getStatusLine().getStatusCode();
        final HttpEntity entity = response.getEntity();
        final String body = (entity != null) ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : "";

        if (statusCode != HTTP_OK && statusCode != HTTP_CREATED && statusCode != HTTP_ACCEPTED) {
            log.error("Real-time upload failed: status={} body(snippet)={}", statusCode, snippet(body));
            throw new HttpResponseException(statusCode, "Real-time upload failed with status " + statusCode);
        }

        if (log.isDebugEnabled()) {
            log.debug("Real-time upload succeeded: status={} body(snippet)={}", statusCode, snippet(body));
        }

        try {
            return gson.fromJson(body, EsmdStatusResponse.class);
        } catch (JsonSyntaxException jse) {
            log.error("Failed to parse real-time upload response JSON. body(snippet)={}", snippet(body));
            throw jse;
        }
    }

    /**
     * Returns file names in a directory matching a given suffix (recursive).
     *
     * @param dirPath directory path
     * @param fileSuffix suffix to match (e.g., ".json")
     * @return sorted list of file names
     * @throws IllegalArgumentException if dirPath is invalid
     */
    public List<String> getOutboundLettersDocuments(String dirPath, String fileSuffix) {
        requireNonBlank(dirPath, "dirPath cannot be null or empty");
        requireNonBlank(fileSuffix, "fileSuffix cannot be null or empty");

        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + dir.getAbsolutePath());
        }

        Collection<File> files = FileUtils.listFiles(
                dir,
                FileFilterUtils.suffixFileFilter(fileSuffix),
                TrueFileFilter.INSTANCE
        );

        List<String> names = new ArrayList<>();
        for (File f : files) {
            if (!f.isDirectory()) {
                names.add(f.getName());
            }
        }
        names.sort(Comparator.naturalOrder());
        return names;
    }

    /* ============================== Helpers ============================== */

    private String buildUploadRealtimeURL(String env) {
        String baseUrl = apiProperties.getProperty(KEY_ENV_PREFIX + env);
        String endpoint = apiProperties.getProperty(KEY_UPLOAD_ENDPOINT);

        if (isBlank(baseUrl) || isBlank(endpoint)) {
            throw new IllegalStateException(
                    "Missing required configuration for environment: " + env +
                            " (expected keys: '" + KEY_ENV_PREFIX + env + "', '" + KEY_UPLOAD_ENDPOINT + "')"
            );
        }
        return baseUrl.trim() + endpoint.trim();
    }

    private String getRequiredProp(String key, String errorMessage) {
        String val = apiProperties.getProperty(key);
        if (isBlank(val)) throw new IllegalStateException(errorMessage + " [" + key + "]");
        return val.trim();
    }

    private int getIntProp(String key, int defVal) {
        String v = apiProperties.getProperty(key);
        if (isBlank(v)) return defVal;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException nfe) {
            log.warn("Invalid integer for {}='{}'; using default {}", key, v, defVal);
            return defVal;
        }
    }

    private static String requireNonBlank(String s, String msg) {
        if (isBlank(s)) throw new IllegalArgumentException(msg);
        return s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String snippet(String s) {
        if (s == null) return "";
        final int max = 1024;
        return (s.length() <= max) ? s : s.substring(0, max) + "...(truncated)";
    }

    // Mask potentially sensitive values for logs (simple)
    private static String mask(String val) {
        if (isBlank(val)) return "";
        if (val.length() <= 4) return "****";
        return "****" + val.substring(val.length() - 4);
    }
}
