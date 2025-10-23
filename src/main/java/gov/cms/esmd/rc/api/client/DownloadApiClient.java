package gov.cms.esmd.rc.api.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import gov.cms.esmd.bean.response.DownloadResponse;
import gov.cms.esmd.bean.response.PresignedUrlResponse;
import gov.cms.esmd.utility.PropertiesUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Properties;

/**
 * DownloadApiClient handles file download operations from esMD system.
 * This class provides methods to retrieve file lists and download files using presigned URLs.
 *
 * @author esMD Team
 * @version 1.0
 * @since 1.0
 */
public class DownloadApiClient implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(DownloadApiClient.class);
    private static final String CONFIG_FILE_PATH = System.getProperty("user.dir") + File.separator + "config" + File.separator + "esmd-rc-client-config.xml";

    // HTTP Status Codes
    private static final int HTTP_OK = 200;
    private static final int HTTP_ACCEPTED = 202;

    // Configuration properties
    private final Properties apiProperties;
    private final CloseableHttpClient httpClient;
    private final String downloadURL;
    private final String senderRoutingID;
    private final String environment;

    // Gson instance for JSON processing
    private final Gson gson = new Gson();

    /**
     * Constructs a new DownloadApiClient for the specified environment.
     *
     * @param env the environment (dev, val, uat, prod)
     * @throws IllegalArgumentException if environment is null or empty
     * @throws IllegalStateException if configuration cannot be loaded
     */
    public DownloadApiClient(String env) {
        validateEnvironment(env);
        this.environment = env;

        try {
            this.apiProperties = PropertiesUtils.loadProperties();
            this.httpClient = HttpClients.createDefault();
            this.downloadURL = buildDownloadURL(env);
            this.senderRoutingID = getSenderRoutingID();

            logger.info("DownloadApiClient initialized successfully for environment: {}", env);
        } catch (Exception e) {
            logger.error("Failed to initialize DownloadApiClient for environment: {}", env, e);
            throw new IllegalStateException("Failed to initialize DownloadApiClient", e);
        }
    }

    /**
     * Retrieves the list of files available for download from esMD.
     *
     * @param token authentication token
     * @param uid user identifier
     * @return DownloadResponse containing the list of files
     * @throws IllegalArgumentException if token or uid is null or empty
     * @throws IllegalStateException if the API call fails
     * @throws JsonSyntaxException if the response cannot be parsed
     */
    public DownloadResponse getListOfFiles(String token, String uid) {
        validateInputs(token, uid);

        logger.info("Retrieving file list for user: {} in environment: {}", uid, environment);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = createFileListRequest(token, uid);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return processFileListResponse(response);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve file list for user: {} in environment: {}", uid, environment, e);
            throw new IllegalStateException("Failed to retrieve file list", e);
        }
    }

    /**
     * Retrieves a presigned URL for downloading a specific file.
     *
     * @param key the file key/identifier
     * @param token authentication token
     * @param uid user identifier
     * @return PresignedUrlResponse containing the presigned URL
     * @throws IllegalArgumentException if any parameter is null or empty
     * @throws IllegalStateException if the API call fails
     * @throws JsonSyntaxException if the response cannot be parsed
     */
    public PresignedUrlResponse getDownloadPresignedURL(String key, String token, String uid) {
        validateInputs(key, token, uid);

        logger.info("Retrieving presigned URL for key: {} and user: {} in environment: {}", key, uid, environment);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpGet request = createPresignedUrlRequest(key, token, uid);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return processPresignedUrlResponse(response);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve presigned URL for key: {} and user: {} in environment: {}", key, uid, environment, e);
            throw new IllegalStateException("Failed to retrieve presigned URL", e);
        }
    }

    /**
     * Downloads a file using the provided presigned URL.
     *
     * @param presignedURL the presigned URL for downloading
     * @param localFilePath the local file path where the file should be saved
     * @param token authentication token
     * @return true if download was successful, false otherwise
     * @throws IllegalArgumentException if any parameter is null or empty
     * @throws IllegalStateException if the download fails
     */
    public boolean downloadFileWithPresignedURL(String presignedURL, String localFilePath, String token) {
        validateInputs(presignedURL, localFilePath, token);

        logger.info("Starting file download from presigned URL to: {}", localFilePath);

        try {
            URL url = new URL(presignedURL);
            Path localPath = Paths.get(localFilePath);

            // Ensure parent directory exists
            Files.createDirectories(localPath.getParent());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                configureConnection(connection, token);

                int responseCode = connection.getResponseCode();
                String responseMessage = connection.getResponseMessage();
                logger.debug("Download response code: {} for URL: {}", responseCode, presignedURL);

                if (responseCode == HTTP_OK) {
                    downloadFile(connection, localPath);
                    logger.info("File downloaded successfully to: {}", localFilePath);
                    return true;
                } else {
                    logger.error("Download failed with HTTP code: {}, Error Message: {}, for URL: {}", responseCode,responseMessage, presignedURL);
                    return false;
                }
            } finally {
                connection.disconnect();
            }
        } catch (MalformedURLException e) {
            logger.error("Invalid presigned URL: {}", presignedURL, e);
            throw new IllegalArgumentException("Invalid presigned URL", e);
        } catch (Exception e) {
            logger.error("Failed to download file from URL: {} to: {}", presignedURL, localFilePath, e);
            throw new IllegalStateException("Failed to download file", e);
        }
    }

    /**
     * Closes the HTTP client and releases resources.
     */
    @Override
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
                logger.debug("HTTP client closed successfully");
            }
        } catch (IOException e) {
            logger.warn("Error closing HTTP client", e);
        }
    }

    // Private helper methods

    private void validateEnvironment(String env) {
        if (env == null || env.trim().isEmpty()) {
            throw new IllegalArgumentException("Environment cannot be null or empty");
        }
    }

    private void validateInputs(String... inputs) {
        for (String input : inputs) {
            if (input == null || input.trim().isEmpty()) {
                throw new IllegalArgumentException("Input parameter cannot be null or empty");
            }
        }
    }

    private String buildDownloadURL(String env) {
        String baseUrl = apiProperties.getProperty("api.environment." + env);
        String endpoint = apiProperties.getProperty("api.url.download-url");

        if (baseUrl == null || endpoint == null) {
            throw new IllegalStateException("Missing required configuration for environment: " + env);
        }

        return baseUrl + endpoint;
    }

    private String getSenderRoutingID() {
        String routingId = apiProperties.getProperty("userinfo.mailboxid");
        if (routingId == null || routingId.trim().isEmpty()) {
            throw new IllegalStateException("Missing sender routing ID configuration");
        }
        return routingId.trim();
    }

    private HttpGet createFileListRequest(String token, String uid) {
        HttpGet request = new HttpGet(downloadURL);
        request.setHeader("uid", uid);
        request.setHeader("senderroutingid", senderRoutingID);
        request.setHeader("authorization",  token);
        request.setHeader("scope", apiProperties.getProperty("api.scope.download"));

        logger.debug("Created file list request for URL: {} with headers: uid={}, senderroutingid={}",
                downloadURL, uid, senderRoutingID);
        return request;
    }

    private HttpGet createPresignedUrlRequest(String key, String token, String uid) {
        String presignedUrlEndpoint = downloadURL + "/" + key;
        HttpGet request = new HttpGet(presignedUrlEndpoint);
        request.setHeader("uid", uid);
        request.setHeader("senderroutingid", senderRoutingID);
        request.setHeader("authorization",  token);
        request.setHeader("scope", apiProperties.getProperty("api.scope.download"));

        logger.debug("Created presigned URL request for URL: {} with headers: uid={}, senderroutingid={}",
                presignedUrlEndpoint, uid, senderRoutingID);
        return request;
    }

    private DownloadResponse processFileListResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String responseContent = EntityUtils.toString(entity);

        logger.debug("File list response - Status: {}, Content length: {}", statusCode, entity.getContentLength());

        if (statusCode != HTTP_OK && statusCode != HTTP_ACCEPTED) {
            logger.error("File list request failed with status: {}, response: {}", statusCode, responseContent);
            throw new IllegalStateException("File list request failed with status: " + statusCode);
        }

        try {
            return gson.fromJson(responseContent, DownloadResponse.class);
        } catch (JsonSyntaxException e) {
            logger.error("Failed to parse file list response: {}", responseContent, e);
            throw new JsonSyntaxException("Invalid JSON response for file list", e);
        }
    }

    private PresignedUrlResponse processPresignedUrlResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String responseContent = EntityUtils.toString(entity);

        logger.debug("Presigned URL response - Status: {}, Content length: {}", statusCode, entity.getContentLength());

        if (statusCode != HTTP_OK && statusCode != HTTP_ACCEPTED) {
            logger.error("Presigned URL request failed with status: {}, response: {}", statusCode, responseContent);
            throw new IllegalStateException("Presigned URL request failed with status: " + statusCode);
        }

        try {
            return gson.fromJson(responseContent, PresignedUrlResponse.class);
        } catch (JsonSyntaxException e) {
            logger.error("Failed to parse presigned URL response: {}", responseContent, e);
            throw new JsonSyntaxException("Invalid JSON response for presigned URL", e);
        }
    }

    private void configureConnection(HttpURLConnection connection, String token) throws IOException {
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("authorization", token);
        //connection.setConnectTimeout(30000); // 30 seconds
        //connection.setReadTimeout(60000); // 60 seconds
    }

    private void downloadFile(HttpURLConnection connection, Path localPath) throws IOException {
        try (InputStream inputStream = connection.getInputStream()) {
            Files.copy(inputStream, localPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // Getters for testing purposes
    public String getEnvironment() {
        return environment;
    }

    public String getDownloadURL() {
        return downloadURL;
    }


}