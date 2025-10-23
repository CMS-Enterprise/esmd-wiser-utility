package gov.cms.esmd.bean.response;

import gov.cms.esmd.bean.ErrorMessage;

import java.util.ArrayList;
import java.util.List;

public class StatusDetail {

    private String esMDTransactionID;
    private String contenttypecd;
    private String parentTransactionID;
    private String deliveryType;
    private String uniqueTrackingNumber;

    public String getUniqueTrackingNumber() {
        return uniqueTrackingNumber;
    }

    public void setUniqueTrackingNumber(String uniqueTrackingNumber) {
        this.uniqueTrackingNumber = uniqueTrackingNumber;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(String carrierId) {
        this.carrierId = carrierId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    private String uniqueId;
    private String carrierId;
    private String transactionType;
    private String status;
    private String statusDescription;
    private List<ErrorMessage> errorMessages;

    public StatusDetail(List<ErrorMessage> errorMessageList){
        status = "FAILED";
        errorMessages = errorMessageList;
    }
    public String getEsMDTransactionID() {
        return esMDTransactionID;
    }

    public void setEsMDTransactionID(String esMDTransactionID) {
        this.esMDTransactionID = esMDTransactionID;
    }

    public String getContenttypecd() {
        return contenttypecd;
    }

    public void setContenttypecd(String contenttypecd) {
        this.contenttypecd = contenttypecd;
    }

    public String getParentTransactionID() {
        return parentTransactionID;
    }

    public void setParentTransactionID(String parentTransactionID) {
        this.parentTransactionID = parentTransactionID;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public List<ErrorMessage> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<ErrorMessage> errorMessages) {
        this.errorMessages = errorMessages;
    }
}
