package evidence_verification.Entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cases")
public class Case {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String caseId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String priority = CasePriority.MEDIUM.name();

    private String status = CaseStatus.OPEN.name();

    private String assignedInvestigator;

    @Column(nullable = false)
    private String createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "caseRecord", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Evidence> evidenceList = new ArrayList<>();

    public Case() {
    }

    public Long getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        if (priority == null || priority.trim().isEmpty()) {
            return CasePriority.MEDIUM.name();
        }
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        if (status == null || status.trim().isEmpty()) {
            return CaseStatus.OPEN.name();
        }
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedInvestigator() {
        return assignedInvestigator;
    }

    public void setAssignedInvestigator(String assignedInvestigator) {
        this.assignedInvestigator = assignedInvestigator;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public List<Evidence> getEvidenceList() {
        return evidenceList;
    }

    public void setEvidenceList(List<Evidence> evidenceList) {
        this.evidenceList = evidenceList;
    }

    public void addEvidence(Evidence evidence) {
        if (evidence != null) {
            evidenceList.add(evidence);
            evidence.setCaseRecord(this);
        }
    }

    public void removeEvidence(Evidence evidence) {
        if (evidence != null) {
            evidenceList.remove(evidence);
            evidence.setCaseRecord(null);
        }
    }
}
