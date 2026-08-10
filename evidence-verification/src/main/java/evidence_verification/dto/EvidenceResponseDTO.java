package evidence_verification.dto;

import evidence_verification.Entity.Evidence;

public class EvidenceResponseDTO {

    private Long id;
    private String evidenceId;
    private String fileName;
    private String fileType;
    private String fileHash;
    private String uploadedBy;

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
        }
    }

    public EvidenceResponseDTO(Long id, String evidenceId, String fileName, String fileType, String fileHash, String uploadedBy) {
        this.id = id;
        this.evidenceId = evidenceId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileHash = fileHash;
        this.uploadedBy = uploadedBy;
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
}
