package evidence_verification.dto;

public class RejectEvidenceRequest {

    private String reason;

    public RejectEvidenceRequest() {
    }

    public RejectEvidenceRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
