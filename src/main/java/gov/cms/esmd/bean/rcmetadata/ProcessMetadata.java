package gov.cms.esmd.bean.rcmetadata;

import java.util.List;

public class ProcessMetadata {
    private String uniqueID;
    private String uniqueTrackingNumber;
    private int numberOfDocuments;
    private SubmissionMetadata submissionMetadata;
    private List<Document> documents;

    // Getters and Setters
    public String getUniqueID() {
        return uniqueID;
    }
    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getUniqueTrackingNumber() {
        return uniqueTrackingNumber;
    }
    public void setUniqueTrackingNumber(String uniqueTrackingNumber) {
        this.uniqueTrackingNumber = uniqueTrackingNumber;
    }

    public int getNumberOfDocuments() {
        return numberOfDocuments;
    }
    public void setNumberOfDocuments(int numberOfDocuments) {
        this.numberOfDocuments = numberOfDocuments;
    }

    public SubmissionMetadata getSubmissionMetadata() {
        return submissionMetadata;
    }
    public void setSubmissionMetadata(SubmissionMetadata submissionMetadata) {
        this.submissionMetadata = submissionMetadata;
    }

    public List<Document> getDocuments() {
        return documents;
    }
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
