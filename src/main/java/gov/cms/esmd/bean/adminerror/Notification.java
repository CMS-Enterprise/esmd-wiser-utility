package gov.cms.esmd.bean.adminerror;

import gov.cms.esmd.bean.ErrorMessage;

import java.util.ArrayList;
import java.util.Date;

public class Notification {
    private String esMDTransactionId;
    private Date creationTime;
    private Date submissionTime;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private String fileName;
    private ArrayList<ErrorMessage> errorMessages;

    public String getEsMDTransactionId() {
        return esMDTransactionId;
    }

    public void setEsMDTransactionId(String esMDTransactionId) {
        this.esMDTransactionId = esMDTransactionId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Date submissionTime) {
        this.submissionTime = submissionTime;
    }

    public ArrayList<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(ArrayList<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }
}
