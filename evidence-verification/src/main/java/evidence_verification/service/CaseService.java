package evidence_verification.service;

import evidence_verification.Entity.AuditLog;
import evidence_verification.Entity.Case;
import evidence_verification.Entity.CasePriority;
import evidence_verification.Entity.CaseStatus;
import evidence_verification.Entity.ChainOfCustodyAction;
import evidence_verification.Entity.Evidence;
import evidence_verification.dto.AssignCaseRequest;
import evidence_verification.dto.CaseDetailDTO;
import evidence_verification.dto.CaseResponseDTO;
import evidence_verification.dto.CreateCaseRequest;
import evidence_verification.dto.UpdateCaseStatusRequest;
import evidence_verification.repository.CaseRepository;
import evidence_verification.repository.EvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final EvidenceRepository evidenceRepository;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    public CaseService(
            CaseRepository caseRepository,
            EvidenceRepository evidenceRepository,
            AuditLogService auditLogService,
            EmailService emailService
    ) {
        this.caseRepository = caseRepository;
        this.evidenceRepository = evidenceRepository;
        this.auditLogService = auditLogService;
        this.emailService = emailService;
    }

    private synchronized String generateNextCaseId() {
        int currentYear = Year.now().getValue();
        long count = caseRepository.count();
        long nextNum = count + 1;
        return String.format("CASE-%d-%03d", currentYear, nextNum);
    }

    @Transactional
    public CaseResponseDTO createCase(CreateCaseRequest request, String createdBy, String actorRole) {
        if (request == null || request.getTitle() == null || request.getTitle().trim().isBlank()) {
            throw new IllegalArgumentException("Case title is required");
        }

        String priorityStr = request.getPriority() != null && CasePriority.isValid(request.getPriority())
                ? request.getPriority().toUpperCase()
                : CasePriority.MEDIUM.name();

        Case caseEntity = new Case();
        caseEntity.setCaseId(generateNextCaseId());
        caseEntity.setTitle(request.getTitle().trim());
        caseEntity.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        caseEntity.setPriority(priorityStr);
        caseEntity.setStatus(CaseStatus.OPEN.name());
        caseEntity.setCreatedBy(createdBy);
        caseEntity.setCreatedAt(LocalDateTime.now());
        caseEntity.setUpdatedAt(LocalDateTime.now());

        if (request.getAssignedInvestigator() != null && !request.getAssignedInvestigator().trim().isBlank()) {
            caseEntity.setAssignedInvestigator(request.getAssignedInvestigator().trim());
        }

        Case saved = caseRepository.save(caseEntity);

        // Audit Event: CASE_CREATED
        auditLogService.logCustodyEvent(
                saved.getCaseId(),
                ChainOfCustodyAction.CASE_CREATED.name(),
                createdBy,
                actorRole,
                null,
                CaseStatus.OPEN.name(),
                null,
                "Investigation case '" + saved.getTitle() + "' (" + saved.getCaseId() + ") created with priority " + saved.getPriority()
        );

        if (saved.getAssignedInvestigator() != null && !saved.getAssignedInvestigator().isBlank()) {
            auditLogService.logCustodyEvent(
                    saved.getCaseId(),
                    ChainOfCustodyAction.CASE_ASSIGNED.name(),
                    createdBy,
                    actorRole,
                    null,
                    CaseStatus.OPEN.name(),
                    null,
                    "Case assigned to investigator: " + saved.getAssignedInvestigator()
            );

            // Email Notification Trigger: CASE_ASSIGNED
            try {
                if (saved.getAssignedInvestigator().contains("@")) {
                    boolean sent = emailService.sendCaseAssignedNotification(
                            saved.getAssignedInvestigator(),
                            saved.getCaseId(),
                            saved.getTitle(),
                            saved.getPriority(),
                            saved.getStatus(),
                            createdBy
                    );
                    auditLogService.logCustodyEvent(
                            saved.getCaseId(),
                            sent ? ChainOfCustodyAction.EMAIL_NOTIFICATION_SENT.name() : ChainOfCustodyAction.EMAIL_NOTIFICATION_FAILED.name(),
                            createdBy,
                            actorRole,
                            null,
                            CaseStatus.OPEN.name(),
                            null,
                            (sent ? "Case assignment notification email sent to " : "Failed to send case assignment notification email to ") + saved.getAssignedInvestigator()
                    );
                }
            } catch (Exception e) {
                // Failure to send email must NOT break primary case creation operation
            }
        }

        return new CaseResponseDTO(saved);
    }

    public List<CaseResponseDTO> getCases(String userEmail, String role, String search, String status, String priority, String investigator) {
        String searchParam = (search != null && !search.trim().isBlank()) ? search.trim() : null;
        String statusParam = (status != null && !status.trim().isBlank()) ? status.trim() : null;
        String priorityParam = (priority != null && !priority.trim().isBlank()) ? priority.trim() : null;
        String investigatorParam = (investigator != null && !investigator.trim().isBlank()) ? investigator.trim() : null;

        List<Case> caseList = caseRepository.searchAndFilterCases(searchParam, statusParam, priorityParam, investigatorParam);

        boolean canViewAll = "ADMIN".equalsIgnoreCase(role)
                || "FORENSIC_ANALYST".equalsIgnoreCase(role)
                || "VIEWER".equalsIgnoreCase(role);

        if (canViewAll) {
            return caseList.stream().map(CaseResponseDTO::new).toList();
        }

        // INVESTIGATOR role can view cases created by them or assigned to them
        return caseList.stream()
                .filter(c -> userEmail.equalsIgnoreCase(c.getCreatedBy()) || userEmail.equalsIgnoreCase(c.getAssignedInvestigator()))
                .map(CaseResponseDTO::new)
                .toList();
    }

    public CaseDetailDTO getCaseDetail(String caseId, String userEmail, String role) {
        Case caseEntity = caseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case record not found for caseId: " + caseId));

        boolean canViewAll = "ADMIN".equalsIgnoreCase(role)
                || "FORENSIC_ANALYST".equalsIgnoreCase(role)
                || "VIEWER".equalsIgnoreCase(role);

        if (!canViewAll && !userEmail.equalsIgnoreCase(caseEntity.getCreatedBy())
                && !userEmail.equalsIgnoreCase(caseEntity.getAssignedInvestigator())) {
            throw new SecurityException("Unauthorized access: You do not have permission to view case " + caseId);
        }

        List<Evidence> evidenceList = caseEntity.getEvidenceList();

        // Compile full case timeline from case audit logs and associated evidence audit logs
        List<AuditLog> caseAuditLogs = new ArrayList<>(auditLogService.getChainOfCustodyForEvidence(caseId));
        if (evidenceList != null) {
            for (Evidence ev : evidenceList) {
                caseAuditLogs.addAll(auditLogService.getChainOfCustodyForEvidence(ev.getEvidenceId()));
            }
        }
        // Sort chronologically ascending
        caseAuditLogs.sort(Comparator.comparing(AuditLog::getTimestamp));

        return new CaseDetailDTO(caseEntity, evidenceList, caseAuditLogs);
    }

    @Transactional
    public CaseResponseDTO updateCaseStatus(String caseId, UpdateCaseStatusRequest request, String userEmail, String role) {
        Case caseEntity = caseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case record not found for caseId: " + caseId));

        if (request == null || request.getStatus() == null || !CaseStatus.isValid(request.getStatus())) {
            throw new IllegalArgumentException("Valid status is required");
        }

        CaseStatus current = CaseStatus.valueOf(caseEntity.getStatus());
        CaseStatus target = CaseStatus.valueOf(request.getStatus().toUpperCase());

        if (!CaseStatus.isValidTransition(current, target)) {
            throw new IllegalArgumentException("Invalid case status transition from " + current + " to " + target);
        }

        String prevStatusStr = current.name();
        String newStatusStr = target.name();

        caseEntity.setStatus(newStatusStr);
        caseEntity.setUpdatedAt(LocalDateTime.now());
        if (target == CaseStatus.CLOSED) {
            caseEntity.setClosedAt(LocalDateTime.now());
        } else if (current == CaseStatus.CLOSED) {
            caseEntity.setClosedAt(null);
        }

        Case updated = caseRepository.save(caseEntity);

        String action = (target == CaseStatus.CLOSED)
                ? ChainOfCustodyAction.CASE_CLOSED.name()
                : ChainOfCustodyAction.CASE_STATUS_CHANGED.name();

        String details = "Case status updated from " + prevStatusStr + " to " + newStatusStr + " by " + userEmail;
        if (request.getReason() != null && !request.getReason().trim().isBlank()) {
            details += ". Reason: " + request.getReason().trim();
        }

        auditLogService.logCustodyEvent(
                caseId,
                action,
                userEmail,
                role,
                prevStatusStr,
                newStatusStr,
                request.getReason(),
                details
        );

        return new CaseResponseDTO(updated);
    }

    @Transactional
    public CaseResponseDTO assignInvestigator(String caseId, AssignCaseRequest request, String userEmail, String role) {
        Case caseEntity = caseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case record not found for caseId: " + caseId));

        String prevInvestigator = caseEntity.getAssignedInvestigator();
        String newInvestigator = (request != null && request.getAssignedInvestigator() != null)
                ? request.getAssignedInvestigator().trim()
                : null;

        caseEntity.setAssignedInvestigator(newInvestigator);
        caseEntity.setUpdatedAt(LocalDateTime.now());
        Case updated = caseRepository.save(caseEntity);

        String action = (prevInvestigator == null)
                ? ChainOfCustodyAction.CASE_ASSIGNED.name()
                : ChainOfCustodyAction.CASE_REASSIGNED.name();

        auditLogService.logCustodyEvent(
                caseId,
                action,
                userEmail,
                role,
                caseEntity.getStatus(),
                caseEntity.getStatus(),
                null,
                "Investigator " + (prevInvestigator == null ? "assigned to" : "reassigned from " + prevInvestigator + " to") + ": " + newInvestigator
        );

        // Email Notification Trigger: CASE_ASSIGNED / CASE_REASSIGNED
        try {
            if (newInvestigator != null && newInvestigator.contains("@")) {
                boolean sent = emailService.sendCaseAssignedNotification(
                        newInvestigator,
                        updated.getCaseId(),
                        updated.getTitle(),
                        updated.getPriority(),
                        updated.getStatus(),
                        userEmail
                );
                auditLogService.logCustodyEvent(
                        caseId,
                        sent ? ChainOfCustodyAction.EMAIL_NOTIFICATION_SENT.name() : ChainOfCustodyAction.EMAIL_NOTIFICATION_FAILED.name(),
                        userEmail,
                        role,
                        updated.getStatus(),
                        updated.getStatus(),
                        null,
                        (sent ? "Case assignment notification email sent to " : "Failed to send case assignment notification email to ") + newInvestigator
                );
            }
        } catch (Exception e) {
            // Failure to send email must NOT break primary case assignment operation
        }

        return new CaseResponseDTO(updated);
    }

    @Transactional
    public CaseDetailDTO addEvidenceToCase(String caseId, String evidenceId, String userEmail, String role) {
        Case caseEntity = caseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case record not found for caseId: " + caseId));

        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        caseEntity.addEvidence(evidence);
        evidenceRepository.save(evidence);
        caseRepository.save(caseEntity);

        // Audit Event: EVIDENCE_ADDED_TO_CASE
        auditLogService.logCustodyEvent(
                caseId,
                ChainOfCustodyAction.EVIDENCE_ADDED_TO_CASE.name(),
                userEmail,
                role,
                caseEntity.getStatus(),
                caseEntity.getStatus(),
                null,
                "Evidence file '" + evidence.getFileName() + "' (" + evidenceId + ") associated with case " + caseId
        );

        auditLogService.logCustodyEvent(
                evidenceId,
                ChainOfCustodyAction.EVIDENCE_ADDED_TO_CASE.name(),
                userEmail,
                role,
                evidence.getStatus(),
                evidence.getStatus(),
                null,
                "Evidence added to investigation case " + caseId
        );

        return getCaseDetail(caseId, userEmail, role);
    }

    @Transactional
    public CaseDetailDTO removeEvidenceFromCase(String caseId, String evidenceId, String userEmail, String role) {
        Case caseEntity = caseRepository.findByCaseId(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case record not found for caseId: " + caseId));

        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        caseEntity.removeEvidence(evidence);
        evidenceRepository.save(evidence);
        caseRepository.save(caseEntity);

        // Audit Event: EVIDENCE_REMOVED_FROM_CASE
        auditLogService.logCustodyEvent(
                caseId,
                ChainOfCustodyAction.EVIDENCE_REMOVED_FROM_CASE.name(),
                userEmail,
                role,
                caseEntity.getStatus(),
                caseEntity.getStatus(),
                null,
                "Evidence file '" + evidence.getFileName() + "' (" + evidenceId + ") removed from case " + caseId
        );

        auditLogService.logCustodyEvent(
                evidenceId,
                ChainOfCustodyAction.EVIDENCE_REMOVED_FROM_CASE.name(),
                userEmail,
                role,
                evidence.getStatus(),
                evidence.getStatus(),
                null,
                "Evidence removed from investigation case " + caseId
        );

        return getCaseDetail(caseId, userEmail, role);
    }
}
