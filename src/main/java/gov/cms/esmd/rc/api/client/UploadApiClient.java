package gov.cms.esmd.rc.api.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import gov.cms.esmd.bean.response.PresignedUrlResponse;
import gov.cms.esmd.utility.FileUtils;
import gov.cms.esmd.utility.PropertiesUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import static gov.cms.esmd.utility.FileUtils.readBytes;

public class UploadApiClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(UploadApiClient.class);
    private static final String CONFIG_FILE_PATH = System.getProperty("user.dir") +
            File.separator + "config" + File.separator + "esmd-rc-client-config.xml";

    // HTTP Status Codes
    private static final int HTTP_OK = 200;
    private static final int HTTP_ACCEPTED = 202;

    // Configuration properties
    private final Properties apiProperties;
    private final CloseableHttpClient httpClient;
    private final String uploadURL;
    private final String senderRoutingID;
    private final String environment;
    // Gson instance for JSON processing
    private final Gson gson = new Gson();
    /**
     * Constructs a new UploadApiClient for the specified environment.
     *
     * @param env the environment (dev, val, uat, prod)
     * @throws IllegalArgumentException if environment is null or empty
     * @throws IllegalStateException    if configuration cannot be loaded
     */
    public UploadApiClient(String env) {
        validateEnvironment(env);
        this.environment = env;

        try {
            this.apiProperties = PropertiesUtils.loadProperties();
            this.httpClient = HttpClients.createDefault();
            this.uploadURL = buildUploadURL(env);
            this.senderRoutingID = getSenderRoutingID();

            logger.info("UploadApiClient initialized successfully for environment: {}", env);
        } catch (Exception e) {
            logger.error("Failed to initialize UploadApiClient for environment: {}", env, e);
            throw new IllegalStateException("Failed to initialize UploadApiClient", e);
        }
    }


    /**
     * Retrieves a presigned URL for Uploading a specific file.
     *
     * @param key   the file key/identifier
     * @param token authentication token
     * @param uid   user identifier
     * @return PresignedUrlResponse containing the presigned URL
     * @throws IllegalArgumentException if any parameter is null or empty
     * @throws IllegalStateException    if the API call fails
     * @throws JsonSyntaxException      if the response cannot be parsed
     */


    public PresignedUrlResponse getUploadPresignedURL(String key, String token,
                                                      String uid, String md5Hex, String fileSize) {
        validateInputs(key, token, uid);

        logger.info("Retrieving presigned URL for key: {} and user: {} in environment: {}", key, uid, environment);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = createPresignedUrlRequest(key, token, uid, md5Hex, fileSize);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return processPresignedUrlResponse(response);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve presigned URL for key: {} and user: {} in environment: {}", key, uid, environment, e);
            throw new IllegalStateException("Failed to retrieve presigned URL", e);
        }
    }

    public PresignedUrlResponse processPresignedUrlResponse(CloseableHttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String responseContent = EntityUtils.toString(entity);

        logger.info("Presigned URL response - Status: {}, Content length: {}, Response Content: {}",
                statusCode, entity.getContentLength(), responseContent);

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

    /**
     * This method uploads given file to AWS S3 bucket using the presigned URL.
     *
     * @param preSignedURL presigned URL of the S3 bucket.
     * @param file   The file to be uploaded.
     * @throws java.io.IOException
     * @throws Exception
     * @throws IOException
     */
    public boolean uploadFileWithPresignedURL(
            String preSignedURL, File file, String token, String md5Hex) throws IOException {
        boolean isUploadSuccessful = true;
        if (preSignedURL == null || preSignedURL.trim().isEmpty()) {
            throw new IllegalArgumentException("Pre-signed URL is null or empty");
        }

        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        long contentLength = file.length();
        logger.info("Uploading file [{}] with size: {} bytes", file.getName(), contentLength);

        URL uploadURL = new URL(preSignedURL);

        HttpURLConnection connection = null;
        try {
            logger.info("Connecting to upload URL...");
            connection = (HttpURLConnection) uploadURL.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-MD5", md5Hex);
            connection.setRequestProperty("Content-Type","application/zip");
            connection.setRequestProperty("Authorization", token);

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            logger.info("uploadFileWithPresignedURL , : Upload URL Connection Successful");
            long partsize =  Long.parseLong(PropertiesUtils.getRequiredProperty("api.file-upload.partsize"));
            // Upload the file parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // The last part could be less than 5 MB, adjust the part size as needed.
                partsize = Math.min(partsize, (contentLength - filePosition));
                logger.info("uploadFileWithPresignedURL ,FILE PART: " + partsize);
                byte[] bytes = FileUtils.readBytes(file,new Long(partsize).intValue());
                out.write(bytes);
                filePosition += partsize;
            }
            logger.info("uploadFileWithPresignedURL ,CONTENT LENGTH : "+ new Long(contentLength).intValue());

            out.close();
            connection.getResponseCode();

            BufferedReader br = null;
            if (connection.getResponseCode() == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String strCurrentLine;
                while ((strCurrentLine = br.readLine()) != null) {
                    logger.info(strCurrentLine);
                }
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                String strCurrentLine;
                while ((strCurrentLine = br.readLine()) != null) {
                    logger.error(strCurrentLine);
                }
            }
        } catch (IOException ioe) {
            logger.error("IOException in uploadFileWithPresignedURL :"+ ioe.getMessage());
            ioe.printStackTrace();
            isUploadSuccessful = false;
            throw ioe;
        }catch (Exception ex){
            logger.error("IOException in uploadFileWithPresignedURL :"+ ex.getMessage());
            isUploadSuccessful = false;
            ex.printStackTrace();
            throw ex;
        }
        return isUploadSuccessful;
    }

    // Private helper methods

        private void validateEnvironment (String env){
            if (env == null || env.trim().isEmpty()) {
                throw new IllegalArgumentException("Environment cannot be null or empty");
            }
        }

        private String buildUploadURL (String env){
            String baseUrl = apiProperties.getProperty("api.environment." + env);
            String endpoint = apiProperties.getProperty("api.url.download-url");

            if (baseUrl == null || endpoint == null) {
                throw new IllegalStateException("Missing required configuration for environment: " + env);
            }

            return baseUrl + endpoint;
        }

        private String getSenderRoutingID () {
            String routingId = apiProperties.getProperty("userinfo.mailboxid");
            if (routingId == null || routingId.trim().isEmpty()) {
                throw new IllegalStateException("Missing sender routing ID configuration");
            }
            return routingId.trim();
        }

        private HttpGet createFileListRequest (String token, String uid){
            HttpGet request = new HttpGet(uploadURL);
            request.setHeader("uid", uid);
            request.setHeader("senderroutingid", senderRoutingID);
            request.setHeader("authorization", "Bearer " + token);
            request.setHeader("scope", apiProperties.getProperty("api.scope.download"));

            logger.debug("Created file list request for URL: {} with headers: uid={}, senderroutingid={}",
                    uploadURL, uid, senderRoutingID);
            return request;
        }

        private HttpPost createPresignedUrlRequest (String filename, String token,
                                                    String uid, String md5Hex, String fileSize){
            HttpPost request = new HttpPost(uploadURL);

            request.setHeader("filename", filename);
            request.setHeader("Content-Type", "application/zip");
            request.setHeader("uid", uid);
            request.setHeader("size", fileSize);
            request.setHeader("scope",apiProperties.getProperty("api.scope.upload"));
            request.setHeader("Authorization", token);
            request.setHeader("contentchecksum", md5Hex);
            request.setHeader("senderroutingid", senderRoutingID);

            logger.debug("Created presigned URL request for URL: {} with headers: uid={}, senderroutingid={}",
                    uploadURL, uid, senderRoutingID);
            return request;
        }

    private void validateInputs(String... inputs) {
        for (String input : inputs) {
            if (input == null || input.trim().isEmpty()) {
                throw new IllegalArgumentException("Input parameter cannot be null or empty");
            }
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
}
