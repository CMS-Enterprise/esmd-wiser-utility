package gov.cms.esmd.bean.response;

import java.util.List;

public class PresignedUrlResponse {
    private String status;
    private String message;
    private List<Content> contents;

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

    public List<Content> getContents() {
        return contents;
    }
    public void setContents(List<Content> contents) {
        this.contents = contents;
    }
}
