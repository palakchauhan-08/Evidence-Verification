package evidence_verification.dto;

import evidence_verification.Entity.AuditLog;
import evidence_verification.Entity.Case;
import evidence_verification.Entity.Evidence;

import java.util.List;
import java.util.Map;

public class CaseDetailDTO {

    private CaseResponseDTO caseDetails;
    private List<EvidenceResponseDTO> evidenceList;
    private Map<String, Integer> evidenceSummary;
    private List<AuditLog> auditLogs;

    public CaseDetailDTO() {
    }

    public CaseDetailDTO(Case caseEntity, List<Evidence> evidenceEntities, List<AuditLog> auditLogs) {
        this.caseDetails = new CaseResponseDTO(caseEntity);
        this.evidenceList = evidenceEntities != null
                ? evidenceEntities.stream().map(EvidenceResponseDTO::new).toList()
                : List.of();

        int total = evidenceList.size();
        int uploaded = (int) evidenceList.stream().filter(e -> "UPLOADED".equalsIgnoreCase(e.getStatus())).count();
        int underReview = (int) evidenceList.stream().filter(e -> "UNDER_REVIEW".equalsIgnoreCase(e.getStatus())).count();
        int verified = (int) evidenceList.stream().filter(e -> "VERIFIED".equalsIgnoreCase(e.getStatus())).count();
        int rejected = (int) evidenceList.stream().filter(e -> "REJECTED".equalsIgnoreCase(e.getStatus())).count();
        int tampered = (int) evidenceList.stream().filter(e -> "TAMPERED".equalsIgnoreCase(e.getStatus())).count();

        this.evidenceSummary = Map.of(
                "total", total,
                "uploaded", uploaded,
                "underReview", underReview,
                "verified", verified,
                "rejected", rejected,
                "tampered", tampered
        );

        this.auditLogs = auditLogs != null ? auditLogs : List.of();
    }

    public CaseResponseDTO getCaseDetails() {
        return caseDetails;
    }

    public List<EvidenceResponseDTO> getEvidenceList() {
        return evidenceList;
    }

    public Map<String, Integer> getEvidenceSummary() {
        return evidenceSummary;
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogs;
    }
}
