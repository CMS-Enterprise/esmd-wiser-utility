package gov.cms.esmd.rc.api.client;

import com.google.gson.Gson;
import gov.cms.esmd.bean.adminerror.Notification;
import gov.cms.esmd.bean.response.NotificationResponse;
import gov.cms.esmd.bean.response.PickupNotification;
import gov.cms.esmd.utility.NotificationUtility;
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

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * NotificationApiClient
 *
 * Sends notification JSON to an esMD endpoint.
 * - Uses HttpClient 4.x with sane, configurable timeouts
 * - Validates inputs; never logs tokens or full response bodies
 * - Accepts 200/201/202 as success; throws HttpResponseException otherwise
 */
public class NotificationApiClient implements AutoCloseable, Closeable {

    private static final Logger log = LoggerFactory.getLogger(NotificationApiClient.class);

    // Defaults (ms). Override by building a custom RequestConfig if needed.
    private static final int DEF_CONNECT_TIMEOUT_MS = 10_000;
    private static final int DEF_SOCKET_TIMEOUT_MS  = 30_000;
    private static final int DEF_CONN_REQ_TIMEOUT_MS= 10_000;

    private final CloseableHttpClient httpClient;
    private final String notificationUrl;   // default endpoint
    private final Gson gson = new Gson();

    /**
     * Build with a default URL and default timeouts.
     */
    public NotificationApiClient(String notificationUrl) {
        this(notificationUrl, RequestConfig.custom()
                .setConnectTimeout(DEF_CONNECT_TIMEOUT_MS)
                .setSocketTimeout(DEF_SOCKET_TIMEOUT_MS)          // 4.x API
                .setConnectionRequestTimeout(DEF_CONN_REQ_TIMEOUT_MS)
                .build());
    }

    /**
     * Build with a default URL and a custom RequestConfig (timeouts, etc.).
     */
    public NotificationApiClient(String notificationUrl, RequestConfig requestConfig) {
        if (isBlank(notificationUrl)) {
            throw new IllegalArgumentException("notificationUrl cannot be null or empty");
        }
        this.notificationUrl = notificationUrl.trim();
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)) // caller handles retries
                .build();
        log.info("NotificationApiClient initialized: url={}", this.notificationUrl);
    }

    /**
     * Submit a notification to the given URL (or the default if url is blank).
     *
     * @param token       Authorization token: either the full header value ("Bearer ...") or the raw token
     * @param jsonString  Notification payload (JSON, UTF-8)
     * @param type        Notification type (for logs only)
     * @param url         Optional override endpoint; if blank, uses the constructor URL
     *
     * @return parsed NotificationResponse on success (HTTP 200/201/202)
     * @throws IllegalArgumentException on invalid inputs
     * @throws IllegalStateException on I/O errors
     * @throws HttpResponseException on non-success HTTP status
     */
    public NotificationResponse submitNotification(String token, String jsonString, String type, String url) {
        requireNonBlank(jsonString, "jsonString cannot be null or empty");
        requireNonBlank(token, "token cannot be null or empty");
        String endpoint = isBlank(url) ? this.notificationUrl : url.trim();

        if (log.isDebugEnabled()) {
            log.debug("Submitting notification: type={}, endpoint={}, payloadBytes={}",
                    safe(type), endpoint, jsonString.getBytes(StandardCharsets.UTF_8).length);
        }

        HttpPost postRequest = new HttpPost(endpoint);
        postRequest.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        postRequest.setHeader("Authorization", normalizeBearer(token));
        postRequest.setEntity(new StringEntity(jsonString, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String body = (response.getEntity() != null)
                    ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)
                    : "";

            // Treat 200/201/202 as success
            if (statusCode != 200 && statusCode != 201 && statusCode != 202) {
                log.error("Notification failed: status={} body(snippet)={}", statusCode, snippet(body));
                throw new HttpResponseException(statusCode, "Notification request failed with status " + statusCode);
            }

            if (log.isDebugEnabled()) {
                log.debug("Notification succeeded: status={} body(snippet)={}", statusCode, snippet(body));
            }

            return gson.fromJson(body, NotificationResponse.class);
        } catch (IOException ioe) {
            log.error("I/O error during notification (type={}, endpoint={}): {}",
                    safe(type), endpoint, ioe.getMessage(), ioe);
            throw new IllegalStateException("I/O error during notification", ioe);
        } catch (RuntimeException re) {
            log.error("Unexpected error during notification (type={}, endpoint={}): {}",
                    safe(type), endpoint, re.getMessage(), re);
            throw re;
        }
    }
    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    /* ============================== Helpers ============================== */

    private static String normalizeBearer(String tokenOrHeader) {
        String t = tokenOrHeader.trim();
        return t.regionMatches(true, 0, "Bearer ", 0, 7) ? t : "Bearer " + t;
    }

    private static void requireNonBlank(String s, String msg) {
        if (isBlank(s)) throw new IllegalArgumentException(msg);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safe(String value) {
        if (isBlank(value)) return "";
        return value.length() <= 32 ? value : value.substring(0, 32) + "...";
    }

    private static String snippet(String s) {
        if (s == null) return "";
        int max = 1024;
        return s.length() <= max ? s : s.substring(0, max) + "...(truncated)";
    }
}
