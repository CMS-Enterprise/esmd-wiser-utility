package gov.cms.esmd.bean.response;


import gov.cms.esmd.bean.adminerror.Notification;

import java.util.List;

public class PickupNotification {

    private String notificationType;
    private String esMDTransactionId;
    private String senderRoutingId;
    private String pickupTime;
    private String submissionTime;
    private String fileName;

    public List<Notification> getNotification() {
        return notification;
    }

    public void setNotification(List<Notification> notification) {
        this.notification = notification;
    }

    private List<Notification> notification;
    private ErrorInfo errorInfo;

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getEsMDTransactionId() {
        return esMDTransactionId;
    }

    public void setEsMDTransactionId(String esMDTransactionId) {
        this.esMDTransactionId = esMDTransactionId;
    }

    public String getSenderRoutingId() {
        return senderRoutingId;
    }

    public void setSenderRoutingId(String senderRoutingId) {
        this.senderRoutingId = senderRoutingId;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(ErrorInfo errorInfo) {
        this.errorInfo = errorInfo;
    }

    public static class ErrorInfo {
        private String errorCode;
        private String errorName;
        private String errorDescription;

        public ErrorInfo() {}

        public ErrorInfo(String errorCode, String errorName, String errorDescription) {
            this.errorCode = errorCode;
            this.errorName = errorName;
            this.errorDescription = errorDescription;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorName() {
            return errorName;
        }

        public void setErrorName(String errorName) {
            this.errorName = errorName;
        }

        public String getErrorDescription() {
            return errorDescription;
        }

        public void setErrorDescription(String errorDescription) {
            this.errorDescription = errorDescription;
        }
    }
}
