package gov.cms.esmd.bean.response;

public class DownloadObject {
    private String filename;
    private String size;
    private String createdOn;
    private String lastDownloaded;

    // Getters and Setters
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSize() {
        return size;
    }
    public void setSize(String size) {
        this.size = size;
    }

    public String getCreatedOn() {
        return createdOn;
    }
    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getLastDownloaded() {
        return lastDownloaded;
    }
    public void setLastDownloaded(String lastDownloaded) {
        this.lastDownloaded = lastDownloaded;
    }
}