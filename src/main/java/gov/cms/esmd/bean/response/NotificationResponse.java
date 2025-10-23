package gov.cms.esmd.bean.response;

import java.util.ArrayList;

public class NotificationResponse {
    private String senderRoutingID;
    private String message;
    private String senderRoutingId;
    private ArrayList<StatusDetail> statusDetails;

    public String getSenderRoutingID() {
        return senderRoutingID;
    }

    public void setSenderRoutingID(String senderRoutingID) {
        this.senderRoutingID = senderRoutingID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderRoutingId() {
        return senderRoutingId;
    }

    public void setSenderRoutingId(String senderRoutingId) {
        this.senderRoutingId = senderRoutingId;
    }

    public ArrayList<StatusDetail> getStatusDetails() {
        return statusDetails;
    }

    public void setStatusDetails(ArrayList<StatusDetail> statusDetails) {
        this.statusDetails = statusDetails;
    }
}
