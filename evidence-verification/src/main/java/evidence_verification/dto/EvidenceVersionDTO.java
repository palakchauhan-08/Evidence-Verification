package evidence_verification.dto;

import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.EvidenceVersion;

import java.time.LocalDateTime;

public class EvidenceVersionDTO {

    private String versionId;
    private String evidenceId;
    private String caseId;
    private Integer versionNumber;
    private String fileName;
    private String fileExtension;
    private String fileType;
    private Long fileSize;
    private String fileHash;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
    private LocalDateTime createdTimestamp;
    private LocalDateTime modifiedTimestamp;
    private String status;
    private String rejectionReason;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime reviewStartedAt;
    private BlockchainRecord blockchainRecord;

    public EvidenceVersionDTO() {
    }

    public EvidenceVersionDTO(EvidenceVersion version, BlockchainRecord blockchainRecord) {
        if (version != null) {
            this.versionId = version.getVersionId();
            if (version.getEvidence() != null) {
                this.evidenceId = version.getEvidence().getEvidenceId();
                this.caseId = version.getEvidence().getCaseId();
            }
            this.versionNumber = version.getVersionNumber();
            this.fileName = version.getFileName();
            this.fileExtension = version.getFileExtension();
            this.fileType = version.getFileType();
            this.fileSize = version.getFileSize();
            this.fileHash = version.getFileHash();
            this.uploadedAt = version.getUploadedAt();
            this.uploadedBy = version.getUploadedBy();
            this.createdTimestamp = version.getCreatedTimestamp();
            this.modifiedTimestamp = version.getModifiedTimestamp();
            this.status = version.getStatus();
            this.rejectionReason = version.getRejectionReason();
            this.reviewedBy = version.getReviewedBy();
            this.reviewedAt = version.getReviewedAt();
            this.reviewStartedAt = version.getReviewStartedAt();
        }
        this.blockchainRecord = blockchainRecord;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
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

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public LocalDateTime getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public void setModifiedTimestamp(LocalDateTime modifiedTimestamp) {
        this.modifiedTimestamp = modifiedTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getReviewStartedAt() {
        return reviewStartedAt;
    }

    public void setReviewStartedAt(LocalDateTime reviewStartedAt) {
        this.reviewStartedAt = reviewStartedAt;
    }

    public BlockchainRecord getBlockchainRecord() {
        return blockchainRecord;
    }

    public void setBlockchainRecord(BlockchainRecord blockchainRecord) {
        this.blockchainRecord = blockchainRecord;
    }
}
