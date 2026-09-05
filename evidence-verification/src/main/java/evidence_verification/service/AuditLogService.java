package evidence_verification.service;

import evidence_verification.Entity.AuditLog;
import evidence_verification.Entity.ChainOfCustodyAction;
import evidence_verification.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog logAction(String evidenceId, String action, String performedBy, String details) {
        AuditLog auditLog = new AuditLog(evidenceId, action, performedBy, details);
        return auditLogRepository.save(auditLog);
    }

    public AuditLog logCustodyEvent(String evidenceId, String action, String performedBy, String actorRole,
                                    String previousStatus, String newStatus, String reason, String details) {
        AuditLog auditLog = new AuditLog(evidenceId, action, performedBy, actorRole, previousStatus, newStatus, reason, details);
        return auditLogRepository.save(auditLog);
    }

    public void logAccessEvent(String evidenceId, String performedBy, String actorRole) {
        if (evidenceId == null || performedBy == null) return;

        String action = ChainOfCustodyAction.EVIDENCE_ACCESSED.name();
        Optional<AuditLog> latestAccess = auditLogRepository
                .findFirstByEvidenceIdAndActionAndPerformedByOrderByTimestampDesc(evidenceId, action, performedBy);

        // Deduplication: If the user accessed this evidence in the last 5 minutes, skip duplicate logging
        if (latestAccess.isPresent()) {
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
            if (latestAccess.get().getTimestamp().isAfter(cutoff)) {
                return;
            }
        }

        logCustodyEvent(
                evidenceId,
                action,
                performedBy,
                actorRole != null ? actorRole : "USER",
                null,
                null,
                null,
                "Evidence details accessed by " + performedBy + " (" + (actorRole != null ? actorRole : "USER") + ")"
        );
    }

    public List<AuditLog> getAuditLogsForEvidence(String evidenceId) {
        return auditLogRepository.findByEvidenceIdOrderByTimestampDesc(evidenceId);
    }

    public List<AuditLog> getChainOfCustodyForEvidence(String evidenceId) {
        return auditLogRepository.findByEvidenceIdOrderByTimestampAsc(evidenceId);
    }
}
