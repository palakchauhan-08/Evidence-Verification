package evidence_verification.dto;

public class VerificationResponse {

    private String evidenceId;
    private String fileName;
    private String calculatedHash;
    private String storedHash;
    private String blockchainHash;
    private String verificationStatus;
    private String verificationMessage;

    public VerificationResponse() {
    }

    public VerificationResponse(String evidenceId, String fileName, String calculatedHash,
                                String storedHash, String blockchainHash,
                                String verificationStatus, String verificationMessage) {
        this.evidenceId = evidenceId;
        this.fileName = fileName;
        this.calculatedHash = calculatedHash;
        this.storedHash = storedHash;
        this.blockchainHash = blockchainHash;
        this.verificationStatus = verificationStatus;
        this.verificationMessage = verificationMessage;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCalculatedHash() {
        return calculatedHash;
    }

    public void setCalculatedHash(String calculatedHash) {
        this.calculatedHash = calculatedHash;
    }

    public String getStoredHash() {
        return storedHash;
    }

    public void setStoredHash(String storedHash) {
        this.storedHash = storedHash;
    }

    public String getBlockchainHash() {
        return blockchainHash;
    }

    public void setBlockchainHash(String blockchainHash) {
        this.blockchainHash = blockchainHash;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getVerificationMessage() {
        return verificationMessage;
    }

    public void setVerificationMessage(String verificationMessage) {
        this.verificationMessage = verificationMessage;
    }
}
