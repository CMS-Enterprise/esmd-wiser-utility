package gov.cms.esmd.bean;

public class ErrorMessage {

    private String errorCode;
    private String errorName;
    private String errorDescription;

    public ErrorMessage(String errorCode, String errorName, String errorDescription){
        this.errorCode = errorCode;
        this.errorName = errorName;
        this.errorDescription = errorDescription;
    }
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorName() {
        return errorName;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}
