package gov.cms.esmd.bean.rcmetadata;

public class SubmissionMetadata {
    private String creationTime;
    private String senderRoutingName;
    private String receiveroid;
    private String deliveryType;
    private String contentTypeCode;

    // Getters and Setters
    public String getCreationTime() {
        return creationTime;
    }
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    public String getSenderRoutingName() {
        return senderRoutingName;
    }
    public void setSenderRoutingName(String senderRoutingName) {
        this.senderRoutingName = senderRoutingName;
    }

    public String getReceiveroid() {
        return receiveroid;
    }
    public void setReceiveroid(String receiveroid) {
        this.receiveroid = receiveroid;
    }

    public String getDeliveryType() {
        return deliveryType;
    }
    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getContentTypeCode() {
        return contentTypeCode;
    }
    public void setContentTypeCode(String contentTypeCode) {
        this.contentTypeCode = contentTypeCode;
    }
}
