package evidence_verification.dto;

public class UpdateCaseStatusRequest {

    private String status;
    private String reason;

    public UpdateCaseStatusRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
