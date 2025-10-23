package gov.cms.esmd.rc.impl;

import gov.cms.esmd.bean.ErrorMessage;
import gov.cms.esmd.bean.auth.response.AuthResponse;
import gov.cms.esmd.bean.response.*;
import gov.cms.esmd.rc.api.client.AuthApiClient;
import gov.cms.esmd.rc.api.client.DownloadApiClient;
import gov.cms.esmd.rc.api.client.NotificationApiClient;
import gov.cms.esmd.utility.PropertiesUtils;
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
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * DownloadImpl handles the complete workflow for downloading Wiser requests from esMD.
 * This class orchestrates the authentication, file listing, and downloading process.
 *
 * @author esMD Team
 * @version 1.0
 * @since 1.0
 */
public class DownloadImpl {

    private static final Logger logger = LoggerFactory.getLogger(DownloadImpl.class);
    
    private final Properties apiProperties;

    /**
     * Constructs a new DownloadImpl instance.
     * Loads the API properties from the YAML configuration file.
     */
    public DownloadImpl() {
        this.apiProperties = PropertiesUtils.loadProperties();
        logger.info("DownloadImpl initialized successfully");
    }

    /**
     * Downloads Wiser requests from esMD for the specified environment.
     * This method performs the complete workflow:
     * 1. Gets authentication token using the download scope
     * 2. Retrieves list of files available for download
     * 3. For each file, gets a presigned URL and downloads it
     * 4. Extracts zip files to the local file path
     *
     * @param environment the target environment (dev, val, uat, prod)
     * @return List of StatusDetail objects containing download status for each zip file
     * @throws Exception if any step in the download process fails
     */
    public List<StatusDetail> downloadWiserRequestsFromesMD(String environment) throws Exception {
        logger.info("Starting download process for environment: {}", environment);
        
        // Initialize status details list
        List<StatusDetail> statusDetails = new ArrayList<>();
        
        // Validate environment parameter
        if (environment == null || environment.trim().isEmpty()) {
            throw new IllegalArgumentException("Environment cannot be null or empty");
        }

        // Read configuration from YAML properties
        String scope = getRequiredProperty("api.scope.download");
        String clientId = getRequiredProperty("userinfo.clientid");
        String localFilePath = getRequiredProperty("api.file-download.local-path");
        
        logger.debug("Configuration loaded - scope: {}, clientId: {}, localPath: {}", 
                    scope, clientId, localFilePath);

        try (AuthApiClient authClient = new AuthApiClient(environment);
             DownloadApiClient downloadClient = new DownloadApiClient(environment)) {

            // Step 1: Get authentication token
            logger.info("Step 1: Getting authentication token for scope: {}", scope);
            AuthResponse authResponse = authClient.getToken(scope);
            
            if (authResponse.getError() != null && !authResponse.getError().isEmpty()) {
                throw new IllegalStateException("Authentication failed: " + authResponse.getError());
            }
            
            String token = authResponse.getAccess_token();
            logger.info("Authentication successful, token acquired");

            // Step 2: Get list of files
            logger.info("Step 2: Retrieving list of files for clientId: {}", clientId);
            DownloadResponse downloadResponse = downloadClient.getListOfFiles(token, clientId);
            
            if (downloadResponse.getObjects() == null || downloadResponse.getObjects().isEmpty()) {
                logger.info("No files available for download");
                return statusDetails;
            }
            
            List<DownloadObject> files = downloadResponse.getObjects();
            logger.info("Found {} files available for download", files.size());

            // Step 3 & 4: Process each file
            for (DownloadObject file : files) {
                StatusDetail statusDetail = processFileDownload(
                        file, downloadClient, token, clientId, localFilePath, environment);
                statusDetails.add(statusDetail);
            }
            
            logger.info("Download process completed for environment: {}", environment);
            
        } catch (Exception e) {
            logger.error("Download process failed for environment: {}", environment, e);
            throw e;
        }
        
        return statusDetails;
    }

    /**
     * Processes the download of a single file.
     * Gets presigned URL, downloads the file to local storage, and extracts if it's a zip file.
     *
     * @param file the file object containing file information
     * @param downloadClient the download API client
     * @param token the authentication token
     * @param clientId the client identifier
     * @param localFilePath the base local file path
     * @return StatusDetail object containing the download status
     */
    private StatusDetail processFileDownload(DownloadObject file, DownloadApiClient downloadClient, 
                                   String token, String clientId, String localFilePath, String environment) {
        
        String filename = file.getFilename();
        logger.info("Processing file: {}", filename);
        
        // Create StatusDetail object for this file
        StatusDetail statusDetail = new StatusDetail(new ArrayList<>());
        statusDetail.setContenttypecd("ZIP"); // Assuming all files are zip files
        statusDetail.setDeliveryType("DOWNLOAD");
        statusDetail.setStatus("SUCCESS");
        statusDetail.setStatusDescription("File processing initiated");

        try {
            // Step 3: Get presigned URL for the file
            logger.debug("Getting presigned URL for file: {}", filename);
            PresignedUrlResponse presignedUrlResponse = downloadClient.getDownloadPresignedURL(filename, token, clientId);
            
            if (presignedUrlResponse.getContents() == null || presignedUrlResponse.getContents().isEmpty()) {
                logger.warn("No presigned URL content received for file: {}", filename);
                statusDetail.setStatus("FAILED");
                statusDetail.setStatusDescription("No presigned URL content received");
                statusDetail.getErrorMessages().add(new ErrorMessage("NO_PRESIGNED_URL", "Missing URL", "No presigned URL content received for file: " + filename));
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

            // Step 4: Download the file using presigned URL
            String fullLocalPath = localFilePath + "\\" + filename;
            logger.info("Downloading file to: {}", fullLocalPath);
            
            boolean downloadSuccess = downloadClient.downloadFileWithPresignedURL(presignedUrl, fullLocalPath, token);
            
            if (!downloadSuccess) {
                logger.error("Failed to download file: {}", filename);
                statusDetail.setStatus("FAILED");
                statusDetail.setStatusDescription("Download failed");
                statusDetail.getErrorMessages().add(new ErrorMessage("DOWNLOAD_FAILED", "Download Error", "Failed to download file: " + filename));
                return statusDetail;
            }
            
            logger.info("Successfully downloaded file: {} to: {}", filename, fullLocalPath);
            statusDetail.setStatusDescription("File downloaded successfully");

            // Extract esMD transaction ID from zip file name before extraction
            String esmdTransactionId = extractEsMDTransactionId(filename);
            if (esmdTransactionId != null && !esmdTransactionId.isEmpty()) {
                statusDetail.setEsMDTransactionID(esmdTransactionId);
                logger.info("Extracted esMD transaction ID: {} from filename: {}", esmdTransactionId, filename);
            } else {
                logger.warn("Could not extract esMD transaction ID from filename: {}", filename);
                statusDetail.getErrorMessages().add(new ErrorMessage("INVALID_FILENAME", "Invalid Filename", "Could not extract esMD transaction ID from filename: " + filename));
            }

            // Step 5: Extract zip file if it's a zip file
            if (filename.toLowerCase().endsWith(".zip")) {
                logger.info("Extracting zip file: {}", filename);
                boolean extractSuccess = extractZipFile(fullLocalPath, localFilePath);
                
                if (extractSuccess) {
                    logger.info("Successfully extracted zip file: {}", filename);
                    statusDetail.setStatusDescription("File downloaded and extracted successfully");
                    String notificationType = getRequiredProperty("api.notification.pickup");
                    String senderRoutingId = getRequiredProperty("userinfo.mailboxid");
                    NotificationsImpl notifications = new NotificationsImpl();
                    // create Pickup Notification
                    String pickupNotification = notifications.createPickupNotification(
                            notificationType, senderRoutingId, esmdTransactionId, filename);

                    // Submit Pickup Notification
                    notifications.sendNotificationToESMD(environment,pickupNotification, notificationType);

                } else {
                    logger.warn("Failed to extract zip file: {}", filename);
                    statusDetail.setStatus("PARTIAL_SUCCESS");
                    statusDetail.setStatusDescription("File downloaded but extraction failed");
                    statusDetail.getErrorMessages().add(new ErrorMessage(
                            "EXTRACTION_FAILED", "Extraction Error",
                            "Failed to extract zip file: " + filename));
                }
            } else {
                logger.info("File is not a zip file, skipping extraction: {}", filename);
                statusDetail.setStatusDescription("File downloaded successfully (not a zip file)");
            }
            
        } catch (Exception e) {
            logger.error("Error processing file: {}", filename, e);
            statusDetail.setStatus("FAILED");
            statusDetail.setStatusDescription("Error processing file: " + e.getMessage());
            statusDetail.getErrorMessages().add(new ErrorMessage("PROCESSING_ERROR", "Processing Error", "Error processing file " + filename + ": " + e.getMessage()));
        }
        
        return statusDetail;
    }

    /**
     * Extracts the esMD transaction ID from a zip file name.
     * Expected format: ES9999.D.L1.EZKW0007260517EC.ESMD2.D071425.T2219020.zip
     * The transaction ID is the part between the 4th and 5th dots: ZKW0007260517EC
     *
     * @param filename the zip file name
     * @return the esMD transaction ID if found, null otherwise
     */
    private String extractEsMDTransactionId(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            logger.warn("Filename is null or empty, cannot extract transaction ID");
            return null;
        }

        try {
            // Remove .zip extension if present
            String nameWithoutExt = filename;
            if (filename.toLowerCase().endsWith(".zip")) {
                nameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));
            }

            // Split by dots to get the parts
            String[] parts = nameWithoutExt.split("\\.");
            
            // Expected format: ES9999.D.L1.EZKW0007260517EC.ESMD2.D071425.T2219020
            // We need the 4th part (index 3) which should contain the transaction ID
            if (parts.length >= 4) {
                String transactionIdPart = parts[3];
                
                // Remove the "E" prefix if present (e.g., EZKW0007260517EC -> ZKW0007260517EC)
                if (transactionIdPart.startsWith("E") && transactionIdPart.length() > 1) {
                    String transactionId = transactionIdPart.substring(1);
                    logger.debug("Extracted transaction ID: {} from part: {}", transactionId, transactionIdPart);
                    return transactionId;
                } else {
                    // If no "E" prefix, use the part as is
                    logger.debug("Using transaction ID as is: {}", transactionIdPart);
                    return transactionIdPart;
                }
            } else {
                logger.warn("Filename does not match expected format. Parts found: {}, filename: {}", parts.length, filename);
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Error extracting transaction ID from filename: {}", filename, e);
            return null;
        }
    }

    /**
     * Extracts a zip file to the specified directory.
     *
     * @param zipFilePath the path to the zip file
     * @param extractToPath the directory to extract files to
     * @return true if extraction was successful, false otherwise
     */
    private boolean extractZipFile(String zipFilePath, String extractToPath) {
        try {
            File zipFile = new File(zipFilePath);
            if (!zipFile.exists()) {
                logger.error("Zip file does not exist: {}", zipFilePath);
                return false;
            }

            // Create extraction directory
            Path extractDir = Paths.get(extractToPath);
            Files.createDirectories(extractDir);

            try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    Path entryPath = extractDir.resolve(entryName);
                    
                    // Security check to prevent zip slip attacks
                    if (!entryPath.normalize().startsWith(extractDir.normalize())) {
                        logger.warn("Skipping potentially malicious zip entry: {}", entryName);
                        continue;
                    }
                    
                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                        logger.debug("Created directory: {}", entryPath);
                    } else {
                        // Create parent directories if they don't exist
                        Files.createDirectories(entryPath.getParent());
                        
                        // Extract the file
                        try (FileOutputStream fos = new FileOutputStream(entryPath.toFile())) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = zipInputStream.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                        }
                        logger.debug("Extracted file: {}", entryPath);
                    }
                    zipInputStream.closeEntry();
                }
            }
            
            logger.info("Successfully extracted zip file: {} to: {}", zipFilePath, extractToPath);
            return true;
            
        } catch (IOException e) {
            logger.error("Failed to extract zip file: {} to: {}", zipFilePath, extractToPath, e);
            return false;
        }
    }

    /**
     * Gets a required property from the configuration.
     * Throws an exception if the property is not found or empty.
     *
     * @param key the property key
     * @return the property value
     * @throws IllegalStateException if the property is not found or empty
     */
    private String getRequiredProperty(String key) {
        String value = apiProperties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Required property not found or empty: " + key);
        }
        return value.trim();
    }


    public static void main(String[] args) throws Exception {
        DownloadImpl downloadImpl = new DownloadImpl();
        downloadImpl.downloadWiserRequestsFromesMD("dev");
        }
    }
