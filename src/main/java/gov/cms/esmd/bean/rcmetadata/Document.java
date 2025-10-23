package gov.cms.esmd.bean.rcmetadata;

public class Document {
    private String documentUniqueIdentifier;
    private String mimeType;
    private String fileName;
    private String checkSum;
    private long fileSize;

    // Getters and Setters
    public String getDocumentUniqueIdentifier() {
        return documentUniqueIdentifier;
    }
    public void setDocumentUniqueIdentifier(String documentUniqueIdentifier) {
        this.documentUniqueIdentifier = documentUniqueIdentifier;
    }

    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCheckSum() {
        return checkSum;
    }
    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public long getFileSize() {
        return fileSize;
    }
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
