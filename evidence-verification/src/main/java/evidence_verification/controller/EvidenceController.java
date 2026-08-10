package evidence_verification.controller;

import evidence_verification.Entity.AuditLog;
import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.Evidence;
import evidence_verification.dto.EvidenceDetailDTO;
import evidence_verification.dto.EvidenceResponseDTO;
import evidence_verification.dto.VerificationResponse;
import evidence_verification.service.AuditLogService;
import evidence_verification.service.BlockchainService;
import evidence_verification.service.EvidenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final AuditLogService auditLogService;
    private final BlockchainService blockchainService;

    public EvidenceController(
            EvidenceService evidenceService,
            AuditLogService auditLogService,
            BlockchainService blockchainService) {
        this.evidenceService = evidenceService;
        this.auditLogService = auditLogService;
        this.blockchainService = blockchainService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadEvidence(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String uploadedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            Evidence savedEvidence = evidenceService.uploadEvidence(file, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEvidence);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload evidence: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserEvidence() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String uploadedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            List<EvidenceResponseDTO> list = evidenceService.getUserEvidence(uploadedBy);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch user evidence records: " + e.getMessage()));
        }
    }

    @GetMapping("/{evidenceId}")
    public ResponseEntity<?> getEvidenceDetail(@PathVariable("evidenceId") String evidenceId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String requestedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            EvidenceDetailDTO detail = evidenceService.getEvidenceDetail(evidenceId, requestedBy);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch evidence details: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyEvidence(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String performedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            VerificationResponse response = evidenceService.verifyEvidence(file, performedBy);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify evidence: " + e.getMessage()));
        }
    }

    @GetMapping("/{evidenceId}/audit-logs")
    public ResponseEntity<?> getAuditLogs(@PathVariable("evidenceId") String evidenceId) {
        try {
            List<AuditLog> auditLogs = auditLogService.getAuditLogsForEvidence(evidenceId);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch audit logs: " + e.getMessage()));
        }
    }

    @GetMapping("/{evidenceId}/blockchain")
    public ResponseEntity<?> getBlockchainRecord(@PathVariable("evidenceId") String evidenceId) {
        try {
            Optional<BlockchainRecord> recordOpt = blockchainService.getRecord(evidenceId);
            if (recordOpt.isPresent()) {
                return ResponseEntity.ok(recordOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Blockchain record not found for evidenceId: " + evidenceId));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch blockchain record: " + e.getMessage()));
        }
    }
}
