package gov.cms.esmd.utility;

import gov.cms.esmd.bean.adminerror.AdminErrorNotificationRoot;
import gov.cms.esmd.bean.adminerror.Notification;
import gov.cms.esmd.bean.auth.AuthInfoBean;
import gov.cms.esmd.bean.parejectjson.PARejectResponseRoot;
import gov.cms.esmd.bean.parejectjson.Rejectreasoncode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.cms.esmd.bean.response.NotificationResponse;
import gov.cms.esmd.bean.ErrorMessage;
import gov.cms.esmd.bean.response.StatusDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ValidatorUtility {

    private static final Logger log = LoggerFactory.getLogger(ValidatorUtility.class);


    // Validates the AuthInfo object for required fields
    public NotificationResponse validateAuthInfo(AuthInfoBean authInfo) {
        log.info("Starting validateAuthInfo method.");

        if (authInfo == null) {
            log.warn("AuthInfo object is null.");
            return generateResponseForNull();
        }

        List<ErrorMessage> errorMessages = new ArrayList<>();

        addErrorMessageIfNull(authInfo.getClientkey(), "CLIENTKEY_REQUIRED", "Client Key is required in authInfo. Please correct and resubmit.", errorMessages);
        addErrorMessageIfNull(authInfo.getClientsecret(), "CLIENTSECRET_REQUIRED", "Client Secret is required in authInfo. Please correct and resubmit.", errorMessages);

        log.info("Finished validateAuthInfo method.");
        return errorMessages.size() > 0 ? generateResponse(errorMessages) : null;
    }

    private void addErrorMessageIfNull(String value, String errorCode, String errorMessage, List<ErrorMessage> errorMessages) {
        if (value == null) {
            log.warn("Validation failed: " + errorMessage);
            errorMessages.add(new ErrorMessage(errorCode, errorMessage, errorMessage));
        }
    }

    public NotificationResponse validateAuthInfoFromProperties(Properties properties) {
        log.info("Starting validateAuthInfoFromProperties method.");
        List<ErrorMessage> errorMessageList = new ArrayList<>();

        String[] requiredProperties = {
                "credentials.username:USERNAME_REQUIRED:user name is required, either pass as a parameter or integrate in yaml file",
                "credentials.password:PASSWORD_REQUIRED:Password is required, either pass as a parameter or integrate in yaml file",
                "credentials.access-key:CLIENTID_REQUIRED:Client id is required, either pass as a parameter or integrate in yaml file",
                "credentials.secret-key:SECRETKEY_REQUIRED:Secret Key is required, either pass as a parameter or integrate in yaml file",
                "api.scope.auth:SCOPE_REQUIRED:API Scope is required, either pass as a parameter or integrate in yaml file",
                "credentials.mailboxid:MAILBOX_REQUIRED:MAIL BOX is required, either pass as a parameter or integrate in yaml file"
        };

        for (String property : requiredProperties) {
            String[] parts = property.split(":");
            String propName = parts[0];
            String errorCode = parts[1];
            String errorMessage = parts[2];

            if (properties.getProperty(propName) == null) {
                log.warn("Validation failed: " + errorMessage);
                errorMessageList.add(new ErrorMessage(errorCode, errorMessage, errorMessage));
            }
        }

        String[] urlProperties = {
                "api.url.admin-error-notification",
                "api.url.auth",
                "api.url.pa-reject-notification",
                "api.environment.prod",
                "api.environment.uat"
        };

        boolean hasNullUrl = false;
        for (String urlProperty : urlProperties) {
            if (properties.getProperty(urlProperty) == null) {
                hasNullUrl = true;
                break;
            }
        }

        if (hasNullUrl) {
            log.warn("Validation failed: API url is required.");
            errorMessageList.add(new ErrorMessage("APIURL_REQUIRED", "API url is required", "API url is required"));
        }

        log.info("Finished validateAuthInfoFromProperties method.");
        return errorMessageList.size() > 0 ? generateResponse(errorMessageList) : null;
    }

    private ErrorMessage generateErrorMessage(String code, String message, String description) {
        log.debug("Generating error message: " + code + ", " + message);
        return new ErrorMessage(code, message, description);
    }

    private ErrorMessage generateErrorMessage(String code, Properties properties) {
        log.debug("Generating error message for code: " + code);
        String errorMessage = properties.getProperty("errorCodes." + code);
        return new ErrorMessage(code, errorMessage, errorMessage);
    }

    public NotificationResponse generateResponseForNull() {
        log.info("Starting generateResponseForNull method.");
        try {
            ErrorMessage errorMessage = generateErrorMessage(
                    "AUTH_INFO_EMPTY",
                    "Username, Password, Clientid and Clientsecret are required. Please correct and resubmit.",
                    "Username, Password, Clientid and Clientsecret are required. Please correct and resubmit."
            );

            List<ErrorMessage> errorMessagesList = new ArrayList<>();
            errorMessagesList.add(errorMessage);

            NotificationResponse response = new NotificationResponse();
            response.setStatusDetails(new ArrayList<>());
            response.getStatusDetails().add(new StatusDetail(errorMessagesList));

            log.info("Finished generateResponseForNull method.");
            return response;
        } catch (Exception ex) {
            log.error("An error occurred while generating the notification response.", ex);
            throw ex;
        }
    }

    public NotificationResponse generateResponseWithErrorMsg(String errorMsg) {
        log.info("Starting generateResponseWithErrorMsg method.");
        ErrorMessage errorMessage = generateErrorMessage("AUTH_ERROR", errorMsg, errorMsg);
        List<ErrorMessage> errorMessagesList = new ArrayList<>();
        errorMessagesList.add(errorMessage);

        NotificationResponse response = new NotificationResponse();
        response.setStatusDetails(new ArrayList<>());
        response.getStatusDetails().add(new StatusDetail(errorMessagesList));

        log.info("Finished generateResponseWithErrorMsg method.");
        return response;
    }

    public NotificationResponse generateResponse(List<ErrorMessage> errorMessageList) {
        log.info("Starting generateResponse method.");
        NotificationResponse responseRoot = new NotificationResponse();
        responseRoot.setStatusDetails(new ArrayList<>());
        responseRoot.getStatusDetails().add(new StatusDetail(errorMessageList));
        log.info("Finished generateResponse method.");
        return responseRoot;
    }

    public NotificationResponse validatePARejectResponse(PARejectResponseRoot paRejectResponseRoot, Properties properties) {
        log.info("Starting validatePARejectResponse method.");
        List<ErrorMessage> errorMessageList = new ArrayList<>();

        if (paRejectResponseRoot == null) {
            log.warn("PARejectResponseRoot is null.");
            errorMessageList.add(generateErrorMessage("EMPTY_PAREJECT_RESPONSE", properties));
        } else {
            if (paRejectResponseRoot.getNotificationType() == null || paRejectResponseRoot.getNotificationType().isEmpty()) {
                log.warn("Notification type is missing.");
                errorMessageList.add(generateErrorMessage("NOTIFICATION_TYPE_MISSING_ERR_CD", properties));
            }
            if (paRejectResponseRoot.getEsmdtransactionid() == null || paRejectResponseRoot.getEsmdtransactionid().isEmpty()) {
                log.warn("esmdTransactionId is missing.");
                errorMessageList.add(generateErrorMessage("ESMD_TRANS_ID_MISSING_ERR_CD", properties));
            }
            if (paRejectResponseRoot.getSenderRoutingId() == null || paRejectResponseRoot.getSenderRoutingId().isEmpty()) {
                log.warn("SenderRoutingId is missing.");
                errorMessageList.add(generateErrorMessage("SENDER_ROUTING_ID_MISSING_ERR_CD", properties));
            }
            if (paRejectResponseRoot.getRequester() != null && paRejectResponseRoot.getRequester().getRejectreasoncodes() != null) {
                for (Rejectreasoncode rejectReasonCodeObj : paRejectResponseRoot.getRequester().getRejectreasoncodes()) {
                    if (rejectReasonCodeObj != null && (rejectReasonCodeObj.getRejectreasoncode() == null || rejectReasonCodeObj.getRejectreasoncode().isEmpty() || rejectReasonCodeObj.getRejectreason() == null || rejectReasonCodeObj.getRejectreason().isEmpty())) {
                        if (rejectReasonCodeObj.getRejectreasoncode() == null || rejectReasonCodeObj.getRejectreasoncode().isEmpty()) {
                            log.warn("Requester reject reason code is missing.");
                            errorMessageList.add(generateErrorMessage("REQTR_REASON_CD", properties));
                        }
                        if (rejectReasonCodeObj.getRejectreason() == null || rejectReasonCodeObj.getRejectreason().isEmpty()) {
                            log.warn("Requester reject reason is missing.");
                            errorMessageList.add(generateErrorMessage("REQTR_REASON", properties));
                        }
                    }
                }
            }
            if (paRejectResponseRoot.getBeneficiary() != null && paRejectResponseRoot.getBeneficiary().getRejectreasoncodes() != null) {
                for (Rejectreasoncode rejectReasonCodeObj : paRejectResponseRoot.getBeneficiary().getRejectreasoncodes()) {
                    if (rejectReasonCodeObj != null && (rejectReasonCodeObj.getRejectreasoncode() == null || rejectReasonCodeObj.getRejectreasoncode().isEmpty() || rejectReasonCodeObj.getRejectreason() == null || rejectReasonCodeObj.getRejectreason().isEmpty())) {
                        if (rejectReasonCodeObj.getRejectreasoncode() == null || rejectReasonCodeObj.getRejectreasoncode().isEmpty()) {
                            log.warn("Beneficiary reject reason code is missing.");
                            errorMessageList.add(generateErrorMessage("BENEFICIARY_REASON_CD", properties));
                        }
                        if (rejectReasonCodeObj.getRejectreason() == null || rejectReasonCodeObj.getRejectreason().isEmpty()) {
                            log.warn("Beneficiary reject reason is missing.");
                            errorMessageList.add(generateErrorMessage("BENEFICIARY_REASON", properties));
                        }
                    }
                }
            }
            if (paRejectResponseRoot.getPatientevent() != null && paRejectResponseRoot.getPatientevent().getRejectreasoncodes() != null) {
                for (Rejectreasoncode rejectReasonCode : paRejectResponseRoot.getPatientevent().getRejectreasoncodes()) {
                    if (rejectReasonCode != null && (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty() || rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty())) {
                        if (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty()) {
                            log.warn("Patient event reject reason code is missing.");
                            errorMessageList.add(generateErrorMessage("PATIENT_EVENT_REASON_CD", properties));
                        }
                        if (rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty()) {
                            log.warn("Patient event reject reason is missing.");
                            errorMessageList.add(generateErrorMessage("PATIENT_EVENT_REASON", properties));
                        }
                    }
                }
            }
            if (paRejectResponseRoot.getFacilityProvider() != null && paRejectResponseRoot.getFacilityProvider().getRejectreasoncodes() != null) {
                if (paRejectResponseRoot.getFacilityProvider().getQualifier() != null && !paRejectResponseRoot.getFacilityProvider().getQualifier().equals("FA")) {
                    log.warn("Facility provider qualifier is incorrect.");
                    errorMessageList.add(generateErrorMessage("FACILITY_PROVIDER_QUALIFIER_CD", properties));
                }
                for (Rejectreasoncode rejectReasonCode : paRejectResponseRoot.getFacilityProvider().getRejectreasoncodes()) {
                    if (rejectReasonCode != null && (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty() || rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty())) {
                        if (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty()) {
                            log.warn("Facility provider reject reason code is missing.");
                            errorMessageList.add(generateErrorMessage("FACILITY_PROVIDER_REASON_CD", properties));
                        }
                        if (rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty()) {
                            log.warn("Facility provider reject reason is missing.");
                            errorMessageList.add(generateErrorMessage("FACILITY_PROVIDER_REASON", properties));
                        }
                    }
                }
            }
            if (paRejectResponseRoot.getOrderingProvider() != null && paRejectResponseRoot.getOrderingProvider().getRejectreasoncodes() != null) {
                if (paRejectResponseRoot.getOrderingProvider().getQualifier() != null && !paRejectResponseRoot.getOrderingProvider().getQualifier().equals("DK")) {
                    log.warn("Ordering provider qualifier is incorrect.");
                    errorMessageList.add(generateErrorMessage("ORDERING_PROVIDER_QUALIFIER_CD", properties));
                }
                for (Rejectreasoncode rejectReasonCode : paRejectResponseRoot.getOrderingProvider().getRejectreasoncodes()) {
                    if (rejectReasonCode != null && (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty() || rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty())) {
                        if (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty()) {
                            log.warn("Ordering provider reject reason code is missing.");
                            errorMessageList.add(generateErrorMessage("ORDERING_PROVIDER_REASON_CD", properties));
                        }
                        if (rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty()) {
                            log.warn("Ordering provider reject reason is missing.");
                            errorMessageList.add(generateErrorMessage("ORDERING_PROVIDER_REASON", properties));
                        }
                    }
                }
            }
            if (paRejectResponseRoot.getRenderingOrSupplierProvider() != null && paRejectResponseRoot.getRenderingOrSupplierProvider().getRejectreasoncodes() != null) {
                if (paRejectResponseRoot.getRenderingOrSupplierProvider().getQualifier() != null && !paRejectResponseRoot.getRenderingOrSupplierProvider().getQualifier().equals("SJ")) {
                    log.warn("Rendering provider qualifier is incorrect.");
                    errorMessageList.add(generateErrorMessage("RENDERING_PROVIDER_QUALIFIER_CD", properties));
                }
                for (Rejectreasoncode rejectReasonCode : paRejectResponseRoot.getRenderingOrSupplierProvider().getRejectreasoncodes()) {
                    if (rejectReasonCode != null && (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty() || rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty())) {
                        if (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty()) {
                            log.warn("Rendering provider reject reason code is missing.");
                            errorMessageList.add(generateErrorMessage("RENDERING_PROVIDER_REASON_CD", properties));
                        }
                        if (rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty()) {
                            log.warn("Rendering provider reject reason is missing.");
                            errorMessageList.add(generateErrorMessage("RENDERING_PROVIDER_REASON", properties));
                        }
                    }
                }
            }
            if (paRejectResponseRoot.getReferringProvider() != null && paRejectResponseRoot.getReferringProvider().getRejectreasoncodes() != null) {
                if (paRejectResponseRoot.getReferringProvider().getQualifier() != null && !paRejectResponseRoot.getReferringProvider().getQualifier().equals("DN")) {
                    log.warn("Referring provider qualifier is incorrect.");
                    errorMessageList.add(generateErrorMessage("REFERRING_PROVIDER_QUALIFIER_CD", properties));
                }
                for (Rejectreasoncode rejectReasonCode : paRejectResponseRoot.getReferringProvider().getRejectreasoncodes()) {
                    if (rejectReasonCode != null && (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty() || rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty())) {
                        if (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty()) {
                            log.warn("Referring provider reject reason code is missing.");
                            errorMessageList.add(generateErrorMessage("REFERRING_PROVIDER_REASON_CD", properties));
                        }
                        if (rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty()) {
                            log.warn("Referring provider reject reason is missing.");
                            errorMessageList.add(generateErrorMessage("REFERRING_PROVIDER_REASON", properties));
                        }
                    }
                }
            }
            if (paRejectResponseRoot.getOperatingProvider() != null && paRejectResponseRoot.getOperatingProvider().getRejectreasoncodes() != null) {
                if (paRejectResponseRoot.getOperatingProvider().getQualifier() != null && !paRejectResponseRoot.getOperatingProvider().getQualifier().equals("72")) {
                    log.warn("Operating provider qualifier is incorrect.");
                    errorMessageList.add(generateErrorMessage("OPERATING_PROVIDER_QUALIFIER_CD", properties));
                }
                for (Rejectreasoncode rejectReasonCode : paRejectResponseRoot.getOperatingProvider().getRejectreasoncodes()) {
                    if (rejectReasonCode != null && (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty() || rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty())) {
                        if (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty()) {
                            log.warn("Operating provider reject reason code is missing.");
                            errorMessageList.add(generateErrorMessage("OPERATING_PROVIDER_REASON_CD", properties));
                        }
                        if (rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty()) {
                            log.warn("Operating provider reject reason is missing.");
                            errorMessageList.add(generateErrorMessage("OPERATING_PROVIDER_REASON", properties));
                        }
                    }
                }
            }
            if (paRejectResponseRoot.getAttendingProvider() != null && paRejectResponseRoot.getAttendingProvider().getRejectreasoncodes() != null) {
                if (paRejectResponseRoot.getAttendingProvider().getQualifier() != null && !paRejectResponseRoot.getAttendingProvider().getQualifier().equals("71")) {
                    log.warn("Attending provider qualifier is incorrect.");
                    errorMessageList.add(generateErrorMessage("ATTENDING_PROVIDER_QUALIFIER_CD", properties));
                }
                for (Rejectreasoncode rejectReasonCode : paRejectResponseRoot.getAttendingProvider().getRejectreasoncodes()) {
                    if (rejectReasonCode != null && (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty() || rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty())) {
                        if (rejectReasonCode.getRejectreasoncode() == null || rejectReasonCode.getRejectreasoncode().isEmpty()) {
                            log.warn("Attending provider reject reason code is missing.");
                            errorMessageList.add(generateErrorMessage("ATTENDING_PROVIDER_REASON_CD", properties));
                        }
                        if (rejectReasonCode.getRejectreason() == null || rejectReasonCode.getRejectreason().isEmpty()) {
                            log.warn("Attending provider reject reason is missing.");
                            errorMessageList.add(generateErrorMessage("ATTENDING_PROVIDER_REASON", properties));
                        }
                    }
                }
            }
        }

        log.info("Finished validatePARejectResponse method.");
        return errorMessageList.size() > 0 ? generateResponse(errorMessageList) : null;
    }

    public NotificationResponse validateAdminErrors(AdminErrorNotificationRoot adminErrorNotificationRoot, Properties properties) {
        log.info("Starting validateAdminErrors method.");
        List<ErrorMessage> errorMessageList = new ArrayList<>();

        if (adminErrorNotificationRoot == null) {
            log.warn("AdminErrorNotificationRoot is null.");
            errorMessageList.add(generateErrorMessage("EMPTY_ADMINERROR_RESPONSE", properties));
        } else {
            if (adminErrorNotificationRoot.getNotificationType() == null || adminErrorNotificationRoot.getNotificationType().isEmpty()) {
                log.warn("Notification type is missing.");
                errorMessageList.add(generateErrorMessage("NOTIFICATION_TYPE_MISSING_ERR_CD", properties));
            }
            if (adminErrorNotificationRoot.getSenderRoutingId() == null || adminErrorNotificationRoot.getSenderRoutingId().isEmpty()) {
                log.warn("SenderRoutingId is missing.");
                errorMessageList.add(generateErrorMessage("SENDER_ROUTING_ID_MISSING_ERR_CD", properties));
            }
            if (adminErrorNotificationRoot.getNotification() == null || adminErrorNotificationRoot.getNotification().isEmpty()) {
                log.warn("Notification element is missing.");
                errorMessageList.add(generateErrorMessage("NOTIFICATION_ELEMENT_MISSING_ERR_CD", properties));
            } else {
                for (Notification notification : adminErrorNotificationRoot.getNotification()) {
                    if (notification.getEsMDTransactionId() == null || notification.getEsMDTransactionId().isEmpty()) {
                        log.warn("esMDTransactionId is missing.");
                        errorMessageList.add(generateErrorMessage("ESMD_TRANS_ID_MISSING_ERR_CD", properties));
                    }
                    if (notification.getCreationTime() == null) {
                        log.warn("Creation time is missing.");
                        errorMessageList.add(generateErrorMessage("CREATION_TIME_MSSING_ERR_CD", properties));
                    }
                    if (notification.getSubmissionTime() == null) {
                        log.warn("Submission time is missing.");
                        errorMessageList.add(generateErrorMessage("SUBMISSION_TIME_MISSING_ERR_CD", properties));
                    }
                    if (notification.getErrorMessages() == null || notification.getErrorMessages().isEmpty()) {
                        log.warn("Error message element is missing.");
                        errorMessageList.add(generateErrorMessage("ERROR_MSG_ELEMENT_MISSING_ERR_CD", properties));
                    } else {
                        for (ErrorMessage errorMessage : notification.getErrorMessages()) {
                            if (errorMessage.getErrorCode() == null || errorMessage.getErrorCode().isEmpty()) {
                                log.warn("Error code is missing.");
                                errorMessageList.add(generateErrorMessage("ERROR_CD_MISSING_ERR_CD", properties));
                            }
                            if (errorMessage.getErrorName() == null || errorMessage.getErrorName().isEmpty()) {
                                log.warn("Error name is missing.");
                                errorMessageList.add(generateErrorMessage("ERR_NAME_MISSING_ERR_CD", properties));
                            }
                            if ((errorMessage.getErrorCode().equalsIgnoreCase("Other") || errorMessage.getErrorName().equalsIgnoreCase("Other")) && (errorMessage.getErrorDescription() == null || errorMessage.getErrorDescription().isEmpty())) {
                                log.warn("Error description is missing for 'Other' error code or name.");
                                errorMessageList.add(generateErrorMessage("ERR_DES_MISSING_ERR_CD", properties));
                            }
                        }
                    }
                }
            }
        }

        log.info("Finished validateAdminErrors method.");
        return errorMessageList.size() > 0 ? generateResponse(errorMessageList) : null;
    }
}
