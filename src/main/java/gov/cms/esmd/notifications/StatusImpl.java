package gov.cms.esmd.notifications;

import gov.cms.esmd.bean.auth.AuthInfoBean;
import gov.cms.esmd.bean.auth.response.AuthResponse;
import gov.cms.esmd.bean.response.NotificationResponse;
import gov.cms.esmd.rc.api.client.AuthApiClient;
import gov.cms.esmd.rc.api.client.StatusApiClient;
import gov.cms.esmd.utility.PropertiesUtils;
import gov.cms.esmd.utility.ValidatorUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class StatusImpl {
    private static final Logger log = LoggerFactory.getLogger(StatusImpl.class);

    // Retrieves the latest status by transaction ID
    public NotificationResponse retrieveLatestStatusByTransactionId(String esMDTransactionId, AuthInfoBean authInfo, String environment) {
        try {
            log.info("Start StatusImpl:retrieveLatestStatusByTransactionId(esMDTransactionId, authInfo, environment)");

            // Initialize Properties and ValidatorUtility
            Properties properties = PropertiesUtils.loadProperties();
            ValidatorUtility validatorUtility = new ValidatorUtility();
            // Validate authInfo metadata contains userid, password, clientid, clientsecret
            NotificationResponse notificationResponse = validatorUtility.validateAuthInfo(authInfo);
            if (notificationResponse != null) {
                return notificationResponse;
            }

            // Get required properties from the configuration file
            String scope = properties.getProperty("api.scope.status");
            String mailboxid = properties.getProperty("userinfo.mailboxid");
            String baseUrl = (environment != null && environment.contains("PROD"))
                    ? properties.getProperty("api.environment.prod") : properties.getProperty("api.environment.uat");
            String url = properties.getProperty("api.url.notification-status");
            url = baseUrl + url;
            //String authUrl = properties.getProperty("api.url.auth");
            //authUrl = baseUrl + authUrl;

            // Get authentication token
            AuthApiClient authApiClient = new AuthApiClient(environment);
            AuthResponse authResponse = authApiClient.getToken(scope);

            if (authResponse == null) {
                return validatorUtility.generateResponseForNull();
            } else if (authResponse.getAccess_token() == null && authResponse.getError() != null) {
                return validatorUtility.generateResponseWithErrorMsg(authResponse.getError());
            }

            String token = authResponse.getAccess_token();

            // Retrieve status using the StatusApiClient
            StatusApiClient statusApiClient = new StatusApiClient(url);
            notificationResponse = statusApiClient.retrieveLatestStatusByTransactionId(
                    esMDTransactionId, mailboxid, token, url);

            log.info("End StatusImpl:retrieveLatestStatusByTransactionId(esMDTransactionId, authInfo, environment)");

            return notificationResponse;
        } catch (Exception ex) {
            // Log any exceptions that occur during status retrieval
            log.error("An error occurred while retrieving status: ", ex);
            throw new IllegalStateException("An error occurred while retrieving status.", ex);
        }
    }
}

