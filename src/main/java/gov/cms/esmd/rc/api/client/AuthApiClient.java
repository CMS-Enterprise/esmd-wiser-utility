package gov.cms.esmd.rc.api.client;

import gov.cms.esmd.bean.auth.response.AuthResponse;
import gov.cms.esmd.bean.auth.response.ErrorResponse;
import gov.cms.esmd.utility.JSONUtility;
import gov.cms.esmd.utility.PropertiesUtils;
import gov.cms.esmd.utility.SecurityUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * AuthApiClient
 *
 * Obtains OAuth tokens from the esMD Auth API.
 * - Uses HttpClient 4.x with configurable timeouts
 * - Validates required inputs & properties
 * - Never logs secrets; logs only masked identifiers
 * - On non-200, returns an AuthResponse with an error message
 * - On malformed/failed success response, throws IllegalStateException
 */
public class AuthApiClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(AuthApiClient.class);

    // Property keys
    private static final String KEY_ENV_PREFIX     = "api.environment.";
    private static final String KEY_AUTH_ENDPOINT  = "api.url.auth";
    private static final String KEY_MAILBOX_ID     = "userinfo.mailboxid";
    private static final String KEY_CLIENT_ID      = "userinfo.clientid";
    private static final String KEY_CLIENT_SECRET  = "userinfo.clientsecret";

    // Optional timeout keys (ms)
    private static final String KEY_CONNECT_TIMEOUT = "http.connect.timeout.ms";
    private static final String KEY_SOCKET_TIMEOUT  = "http.socket.timeout.ms";
    private static final String KEY_CONN_REQ_TIMEOUT= "http.connection.request.timeout.ms";

    // Defaults
    private static final int DEF_CONNECT_TIMEOUT_MS = 10_000;
    private static final int DEF_SOCKET_TIMEOUT_MS  = 30_000;
    private static final int DEF_CONN_REQ_TIMEOUT_MS= 10_000;

    // HTTP
    private final CloseableHttpClient httpClient;

    // Config
    private final Properties apiProperties;
    private final String authUrl;
    private final String mailboxId;
    private final String clientId;
    private final String clientSecret;

    public AuthApiClient(String env) {
        SecurityUtils.validateAlphanumeric(env, "Environment");

        this.apiProperties = PropertiesUtils.loadProperties();
        String baseUrl   = getRequiredProp(KEY_ENV_PREFIX + env, "Missing base URL for env='" + env + "'");
        String endpoint  = getRequiredProp(KEY_AUTH_ENDPOINT, "Missing auth endpoint property");
        this.authUrl     = baseUrl.trim() + endpoint.trim();

        this.mailboxId   = getRequiredProp(KEY_MAILBOX_ID, "Missing mailbox id property");
        this.clientId    = getRequiredProp(KEY_CLIENT_ID, "Missing client id property");
        this.clientSecret= getRequiredProp(KEY_CLIENT_SECRET, "Missing client secret property");

        int connectTimeout = getIntProp(KEY_CONNECT_TIMEOUT, DEF_CONNECT_TIMEOUT_MS);
        int socketTimeout  = getIntProp(KEY_SOCKET_TIMEOUT,  DEF_SOCKET_TIMEOUT_MS);
        int connReqTimeout = getIntProp(KEY_CONN_REQ_TIMEOUT, DEF_CONN_REQ_TIMEOUT_MS);

        RequestConfig rc = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)                // HttpClient 4.x
                .setConnectionRequestTimeout(connReqTimeout)
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(rc)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .build();

        log.info("AuthApiClient initialized: env={}, url={}", env, authUrl);
    }

    /**
     * Get an access token for the requested scope.
     *
     * @param scope required API scope (e.g., "rc/upload")
     * @return AuthResponse; on HTTP non-200 returns an object with error set; on a malformed success body throws
     */
    public AuthResponse getToken(String scope) {
        SecurityUtils.validateNotNullOrEmpty(scope, "Scope");

        log.debug("Auth token request: mailboxId={}, clientId={}", mailboxId, mask(clientId));

        HttpPost request = new HttpPost(authUrl);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("clientid", clientId);
        request.addHeader("clientsecret", clientSecret);
        request.addHeader("scope", scope);
        request.addHeader("mailboxid", mailboxId);
        
        // Add security headers
        request.addHeader("User-Agent", "esmd-wiser-utility/1.0");
        request.addHeader("Accept", "application/json");

        try {
            try (org.apache.http.client.methods.CloseableHttpResponse response = httpClient.execute(request)) {
                int status = response.getStatusLine().getStatusCode();
                String body = (response.getEntity() != null)
                        ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)
                        : "";

                if (status != 200) {
                    // Try to parse a structured error, but always return an AuthResponse with an error message
                    ErrorResponse er = tryParse(body, ErrorResponse.class);
                    AuthResponse ar = new AuthResponse();
                    ar.setStatusCode(String.valueOf(status));
                    ar.setError(er != null && !isBlank(er.getError()) ? er.getError()
                            : "Request failed with status " + status);
                    log.warn("Auth token request failed: status={}, error={}", status, ar.getError());
                    return ar;
                }

                AuthResponse auth = JSONUtility.deserialize(body, AuthResponse.class);
                // Validate a "successful" body still has a token and no error
                if (auth == null || !isBlank(auth.getError()) || isBlank(auth.getAccess_token())) {
                    String msg = "Authentication failed: invalid success response (missing token or error present)";
                    log.error("{} body(snippet)={}", msg, snippet(body));
                    throw new IllegalStateException(msg);
                }

                // Normalize Authorization header format for callers that want it directly
                auth.setAccess_token("Bearer " + auth.getAccess_token());
                log.debug("Auth token acquired successfully");
                return auth;
            }
        } catch (IOException io) {
            log.error("I/O error during auth request: {}", io.getMessage(), io);
            throw new IllegalStateException("I/O error during auth request", io);
        } catch (RuntimeException re) {
            // Preserve stack and add context
            log.error("Unexpected error during auth: {}", re.getMessage(), re);
            throw re;
        }
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    /* ============================== Helpers ============================== */

    private <T> T tryParse(String json, Class<T> type) {
        try {
            return JSONUtility.deserialize(json, type);
        } catch (Exception ignore) {
            return null;
        }
    }

    private String getRequiredProp(String key, String err) {
        String v = apiProperties.getProperty(key);
        if (isBlank(v)) throw new IllegalStateException(err + " [" + key + "]");
        return v.trim();
    }

    private int getIntProp(String key, int def) {
        String v = apiProperties.getProperty(key);
        if (isBlank(v)) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException nfe) {
            log.warn("Invalid integer for {}='{}'; using default {}", key, v, def);
            return def;
        }
    }


    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String mask(String value) {
        if (isBlank(value)) return "";
        return (value.length() <= 4) ? "****" : "****" + value.substring(value.length() - 4);
    }

    private static String snippet(String s) {
        if (s == null) return "";
        int max = 512;
        return s.length() <= max ? s : s.substring(0, max) + "...(truncated)";
    }

    public static void main(String[] args) {
        AuthApiClient authApiClient = new AuthApiClient("dev") ;
        AuthResponse authResponse= authApiClient.getToken("wiser/download");
        log.info("Token - , {}", authResponse.getAccess_token());
    }
}

