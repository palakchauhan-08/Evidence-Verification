package evidence_verification.dto;

import evidence_verification.Entity.Evidence;
import java.time.LocalDateTime;

public class EvidenceResponseDTO {

    private Long id;
    private String evidenceId;
    private String fileName;
    private String fileType;
    private String fileHash;
    private String uploadedBy;
    private String status;
    private String rejectionReason;
    private LocalDateTime uploadedAt;
    private String fileExtension;
    private Long fileSize;
    private LocalDateTime createdTimestamp;
    private LocalDateTime modifiedTimestamp;
    private String caseId;

    public EvidenceResponseDTO() {
    }

    public EvidenceResponseDTO(Evidence evidence) {
        if (evidence != null) {
            this.id = evidence.getId();
            this.evidenceId = evidence.getEvidenceId();
            this.fileName = evidence.getFileName();
            this.fileType = evidence.getFileType();
            this.fileHash = evidence.getFileHash();
            this.uploadedBy = evidence.getUploadedBy();
            this.status = evidence.getStatus();
            this.rejectionReason = evidence.getRejectionReason();
            this.uploadedAt = evidence.getUploadedAt();
            this.fileExtension = evidence.getFileExtension();
            this.fileSize = evidence.getFileSize();
            this.createdTimestamp = evidence.getCreatedTimestamp();
            this.modifiedTimestamp = evidence.getModifiedTimestamp();
            this.caseId = evidence.getCaseId();
        }
    }

    public EvidenceResponseDTO(Long id, String evidenceId, String fileName, String fileType, String fileHash, String uploadedBy, String status) {
        this.id = id;
        this.evidenceId = evidenceId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileHash = fileHash;
        this.uploadedBy = uploadedBy;
        this.status = status;
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

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
}
