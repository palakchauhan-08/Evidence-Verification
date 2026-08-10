package evidence_verification.service;

import evidence_verification.Entity.AuditLog;
import evidence_verification.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<AuditLog> getAuditLogsForEvidence(String evidenceId) {
        return auditLogRepository.findByEvidenceIdOrderByTimestampDesc(evidenceId);
    }
}
