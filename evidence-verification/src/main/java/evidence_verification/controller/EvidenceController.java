package evidence_verification.controller;

import evidence_verification.Entity.AuditLog;
import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.Evidence;
import evidence_verification.dto.EvidenceDetailDTO;
import evidence_verification.dto.EvidenceResponseDTO;
import evidence_verification.dto.RejectEvidenceRequest;
import evidence_verification.dto.VerificationResponse;
import evidence_verification.dto.EvidenceNoteDTO;
import evidence_verification.dto.NoteRequestDTO;
import evidence_verification.service.AuditLogService;
import evidence_verification.service.BlockchainService;
import evidence_verification.service.EvidenceNoteService;
import evidence_verification.service.EvidenceService;
import evidence_verification.service.PdfReportService;
import evidence_verification.service.QrCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import evidence_verification.dto.EvidenceVersionDTO;
import evidence_verification.service.EvidenceVersionService;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final AuditLogService auditLogService;
    private final BlockchainService blockchainService;
    private final PdfReportService pdfReportService;
    private final QrCodeService qrCodeService;
    private final EvidenceNoteService evidenceNoteService;
    private final EvidenceVersionService evidenceVersionService;

    public EvidenceController(
            EvidenceService evidenceService,
            AuditLogService auditLogService,
            BlockchainService blockchainService,
            PdfReportService pdfReportService,
            QrCodeService qrCodeService,
            EvidenceNoteService evidenceNoteService,
            EvidenceVersionService evidenceVersionService) {
        this.evidenceService = evidenceService;
        this.auditLogService = auditLogService;
        this.blockchainService = blockchainService;
        this.pdfReportService = pdfReportService;
        this.qrCodeService = qrCodeService;
        this.evidenceNoteService = evidenceNoteService;
        this.evidenceVersionService = evidenceVersionService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR')")
    public ResponseEntity<?> uploadEvidence(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String uploadedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            Evidence savedEvidence = evidenceService.uploadEvidence(file, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(new EvidenceResponseDTO(savedEvidence));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload evidence: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getEvidenceDetail(@PathVariable("evidenceId") String evidenceId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String requestedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            EvidenceDetailDTO detail = evidenceService.getEvidenceDetail(evidenceId, requestedBy);

            // Log EVIDENCE_ACCESSED with 5-minute deduplication check
            String userRole = (authentication != null && !authentication.getAuthorities().isEmpty())
                    ? authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                    : "USER";
            auditLogService.logAccessEvent(evidenceId, requestedBy, userRole);

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

    @PostMapping("/{evidenceId}/review/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORENSIC_ANALYST')")
    public ResponseEntity<?> startReview(@PathVariable("evidenceId") String evidenceId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String performedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            Evidence updated = evidenceService.startReview(evidenceId, performedBy);
            return ResponseEntity.ok(new EvidenceResponseDTO(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start review: " + e.getMessage()));
        }
    }

    @PostMapping("/{evidenceId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORENSIC_ANALYST')")
    public ResponseEntity<?> rejectEvidence(@PathVariable("evidenceId") String evidenceId, @RequestBody RejectEvidenceRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String performedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            String reason = request != null ? request.getReason() : null;
            Evidence updated = evidenceService.rejectEvidence(evidenceId, reason, performedBy);
            return ResponseEntity.ok(new EvidenceResponseDTO(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reject evidence: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getAuditLogs(@PathVariable("evidenceId") String evidenceId) {
        try {
            List<AuditLog> auditLogs = auditLogService.getAuditLogsForEvidence(evidenceId);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch audit logs: " + e.getMessage()));
        }
    }

    @GetMapping("/{evidenceId}/chain-of-custody")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getChainOfCustody(@PathVariable("evidenceId") String evidenceId) {
        try {
            List<AuditLog> auditLogs = auditLogService.getChainOfCustodyForEvidence(evidenceId);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch chain of custody: " + e.getMessage()));
        }
    }

    @GetMapping("/{evidenceId}/status-history")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getStatusHistory(@PathVariable("evidenceId") String evidenceId) {
        try {
            List<AuditLog> auditLogs = auditLogService.getAuditLogsForEvidence(evidenceId);
            return ResponseEntity.ok(auditLogs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch status history: " + e.getMessage()));
        }
    }

    @GetMapping("/{evidenceId}/blockchain")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
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

    @GetMapping("/{evidenceId}/verification-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getVerificationReport(@PathVariable("evidenceId") String evidenceId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String requestedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            byte[] pdfBytes = pdfReportService.generateVerificationReport(evidenceId, requestedBy);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Verification_Report_" + evidenceId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate verification report: " + e.getMessage()));
        }
    }

    @GetMapping("/{evidenceId}/qr")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getEvidenceQrCode(@PathVariable("evidenceId") String evidenceId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String requestedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            byte[] qrImageBytes = qrCodeService.generateEvidenceQrCode(evidenceId, requestedBy);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setCacheControl("max-age=3600, must-revalidate");

            return new ResponseEntity<>(qrImageBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate QR code: " + e.getMessage()));
        }
    }

    @PostMapping("/{evidenceId}/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST')")
    public ResponseEntity<?> addNote(
            @PathVariable("evidenceId") String evidenceId,
            @RequestBody NoteRequestDTO request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String author = (authentication != null) ? authentication.getName() : "Anonymous";

            EvidenceNoteDTO note = evidenceNoteService.addNote(evidenceId, request, author);
            return ResponseEntity.status(HttpStatus.CREATED).body(note);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add investigator note: " + e.getMessage()));
        }
    }

    @GetMapping("/{evidenceId}/notes")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getNotes(@PathVariable("evidenceId") String evidenceId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String requestedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            List<EvidenceNoteDTO> notes = evidenceNoteService.getNotesForEvidence(evidenceId, requestedBy);
            return ResponseEntity.ok(notes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch investigator notes: " + e.getMessage()));
        }
    }

    @PutMapping("/{evidenceId}/notes/{noteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST')")
    public ResponseEntity<?> updateNote(
            @PathVariable("evidenceId") String evidenceId,
            @PathVariable("noteId") String noteId,
            @RequestBody NoteRequestDTO request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String requestedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            EvidenceNoteDTO updated = evidenceNoteService.updateNote(evidenceId, noteId, request, requestedBy);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update investigator note: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{evidenceId}/notes/{noteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST')")
    public ResponseEntity<?> deleteNote(
            @PathVariable("evidenceId") String evidenceId,
            @PathVariable("noteId") String noteId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String requestedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            evidenceNoteService.deleteNote(evidenceId, noteId, requestedBy);
            return ResponseEntity.ok(Map.of("message", "Investigator note deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete investigator note: " + e.getMessage()));
        }
    }

    @PostMapping("/{evidenceId}/versions")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR')")
    public ResponseEntity<?> uploadNewVersion(
            @PathVariable("evidenceId") String evidenceId,
            @RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String uploadedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            EvidenceVersionDTO dto = evidenceVersionService.uploadNewVersion(evidenceId, file, uploadedBy);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload new evidence version: " + e.getMessage()));
        }
    }

    @GetMapping("/{evidenceId}/versions")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getVersions(@PathVariable("evidenceId") String evidenceId) {
        try {
            List<EvidenceVersionDTO> versions = evidenceVersionService.getVersionsForEvidence(evidenceId);
            return ResponseEntity.ok(versions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch evidence versions: " + e.getMessage()));
        }
    }

    @GetMapping("/{evidenceId}/versions/{versionNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getVersion(
            @PathVariable("evidenceId") String evidenceId,
            @PathVariable("versionNumber") Integer versionNumber) {
        try {
            EvidenceVersionDTO version = evidenceVersionService.getVersion(evidenceId, versionNumber);
            return ResponseEntity.ok(version);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch version details: " + e.getMessage()));
        }
    }

    @PostMapping("/{evidenceId}/versions/{versionNumber}/verify")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST')")
    public ResponseEntity<?> verifyVersion(
            @PathVariable("evidenceId") String evidenceId,
            @PathVariable("versionNumber") Integer versionNumber,
            @RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String performedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            VerificationResponse response = evidenceVersionService.verifyVersion(evidenceId, versionNumber, file, performedBy);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to verify evidence version: " + e.getMessage()));
        }
    }

    @GetMapping("/{evidenceId}/versions/{versionNumber}/verification-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getVersionVerificationReport(
            @PathVariable("evidenceId") String evidenceId,
            @PathVariable("versionNumber") Integer versionNumber) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String requestedBy = (authentication != null) ? authentication.getName() : "Anonymous";

            byte[] pdfBytes = pdfReportService.generateVerificationReport(evidenceId, versionNumber, requestedBy);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Verification_Report_" + evidenceId + "_v" + versionNumber + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate version verification report: " + e.getMessage()));
        }
    }
}
