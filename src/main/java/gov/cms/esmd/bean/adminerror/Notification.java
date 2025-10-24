package gov.cms.esmd.bean.adminerror;

import gov.cms.esmd.bean.ErrorMessage;

import java.util.ArrayList;
import java.util.Date;

public class Notification {
    private String esMDTransactionId;
    private String creationTime;
    private String submissionTime;

    public String getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    private String pickupTime;
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private String filename;
    private ArrayList<ErrorMessage> errorMessages;

    public String getEsMDTransactionId() {
        return esMDTransactionId;
    }

    public void setEsMDTransactionId(String esMDTransactionId) {
        this.esMDTransactionId = esMDTransactionId;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    public ArrayList<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(ArrayList<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }
}
