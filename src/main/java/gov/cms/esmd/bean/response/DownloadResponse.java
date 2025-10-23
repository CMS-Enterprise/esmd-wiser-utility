package gov.cms.esmd.bean.response;

import java.util.List;

public class DownloadResponse {
    private String status;
    private String message;
    private List<DownloadObject> objects;

    // Getters and Setters
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public List<DownloadObject> getObjects() {
        return objects;
    }
    public void setObjects(List<DownloadObject> objects) {
        this.objects = objects;
    }
}