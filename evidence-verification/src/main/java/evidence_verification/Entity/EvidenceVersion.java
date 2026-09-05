package evidence_verification.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evidence_versions")
public class EvidenceVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String versionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evidence_id", referencedColumnName = "evidenceId", nullable = false)
    private Evidence evidence;

    @Column(nullable = false)
    private Integer versionNumber;

    @Column(nullable = false)
    private String fileName;

    private String fileExtension;

    private String fileType;

    private Long fileSize;

    @Column(nullable = false)
    private String fileHash;

    private LocalDateTime uploadedAt;

    private String uploadedBy;

    private LocalDateTime createdTimestamp;

    private LocalDateTime modifiedTimestamp;

    @Column(nullable = false)
    private String status;

    private String rejectionReason;

    private String reviewedBy;

    private LocalDateTime reviewedAt;

    private LocalDateTime reviewStartedAt;

    public EvidenceVersion() {
        this.uploadedAt = LocalDateTime.now();
        this.status = EvidenceStatus.UPLOADED.name();
    }

    public EvidenceVersion(
            String versionId,
            Evidence evidence,
            Integer versionNumber,
            String fileName,
            String fileExtension,
            String fileType,
            Long fileSize,
            String fileHash,
            String uploadedBy,
            LocalDateTime createdTimestamp,
            LocalDateTime modifiedTimestamp) {
        this.versionId = versionId;
        this.evidence = evidence;
        this.versionNumber = versionNumber;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileHash = fileHash;
        this.uploadedBy = uploadedBy;
        this.createdTimestamp = createdTimestamp;
        this.modifiedTimestamp = modifiedTimestamp;
        this.uploadedAt = LocalDateTime.now();
        this.status = EvidenceStatus.UPLOADED.name();
    }

    public Long getId() {
        return id;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
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
}
