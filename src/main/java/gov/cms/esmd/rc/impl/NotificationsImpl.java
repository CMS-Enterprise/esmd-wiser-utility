package gov.cms.esmd.rc.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.cms.esmd.bean.adminerror.Notification;
import gov.cms.esmd.bean.auth.response.AuthResponse;
import gov.cms.esmd.bean.response.*;
import gov.cms.esmd.rc.api.client.AuthApiClient;
import gov.cms.esmd.rc.api.client.NotificationApiClient;
import gov.cms.esmd.utility.NotificationUtility;
import gov.cms.esmd.utility.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * NotificationsImpl handles the complete workflow for sending notification to esMD.
 * This class orchestrates the authentication and send notification message to esMD.
 *
 * @author esMD Team
 * @version 1.0
 * @since 1.0
 */
public class NotificationsImpl {



    private static final Logger logger = LoggerFactory.getLogger(gov.cms.esmd.rc.impl.DownloadImpl.class);
    private final Properties apiProperties;
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXX";
    /**
     * Constructs a new NotificationsImpl instance.
     * Loads the API properties from the YAML configuration file.
     */
    public NotificationsImpl() {
        this.apiProperties = PropertiesUtils.loadProperties();
        logger.info("StatusImpl initialized successfully");
    }

    /**
     * Sends notifications to esMD for the specified environment.
     * This method performs the complete workflow:
     * 1. Gets authentication token using the download scope
     * 2. Retrieves notification statuses available for Wiser Requests and Responses
     *
     * @param environment the target environment (dev, val, uat, prod)
     * @return List of StatusDetail objects containing download status for each zip file
     * @throws Exception if any step in the download process fails
     */
    public NotificationResponse sendNotificationToESMD(
            String environment, String jsonMessage, String notificationType) throws Exception {
        logger.info("Starting Notification process for environment: {}", environment);

        // Initialize status details list
        NotificationResponse notificationResponse = null;

        // Read configuration from YAML properties
        String scope = getRequiredProperty("api.scope.status");
        String mailboxId = apiProperties.getProperty("userinfo.mailboxid");

        // Validate environment parameter
        if (environment == null || environment.trim().isEmpty()) {
            throw new IllegalArgumentException("Environment cannot be null or empty");
        }

        try {
            AuthApiClient authClient = new AuthApiClient(environment);

            // Step 1: Get authentication token
            logger.info("Step 1: Getting authentication token for scope: {}", scope);
            AuthResponse authResponse = authClient.getToken(scope);

            if (authResponse.getError() != null && !authResponse.getError().isEmpty()) {
                throw new IllegalStateException("Authentication failed: " + authResponse.getError());
            }

            String token = authResponse.getAccess_token();
            logger.info("Authentication successful, token acquired");

            // Step 2: Build Notification URL
            logger.info("Step 2: Build Notification URL : {}", notificationType);
            String notificationURL = buildNotificationURL(environment);
            NotificationApiClient notificationApiClient = new NotificationApiClient(notificationURL);

            // Step 3: Submit Notification to esMD
            logger.info("Step 3: Submit Notification to esMD : {}", notificationType);
            notificationResponse =  notificationApiClient.submitNotification(
                    token,jsonMessage,notificationType,notificationURL);

            if (notificationResponse.getStatusDetails() == null
                    || notificationResponse.getStatusDetails().isEmpty()) {
                logger.info("No new statues available for {}" ,mailboxId);
                return notificationResponse;
            }
            logger.info("Notification process completed for mailboxId: {}", mailboxId);

        } catch (Exception e) {
            logger.error("Notification process failed for mailboxId: {}", mailboxId, e);
            throw e;
        }

        return notificationResponse;
    }

    public String createPickupNotification(String notificationType, String senderRoutingID,
                                           String esMDTransactionId, String filename) throws ParseException {
        String jsonString = null;
        List<Notification> notificationList = new ArrayList<Notification>();
        PickupNotification pickupNotification = new PickupNotification();
        pickupNotification.setNotificationType(notificationType);
        pickupNotification.setSenderRoutingId(senderRoutingID);
        Notification notification = new Notification();

        String formattedDate = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        Date parsedDate = new SimpleDateFormat(DATE_FORMAT).parse(formattedDate);

        notification.setPickupTime(formattedDate);
        notification.setSubmissionTime(formattedDate);
        notification.setFilename(filename);
        notification.setEsMDTransactionId(esMDTransactionId);
        notification.setStatus("SUCCESS");
        notification.setErrorMessages(new ArrayList<>());
        notificationList.add(notification);
        pickupNotification.setNotification(notificationList);
        jsonString = new Gson().toJson(pickupNotification);
        logger.info("createJsonPickupNotification,jsonString: " + jsonString);
        return jsonString;
    }


    public String buildNotificationURL(String env) {
        String baseUrl = apiProperties.getProperty("api.environment." + env);
        String endpoint = apiProperties.getProperty("api.url.pickup-notification");

        if (baseUrl == null || endpoint == null) {
            throw new IllegalStateException("Missing required configuration for environment: " + env);
        }

        return baseUrl + endpoint;
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
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static void main(String[] args) throws Exception {
        NotificationsImpl notificationsImpl = new NotificationsImpl();
        String pickupJson = "{\n" +
                "      \"notificationType\" : \"PICKUP\",\n" +
                "      \"senderRoutingId\" : \"PT9993\",\n" +
                "      \"notification\" : [ {\n" +
                "            \"esMDTransactionId\": \"GNY0007249530EC\",\n" +
                "            \"contenttypecd\": \"\",\n" +
                "            \"pickupTime\": \"2025-04-09T11:21:10.7929233-04:00\",\n" +
                "            \"submissionTime\": \"2025-04-09T11:21:10.7929233-04:00\",\n" +
                "            \"creationTime\": \"0001-01-01T00:00:00.0000000-05:00\",\n" +
                "            \"filename\": \"PT9993.D.L1.EGNY0007249530EC.ESMD2.D050725.T0920480.zip\",\n" +
                "        \"status\" : \"SUCCESS\",\n" +
                "        \"errorMessages\" : [ ]\n" +
                "      } ]\n" +
                "    }";
        NotificationResponse notificationResponse =  notificationsImpl.sendNotificationToESMD("dev",pickupJson,"PICKUP");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(notificationResponse));
    }
}
