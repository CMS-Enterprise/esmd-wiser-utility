package gov.cms.esmd.rc.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.cms.esmd.bean.ErrorMessage;
import gov.cms.esmd.bean.auth.response.AuthResponse;
import gov.cms.esmd.bean.response.*;
import gov.cms.esmd.rc.api.client.AuthApiClient;
import gov.cms.esmd.rc.api.client.StatusApiClient;
import gov.cms.esmd.utility.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
/**
 * StatusImpl handles the complete workflow for retrieving Wiser statuses from esMD.
 * This class orchestrates the authentication and retrieving the Wiser statuses.
 *
 * @author esMD Team
 * @version 1.0
 * @since 1.0
 */
public class StatusImpl {



        private static final Logger logger = LoggerFactory.getLogger(gov.cms.esmd.rc.impl.DownloadImpl.class);
        private final Properties apiProperties;

        /**
         * Constructs a new StatusImpl instance.
         * Loads the API properties from the YAML configuration file.
         */
        public StatusImpl() {
            this.apiProperties = PropertiesUtils.loadProperties();
            logger.info("StatusImpl initialized successfully");
        }

        /**
         * Retrieves Wiser notification statues from esMD for the specified environment.
         * This method performs the complete workflow:
         * 1. Gets authentication token using the download scope
         * 2. Retrieves notification statuses available for Wiser Requests and Responses
         *
         * @param environment the target environment (dev, val, uat, prod)
         * @return List of StatusDetail objects containing download status for each zip file
         * @throws Exception if any step in the download process fails
         */
        public NotificationResponse getStatusFromesMD(String environment, String esMDTransactionId) throws Exception {
            logger.info("Starting status process for environment: {}", environment);

            // Initialize status details list
            NotificationResponse notificationResponse = null;

            // Validate environment parameter
            if (environment == null || environment.trim().isEmpty()) {
                throw new IllegalArgumentException("Environment cannot be null or empty");
            }

            // Read configuration from YAML properties
            String scope = getRequiredProperty("api.scope.status");
            String clientId = getRequiredProperty("userinfo.clientid");

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

                // Step 2: Get Notification Statues
                logger.info("Step 2: Retrieving notification statues: {}", clientId);
                String statusURL = buildStatusURL(environment);
                StatusApiClient statusApiClient = new StatusApiClient(statusURL);
                String mailboxId = apiProperties.getProperty("userinfo.mailboxid");

                notificationResponse =  statusApiClient.retrieveLatestStatus(
                        esMDTransactionId,mailboxId,token,statusURL);


                if (notificationResponse.getStatusDetails() == null
                        || notificationResponse.getStatusDetails().isEmpty()) {
                    logger.info("No new statues available.");
                    return notificationResponse;
                }
                logger.info("Status process completed for environment: {}", environment);

            } catch (Exception e) {
                logger.error("Status process failed for environment: {}", environment, e);
                throw e;
            }

            return notificationResponse;
        }

    private String buildStatusURL(String env) {
        String baseUrl = apiProperties.getProperty("api.environment." + env);
        String endpoint = apiProperties.getProperty("api.url.notification-status");

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
            StatusImpl statusImpl = new StatusImpl();
            NotificationResponse response = statusImpl.getStatusFromesMD("dev", "VFQ0007277431EC"); //RUH0007275137EC
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println(gson.toJson(response));
        }
    }
