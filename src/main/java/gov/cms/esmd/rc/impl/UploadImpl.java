package gov.cms.esmd.rc.impl;

import com.google.gson.Gson;
import gov.cms.esmd.bean.ErrorMessage;
import gov.cms.esmd.bean.auth.response.AuthResponse;
import gov.cms.esmd.bean.response.DownloadObject;
import gov.cms.esmd.bean.response.DownloadResponse;
import gov.cms.esmd.bean.response.PresignedUrlResponse;
import gov.cms.esmd.bean.response.StatusDetail;
import gov.cms.esmd.rc.api.client.AuthApiClient;
import gov.cms.esmd.rc.api.client.DownloadApiClient;
import gov.cms.esmd.rc.api.client.UploadApiClient;
import gov.cms.esmd.utility.ChecksumUtil;
import gov.cms.esmd.utility.FileUtils;
import gov.cms.esmd.utility.PropertiesUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * UploadImpl handles the complete workflow for uploading Wiser requests to esMD.
 * This class orchestrates UploadImpl {

    private static final Logger logger = LoggerFactory.getLogger(DownloadImpl.class);

    private final Properties apiProperties;

    /**
     * Constructs a new DownloadImpl instance.
     * Loads the API properties from the YAML configuration file.
     */

public class UploadImpl{
private static final Logger logger = LoggerFactory.getLogger(DownloadApiClient.class);
private static final String CONFIG_FILE_PATH = System.getProperty("user.dir") + File.separator + "config" + File.separator + "esmd-rc-client-config.xml";

// HTTP Status Codes
private static final int HTTP_OK = 200;
private static final int HTTP_ACCEPTED = 202;

// Configuration properties
private final Properties apiProperties;
// Gson instance for JSON processing
private final Gson gson = new Gson();

    public UploadImpl() {
        this.apiProperties = PropertiesUtils.loadProperties();
        logger.info("DownloadImpl initialized successfully");
    }

    /**
     * Uploads Wiser requests to esMD for the specified environment.
     * This method performs the complete workflow:
     * Gets authentication token using the download scope
     * Gets a presigned URL for the file to be uploaded
     * Upload the file to esMD using presigned URL
     *
     * @param environment the target environment (dev, val, uat, prod)
     * @return List of StatusDetail objects containing download status for each zip file
     * @throws Exception if any step in the download process fails
     */
    public StatusDetail uploadWiserRequestsToesMD(String environment) throws Exception {
        logger.info("Starting upload process for environment: {}", environment);

        // Initialize status details list
        StatusDetail statusDetail = new StatusDetail(new ArrayList<>());
        statusDetail.setContenttypecd("ZIP");
        statusDetail.setDeliveryType("UPLOAD");
        statusDetail.setStatus("SUCCESS");
        statusDetail.setStatusDescription("File Upload processing initiated");
        // Validate environment parameter
        if (environment == null || environment.trim().isEmpty()) {
            throw new IllegalArgumentException("Environment cannot be null or empty");
        }

        // Read configuration from YAML properties
        String scope = PropertiesUtils.getRequiredProperty("api.scope.upload");
        String clientId = PropertiesUtils.getRequiredProperty("userinfo.clientid");
        String localFilePath = PropertiesUtils.getRequiredProperty("api.file-upload.local-path");

        logger.debug("Configuration loaded - scope: {}, clientId: {}, localPath: {}",
                scope, clientId, localFilePath);

        try (AuthApiClient authClient = new AuthApiClient(environment);
             UploadApiClient uploadApiClient = new UploadApiClient(environment)) {

            // Step 1: Get authentication token
            logger.info("Step 1: Getting authentication token for scope: {}", scope);
            AuthResponse authResponse = authClient.getToken(scope);

            if (authResponse.getError() != null && !authResponse.getError().isEmpty()) {
                throw new IllegalStateException("Authentication failed: " + authResponse.getError());
            }

            String token = authResponse.getAccess_token();
            logger.info("Authentication successful, token acquired");
            String uploadDirectory = apiProperties.getProperty("api.file-upload.local-path");

            List<Path> files = FileUtils.getFilesFromDirectory(uploadDirectory);

            for (Path path : files) {
                File file = path.toFile();

                // Step 3: Get presigned URL for the file
                logger.debug("Getting presigned URL for file: {}", file);
                String fileSizeInMB = FileUtils.getFileSize(file,"MB","0.0000");
                //String md5Hex = ChecksumUtil.md5Hex(file.getAbsolutePath());
                String md5Hex = ChecksumUtil.checkMD5(file.getAbsolutePath());
                String filename = file.getName();
                PresignedUrlResponse presignedUrlResponse = uploadApiClient.getUploadPresignedURL(
                        filename, token, clientId,md5Hex, fileSizeInMB);

                if (presignedUrlResponse.getContents() == null || presignedUrlResponse.getContents().isEmpty()) {
                    logger.warn("No presigned URL content received for file: {}", filename);
                    statusDetail.setStatus("FAILED");
                    statusDetail.setStatusDescription("No presigned URL content received");
                    statusDetail.getErrorMessages().add(new ErrorMessage(
                            "NO_PRESIGNED_URL", "Missing URL", "No presigned URL content received for file: " + filename));
                    return statusDetail;
                }

                // Get the first content object (assuming one URL per file)
                String presignedUrl = presignedUrlResponse.getContents().get(0).getUrl();
                if (presignedUrl == null || presignedUrl.trim().isEmpty()) {
                    logger.warn("Empty presigned URL received for file: {}", filename);
                    statusDetail.setStatus("FAILED");
                    statusDetail.setStatusDescription("Empty presigned URL received");
                    statusDetail.getErrorMessages().add(new ErrorMessage("EMPTY_PRESIGNED_URL", "Empty URL", "Empty presigned URL received for file: " + filename));
                    return statusDetail;
                }
                logger.debug("Presigned URL obtained for file: {}", filename);
                boolean isUplaodSuccess = uploadApiClient.uploadFileWithPresignedURL(presignedUrl, file, token, md5Hex);

                if (!isUplaodSuccess) {
                    logger.error("Failed to upload file: {}", filename);
                    statusDetail.setStatus("FAILED");
                    statusDetail.setStatusDescription("upload failed");
                    statusDetail.getErrorMessages().add(new ErrorMessage("UPLOAD_FAILED", "Upload Error", "Failed to upload file: " + filename));
                    return statusDetail;
                }
            }
            logger.info("Upload process completed for environment: {}", environment);
        } catch (Exception e) {
            logger.error("Upload process failed for environment: {}", environment, e);
            throw e;
        }
        return statusDetail;
    }


    public static void main(String[] args) throws Exception {
        UploadImpl uploadImpl = new UploadImpl();
        uploadImpl.uploadWiserRequestsToesMD("dev");
    }
}
