package evidence_verification.dto;

import java.time.LocalDateTime;

public class PublicVerificationResponseDTO {

    private String evidenceId;
    private String fileName;
    private String fileExtension;
    private String fileType;
    private Long fileSize;
    private String fileHash;
    private String verificationStatus;
    private LocalDateTime verificationTimestamp;
    private String blockchainNetwork;
    private String blockchainTransactionHash;
    private String polygonExplorerUrl;
    private String caseId;
    private String message;

    public PublicVerificationResponseDTO() {
    }

    public PublicVerificationResponseDTO(
            String evidenceId,
            String fileName,
            String fileExtension,
            String fileType,
            Long fileSize,
            String fileHash,
            String verificationStatus,
            LocalDateTime verificationTimestamp,
            String blockchainNetwork,
            String blockchainTransactionHash,
            String polygonExplorerUrl,
            String caseId,
            String message) {
        this.evidenceId = evidenceId;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.verificationStatus = verificationStatus;
        this.verificationTimestamp = verificationTimestamp;
        this.blockchainNetwork = blockchainNetwork;
        this.blockchainTransactionHash = blockchainTransactionHash;
        this.polygonExplorerUrl = polygonExplorerUrl;
        this.caseId = caseId;
        this.message = message;
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

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDateTime getVerificationTimestamp() {
        return verificationTimestamp;
    }

    public void setVerificationTimestamp(LocalDateTime verificationTimestamp) {
        this.verificationTimestamp = verificationTimestamp;
    }

    public String getBlockchainNetwork() {
        return blockchainNetwork;
    }

    public void setBlockchainNetwork(String blockchainNetwork) {
        this.blockchainNetwork = blockchainNetwork;
    }

    public String getBlockchainTransactionHash() {
        return blockchainTransactionHash;
    }

    public void setBlockchainTransactionHash(String blockchainTransactionHash) {
        this.blockchainTransactionHash = blockchainTransactionHash;
    }

    public String getPolygonExplorerUrl() {
        return polygonExplorerUrl;
    }

    public void setPolygonExplorerUrl(String polygonExplorerUrl) {
        this.polygonExplorerUrl = polygonExplorerUrl;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
