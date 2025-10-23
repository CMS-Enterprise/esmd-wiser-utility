package gov.cms.esmd.bean.response;

import gov.cms.esmd.bean.ErrorMessage;

import java.util.ArrayList;
import java.util.List;

public class EsmdStatusResponse {
    private String esmdTransactionId;
    private String routingId;
    private String hihOid;
    private String rcOid;
    private String rcName;
    private String letterId;
    private String contentType;
    private String status;
    private String statusDescription;
    private List<ErrorMessage> errorDetails = new ArrayList<>();

    public String getEsmdTransactionId() {
        return esmdTransactionId;
    }

    public void setEsmdTransactionId(String esmdTransactionId) {
        this.esmdTransactionId = esmdTransactionId;
    }

    public String getRoutingId() {
        return routingId;
    }

    public void setRoutingId(String routingId) {
        this.routingId = routingId;
    }

    public String getHihOid() {
        return hihOid;
    }

    public void setHihOid(String hihOid) {
        this.hihOid = hihOid;
    }

    public String getRcOid() {
        return rcOid;
    }

    public void setRcOid(String rcOid) {
        this.rcOid = rcOid;
    }

    public String getRcName() {
        return rcName;
    }

    public void setRcName(String rcName) {
        this.rcName = rcName;
    }

    public String getLetterId() {
        return letterId;
    }

    public void setLetterId(String letterId) {
        this.letterId = letterId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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

    public List<ErrorMessage> getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(List<ErrorMessage> errorDetails) {
        this.errorDetails = errorDetails;
    }
}
