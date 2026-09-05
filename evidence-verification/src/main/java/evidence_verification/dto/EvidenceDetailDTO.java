package evidence_verification.dto;

import evidence_verification.Entity.AuditLog;
import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.Evidence;

import java.time.LocalDateTime;
import java.util.List;

public class EvidenceDetailDTO {

    private Long id;
    private String evidenceId;
    private String fileName;
    private String fileType;
    private String fileHash;
    private String uploadedBy;
    private String status;
    private String rejectionReason;
    private LocalDateTime reviewStartedAt;
    private LocalDateTime reviewedAt;
    private String reviewedBy;
    private LocalDateTime uploadedAt;
    private String fileExtension;
    private Long fileSize;
    private LocalDateTime createdTimestamp;
    private LocalDateTime modifiedTimestamp;
    private BlockchainRecord blockchainRecord;
    private List<AuditLog> auditLogs;
    private List<EvidenceVersionDTO> versions;
    private Integer currentVersionNumber;

    public EvidenceDetailDTO() {
    }

    public EvidenceDetailDTO(Evidence evidence, BlockchainRecord blockchainRecord, List<AuditLog> auditLogs, List<EvidenceVersionDTO> versions) {
        if (evidence != null) {
            this.id = evidence.getId();
            this.evidenceId = evidence.getEvidenceId();
            this.fileName = evidence.getFileName();
            this.fileType = evidence.getFileType();
            this.fileHash = evidence.getFileHash();
            this.uploadedBy = evidence.getUploadedBy();
            this.status = evidence.getStatus();
            this.rejectionReason = evidence.getRejectionReason();
            this.reviewStartedAt = evidence.getReviewStartedAt();
            this.reviewedAt = evidence.getReviewedAt();
            this.reviewedBy = evidence.getReviewedBy();
            this.uploadedAt = evidence.getUploadedAt();
            this.fileExtension = evidence.getFileExtension();
            this.fileSize = evidence.getFileSize();
            this.createdTimestamp = evidence.getCreatedTimestamp();
            this.modifiedTimestamp = evidence.getModifiedTimestamp();
        }
        this.blockchainRecord = blockchainRecord;
        this.auditLogs = auditLogs;
        this.versions = versions;
        if (versions != null && !versions.isEmpty()) {
            this.currentVersionNumber = versions.get(0).getVersionNumber();
        } else {
            this.currentVersionNumber = 1;
        }
    }

    public EvidenceDetailDTO(Evidence evidence, BlockchainRecord blockchainRecord, List<AuditLog> auditLogs) {
        this(evidence, blockchainRecord, auditLogs, null);
    }

    public EvidenceDetailDTO(Long id, String evidenceId, String fileName, String fileType, String fileHash,
                             String uploadedBy, BlockchainRecord blockchainRecord, List<AuditLog> auditLogs) {
        this.id = id;
        this.evidenceId = evidenceId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileHash = fileHash;
        this.uploadedBy = uploadedBy;
        this.blockchainRecord = blockchainRecord;
        this.auditLogs = auditLogs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
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

    public LocalDateTime getReviewStartedAt() {
        return reviewStartedAt;
    }

    public void setReviewStartedAt(LocalDateTime reviewStartedAt) {
        this.reviewStartedAt = reviewStartedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
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

    public BlockchainRecord getBlockchainRecord() {
        return blockchainRecord;
    }

    public void setBlockchainRecord(BlockchainRecord blockchainRecord) {
        this.blockchainRecord = blockchainRecord;
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogs;
    }

    public void setAuditLogs(List<AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }

    public List<EvidenceVersionDTO> getVersions() {
        return versions;
    }

    public void setVersions(List<EvidenceVersionDTO> versions) {
        this.versions = versions;
    }

    public Integer getCurrentVersionNumber() {
        return currentVersionNumber;
    }

    public void setCurrentVersionNumber(Integer currentVersionNumber) {
        this.currentVersionNumber = currentVersionNumber;
    }
}
