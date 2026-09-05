package evidence_verification.dto;

import evidence_verification.Entity.Case;

import java.time.LocalDateTime;

public class CaseResponseDTO {

    private Long id;
    private String caseId;
    private String title;
    private String description;
    private String priority;
    private String status;
    private String assignedInvestigator;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private int evidenceCount;

    public CaseResponseDTO() {
    }

    public CaseResponseDTO(Case caseEntity) {
        if (caseEntity != null) {
            this.id = caseEntity.getId();
            this.caseId = caseEntity.getCaseId();
            this.title = caseEntity.getTitle();
            this.description = caseEntity.getDescription();
            this.priority = caseEntity.getPriority();
            this.status = caseEntity.getStatus();
            this.assignedInvestigator = caseEntity.getAssignedInvestigator();
            this.createdBy = caseEntity.getCreatedBy();
            this.createdAt = caseEntity.getCreatedAt();
            this.updatedAt = caseEntity.getUpdatedAt();
            this.closedAt = caseEntity.getClosedAt();
            this.evidenceCount = caseEntity.getEvidenceList() != null ? caseEntity.getEvidenceList().size() : 0;
        }
    }

    public Long getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public String getAssignedInvestigator() {
        return assignedInvestigator;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public int getEvidenceCount() {
        return evidenceCount;
    }
}
