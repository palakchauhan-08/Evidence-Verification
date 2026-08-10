package evidence_verification.dto;

import evidence_verification.Entity.AuditLog;
import evidence_verification.Entity.BlockchainRecord;

import java.util.List;

public class EvidenceDetailDTO {

    private Long id;
    private String evidenceId;
    private String fileName;
    private String fileType;
    private String fileHash;
    private String uploadedBy;
    private BlockchainRecord blockchainRecord;
    private List<AuditLog> auditLogs;

    public EvidenceDetailDTO() {
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
}
