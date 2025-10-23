package gov.cms.esmd.bean.adminerror;

import java.util.ArrayList;

public class AdminErrorNotificationRoot {
    private String notificationType;
    private String senderRoutingId;
    private ArrayList<Notification> notification;

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getSenderRoutingId() {
        return senderRoutingId;
    }

    public void setSenderRoutingId(String senderRoutingId) {
        this.senderRoutingId = senderRoutingId;
    }

    public ArrayList<Notification> getNotification() {
        return notification;
    }

    public void setNotification(ArrayList<Notification> notification) {
        this.notification = notification;
    }
}
