package gov.cms.esmd.rc.api.client;

import com.google.gson.Gson;
import gov.cms.esmd.bean.response.NotificationResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * StatusApiClient
 *
 * Retrieves latest esMD status for a transaction.
 * - Uses HttpClient 4.x with configurable timeouts
 * - Validates inputs; never logs tokens
 * - Logs only small body snippets
 * - Throws HttpResponseException on non-200
 *
 * NOTE: Header "uid" is set to the provided esmdTransactionId per existing code.
 *       Verify with API contract if "uid" should instead be a client ID.
 */
public class StatusApiClient implements AutoCloseable, Closeable {
    private static final Logger log = LoggerFactory.getLogger(StatusApiClient.class);

    // Defaults (ms)
    private static final int DEF_CONNECT_TIMEOUT_MS = 10_000;
    private static final int DEF_SOCKET_TIMEOUT_MS  = 30_000;
    private static final int DEF_CONN_REQ_TIMEOUT_MS= 10_000;

    private final CloseableHttpClient httpClient;
    private final String statusUrl; // default endpoint
    private final Gson gson = new Gson();

    /** Build with defaults and sane timeouts. */
    public StatusApiClient(String statusUrl) {
        this(statusUrl, RequestConfig.custom()
                .setConnectTimeout(DEF_CONNECT_TIMEOUT_MS)
                .setSocketTimeout(DEF_SOCKET_TIMEOUT_MS)           // 4.x API
                .setConnectionRequestTimeout(DEF_CONN_REQ_TIMEOUT_MS)
                .build());
    }

    /** Build with custom RequestConfig (timeouts, etc.). */
    public StatusApiClient(String statusUrl, RequestConfig requestConfig) {
        if (isBlank(statusUrl)) throw new IllegalArgumentException("statusUrl cannot be null or empty");
        this.statusUrl = statusUrl.trim();
        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)) // caller decides retries
                .build();
        log.info("StatusApiClient initialized: url={}", this.statusUrl);
    }

    /**
     * Retrieve latest status for the given transaction.
     *
     * @param esMDTransactionId transaction identifier (required)
     * @param mailboxId         sender routing ID header value (required)
     * @param token             bearer token or raw token (required)
     * @param url               optional override endpoint; if blank, uses constructor URL
     * @return parsed NotificationResponse
     * @throws IllegalArgumentException on invalid inputs
     * @throws IllegalStateException on I/O errors
     * @throws HttpResponseException on non-200 HTTP status
     */
    public NotificationResponse retrieveLatestStatusByTransactionId(
            String esMDTransactionId, String mailboxId, String token, String url) {

        requireNonBlank(esMDTransactionId, "esMDTransactionId cannot be null or empty");
        requireNonBlank(mailboxId, "mailboxId cannot be null or empty");
        requireNonBlank(token, "token cannot be null or empty");

        final String endpoint = isBlank(url) ? this.statusUrl : url.trim();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving status: txId={}, mailboxId={}, endpoint={}",
                    safe(esMDTransactionId), mailboxId, endpoint);
        }

        HttpGet get = new HttpGet(endpoint);
        get.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        get.setHeader("Authorization", normalizeBearer(token));
        get.setHeader("uid", esMDTransactionId);
        get.setHeader("senderroutingid", mailboxId);
        if(!isBlank(esMDTransactionId)) {
            get.setHeader("esMDTransactionId", esMDTransactionId);
        }

        try (CloseableHttpResponse resp = httpClient.execute(get)) {
            int status = resp.getStatusLine().getStatusCode();
            String body = (resp.getEntity() != null)
                    ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8)
                    : "";

            if (status != 200) {
                log.error("Status request failed: status={} body(snippet)={}", status, snippet(body));
                throw new HttpResponseException(status, "Status request failed with status " + status);
            }

            if (log.isDebugEnabled()) {
                log.debug("Status request succeeded: status={} body(snippet)={}", status, snippet(body));
            }

            return gson.fromJson(body, NotificationResponse.class);
        } catch (IOException ioe) {
            log.error("I/O error during status request (txId={}, endpoint={}): {}",
                    safe(esMDTransactionId), endpoint, ioe.getMessage(), ioe);
            throw new IllegalStateException("I/O error during status request", ioe);
        } catch (RuntimeException re) {
            log.error("Unexpected error during status request (txId={}, endpoint={}): {}",
                    safe(esMDTransactionId), endpoint, re.getMessage(), re);
            throw re;
        }
    }

    /**
     * Retrieve latest status for the Routing ID.
     *
     * @param mailboxId         sender routing ID header value (required)
     * @param token             bearer token or raw token (required)
     * @param url               optional override endpoint; if blank, uses constructor URL
     * @return parsed NotificationResponse
     * @throws IllegalArgumentException on invalid inputs
     * @throws IllegalStateException on I/O errors
     * @throws HttpResponseException on non-200 HTTP status
     */
    public NotificationResponse retrieveLatestStatus(
            String esMDTransactionId, String mailboxId, String token, String url) {

        requireNonBlank(mailboxId, "mailboxId cannot be null or empty");
        requireNonBlank(token, "token cannot be null or empty");

        final String endpoint = isBlank(url) ? this.statusUrl : url.trim();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving status:  mailboxId={}, endpoint={}",
                     mailboxId, endpoint);
        }

        HttpGet get = new HttpGet(endpoint);
        get.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        get.setHeader("Authorization", normalizeBearer(token));
        get.setHeader("senderroutingid", mailboxId);
        get.setHeader("uid", "mailboxId");
        if(!isBlank(esMDTransactionId)) {
            get.setHeader("esMDTransactionId", esMDTransactionId);
        }

        try (CloseableHttpResponse resp = httpClient.execute(get)) {
            int status = resp.getStatusLine().getStatusCode();
            String body = (resp.getEntity() != null)
                    ? EntityUtils.toString(resp.getEntity(), StandardCharsets.UTF_8)
                    : "";

            if (status != 200) {
                log.error("Status request failed: status={} body(snippet)={}", status, snippet(body));
                throw new HttpResponseException(status, "Status request failed with status " + status);
            }

            if (log.isDebugEnabled()) {
                log.debug("Status request succeeded: status={} body(snippet)={}", status, snippet(body));
            }

            return gson.fromJson(body, NotificationResponse.class);
        } catch (IOException ioe) {
            log.error("I/O error during status request ( endpoint={}): {}",
                     endpoint, ioe.getMessage(), ioe);
            throw new IllegalStateException("I/O error during status request", ioe);
        } catch (RuntimeException re) {
            log.error("Unexpected error during status request ( endpoint={}): {}",
                     endpoint, re.getMessage(), re);
            throw re;
        }
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    /* ============================== Helpers ============================== */

    private static void requireNonBlank(String s, String msg) {
        if (isBlank(s)) throw new IllegalArgumentException(msg);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String normalizeBearer(String tokenOrHeader) {
        String t = tokenOrHeader.trim();
        return t.regionMatches(true, 0, "Bearer ", 0, 7) ? t : "Bearer " + t;
    }

    private static String safe(String value) {
        if (isBlank(value)) return "";
        return value.length() <= 12 ? value : value.substring(0, 4) + "..." + value.substring(value.length() - 4);
    }

    private static String snippet(String s) {
        if (s == null) return "";
        int max = 1024;
        return s.length() <= max ? s : s.substring(0, max) + "...(truncated)";
    }
}
