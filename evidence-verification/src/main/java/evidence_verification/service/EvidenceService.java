package evidence_verification.service;

import evidence_verification.Entity.AuditLog;
import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.ChainOfCustodyAction;
import evidence_verification.Entity.Evidence;
import evidence_verification.Entity.EvidenceStatus;
import evidence_verification.dto.EvidenceDetailDTO;
import evidence_verification.dto.EvidenceResponseDTO;
import evidence_verification.dto.EvidenceVersionDTO;
import evidence_verification.dto.VerificationResponse;
import evidence_verification.repository.EvidenceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final AuditLogService auditLogService;
    private final BlockchainService blockchainService;
    private final EmailService emailService;
    private final EvidenceVersionService evidenceVersionService;

    public EvidenceService(
            EvidenceRepository evidenceRepository,
            AuditLogService auditLogService,
            BlockchainService blockchainService,
            EmailService emailService,
            EvidenceVersionService evidenceVersionService
    ) {
        this.evidenceRepository = evidenceRepository;
        this.auditLogService = auditLogService;
        this.blockchainService = blockchainService;
        this.emailService = emailService;
        this.evidenceVersionService = evidenceVersionService;
    }

    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getAuthorities().stream()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .findFirst()
                    .orElse("USER");
        }
        return "USER";
    }

    public Evidence uploadEvidence(MultipartFile file, String uploadedBy) throws IOException, NoSuchAlgorithmException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String userRole = getCurrentUserRole();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Original filename cannot be empty");
        }
        originalFilename = originalFilename.trim();

        long fileSize = file.getSize();
        if (fileSize <= 0) {
            throw new IllegalArgumentException("File size must be greater than 0 bytes");
        }
        if (fileSize > 50L * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum allowed upload limit of 50MB");
        }

        // Security Sanitization & Path Traversal Check
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\") || originalFilename.contains("\0") || originalFilename.toLowerCase().contains("web-inf")) {
            throw new IllegalArgumentException("Filename contains invalid characters or path traversal sequence.");
        }

        // 1. Derive metadata (extension, MIME type)
        String fileExtension = extractFileExtension(originalFilename);

        // Banned Dangerous Executable File Extension Check
        java.util.Set<String> BANNED_EXTENSIONS = java.util.Set.of(
                "exe", "bat", "cmd", "sh", "vbs", "js", "jar", "msi", "dll", "scr", "php", "py", "asp", "aspx", "jsp", "ps1", "com", "pif"
        );
        if (BANNED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("File extension ." + fileExtension + " is prohibited for security reasons.");
        }

        String mimeType = extractMimeType(file, originalFilename);

        // 2. Generate SHA-256 hash using existing hashing implementation
        String fileHash = calculateSHA256(file.getBytes());

        // 3. Generate a unique Evidence ID
        String evidenceId = "EVI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // 4. Extract original file timestamps if reliably available (else null)
        LocalDateTime createdTimestamp = extractCreatedTimestamp(file);
        LocalDateTime modifiedTimestamp = extractModifiedTimestamp(file);

        // 5. Build and save Evidence entity
        Evidence evidence = new Evidence();
        evidence.setEvidenceId(evidenceId);
        evidence.setFileName(originalFilename);
        evidence.setFileExtension(fileExtension);
        evidence.setFileType(mimeType);
        evidence.setFileSize(fileSize);
        evidence.setFileHash(fileHash);
        evidence.setUploadedBy(uploadedBy);
        evidence.setStatus(EvidenceStatus.UPLOADED.name());
        evidence.setUploadedAt(LocalDateTime.now());
        evidence.setCreatedTimestamp(createdTimestamp);
        evidence.setModifiedTimestamp(modifiedTimestamp);

        Evidence savedEvidence = evidenceRepository.save(evidence);

        // Ensure Initial Version 1 record exists in evidence_versions table
        evidenceVersionService.ensureVersion1Exists(savedEvidence);

        // Chain of Custody Event 1: EVIDENCE_UPLOADED
        auditLogService.logCustodyEvent(
                savedEvidence.getEvidenceId(),
                ChainOfCustodyAction.EVIDENCE_UPLOADED.name(),
                uploadedBy,
                userRole,
                null,
                EvidenceStatus.UPLOADED.name(),
                null,
                "File '" + savedEvidence.getFileName() + "' (Ext: " + (savedEvidence.getFileExtension() != null ? savedEvidence.getFileExtension() : "none")
                        + ", Size: " + savedEvidence.getFileSize() + " B, MIME: " + savedEvidence.getFileType()
                        + ") uploaded to digital evidence repository and metadata extracted"
        );

        // Chain of Custody Event 2: HASH_GENERATED
        auditLogService.logCustodyEvent(
                savedEvidence.getEvidenceId(),
                ChainOfCustodyAction.HASH_GENERATED.name(),
                uploadedBy,
                userRole,
                EvidenceStatus.UPLOADED.name(),
                EvidenceStatus.UPLOADED.name(),
                null,
                "Cryptographic SHA-256 fingerprint generated: " + fileHash
        );

        // 4. Anchor evidence hash to blockchain
        BlockchainRecord blockchainRecord = blockchainService.anchorHash(
                savedEvidence.getEvidenceId(),
                savedEvidence.getFileHash()
        );

        // Chain of Custody Event 3: BLOCKCHAIN_ANCHORED
        if (blockchainRecord != null && "CONFIRMED".equalsIgnoreCase(blockchainRecord.getStatus())) {
            auditLogService.logCustodyEvent(
                    savedEvidence.getEvidenceId(),
                    ChainOfCustodyAction.BLOCKCHAIN_ANCHORED.name(),
                    uploadedBy,
                    userRole,
                    EvidenceStatus.UPLOADED.name(),
                    EvidenceStatus.UPLOADED.name(),
                    null,
                    "Evidence hash anchored on Polygon Amoy blockchain ledger. Transaction: " + blockchainRecord.getTransactionHash()
            );
        } else {
            String statusDetails = blockchainRecord != null ? blockchainRecord.getStatus() : "FAILED";
            auditLogService.logCustodyEvent(
                    savedEvidence.getEvidenceId(),
                    "BLOCKCHAIN_ANCHORING_FAILED",
                    uploadedBy,
                    userRole,
                    EvidenceStatus.UPLOADED.name(),
                    EvidenceStatus.UPLOADED.name(),
                    null,
                    "Evidence hash anchoring failed with status: " + statusDetails
            );
        }

        // Email Notification Trigger: EVIDENCE_UPLOADED
        try {
            if (uploadedBy != null && uploadedBy.contains("@")) {
                boolean sent = emailService.sendEvidenceUploadedNotification(
                        uploadedBy,
                        savedEvidence.getEvidenceId(),
                        savedEvidence.getCaseId(),
                        savedEvidence.getFileName(),
                        savedEvidence.getUploadedBy(),
                        savedEvidence.getUploadedAt(),
                        savedEvidence.getStatus()
                );
                auditLogService.logCustodyEvent(
                        savedEvidence.getEvidenceId(),
                        sent ? ChainOfCustodyAction.EMAIL_NOTIFICATION_SENT.name() : ChainOfCustodyAction.EMAIL_NOTIFICATION_FAILED.name(),
                        "SYSTEM",
                        "SYSTEM",
                        null,
                        null,
                        null,
                        (sent ? "Upload notification email sent to " : "Failed to send upload notification email to ") + uploadedBy
                );
            }
        } catch (Exception e) {
            // Failure to send email must NOT break primary evidence upload operation
        }

        return savedEvidence;
    }

    public List<EvidenceResponseDTO> getUserEvidence(String uploadedBy) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean canViewAll = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_FORENSIC_ANALYST")
                        || a.getAuthority().equals("ROLE_VIEWER"));

        if (canViewAll) {
            return evidenceRepository.findAll().stream()
                    .map(EvidenceResponseDTO::new)
                    .toList();
        }

        return evidenceRepository.findByUploadedBy(uploadedBy).stream()
                .map(EvidenceResponseDTO::new)
                .toList();
    }

    public EvidenceDetailDTO getEvidenceDetail(String evidenceId, String requestedBy) {
        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean canViewAll = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_FORENSIC_ANALYST")
                        || a.getAuthority().equals("ROLE_VIEWER"));

        if (!canViewAll && !evidence.getUploadedBy().equalsIgnoreCase(requestedBy)) {
            throw new SecurityException("Unauthorized access: You do not have permission to view this evidence record.");
        }

        BlockchainRecord blockchainRecord = blockchainService.getRecord(evidenceId).orElse(null);
        List<AuditLog> auditLogs = auditLogService.getChainOfCustodyForEvidence(evidenceId);
        List<EvidenceVersionDTO> versions = evidenceVersionService.getVersionsForEvidence(evidenceId);

        return new EvidenceDetailDTO(evidence, blockchainRecord, auditLogs, versions);
    }

    public Evidence startReview(String evidenceId, String performedBy) {
        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        EvidenceStatus current = EvidenceStatus.valueOf(evidence.getStatus());
        if (!EvidenceStatus.isValidTransition(current, EvidenceStatus.UNDER_REVIEW)) {
            throw new IllegalArgumentException("Invalid status transition from " + current + " to UNDER_REVIEW");
        }

        String userRole = getCurrentUserRole();
        evidence.setStatus(EvidenceStatus.UNDER_REVIEW.name());
        evidence.setReviewStartedAt(LocalDateTime.now());
        Evidence updated = evidenceRepository.save(evidence);

        auditLogService.logCustodyEvent(
                evidenceId,
                ChainOfCustodyAction.REVIEW_STARTED.name(),
                performedBy,
                userRole,
                current.name(),
                EvidenceStatus.UNDER_REVIEW.name(),
                null,
                "Forensic examination initiated by " + performedBy + " (" + userRole + ")"
        );

        return updated;
    }

    public Evidence rejectEvidence(String evidenceId, String reason, String performedBy) {
        if (reason == null || reason.trim().isBlank() || reason.trim().length() < 5) {
            throw new IllegalArgumentException("A valid rejection reason (minimum 5 characters) is required.");
        }

        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        EvidenceStatus current = EvidenceStatus.valueOf(evidence.getStatus());
        if (!EvidenceStatus.isValidTransition(current, EvidenceStatus.REJECTED)) {
            throw new IllegalArgumentException("Invalid status transition from " + current + " to REJECTED");
        }

        String userRole = getCurrentUserRole();
        evidence.setStatus(EvidenceStatus.REJECTED.name());
        evidence.setRejectionReason(reason.trim());
        evidence.setReviewedBy(performedBy);
        evidence.setReviewedAt(LocalDateTime.now());
        Evidence updated = evidenceRepository.save(evidence);

        auditLogService.logCustodyEvent(
                evidenceId,
                ChainOfCustodyAction.EVIDENCE_REJECTED.name(),
                performedBy,
                userRole,
                current.name(),
                EvidenceStatus.REJECTED.name(),
                reason.trim(),
                "Evidence rejected by " + performedBy + " (" + userRole + "). Reason: " + reason.trim()
        );

        // Email Notification Trigger: EVIDENCE_REJECTED
        try {
            String recipient = updated.getUploadedBy();
            if (recipient != null && recipient.contains("@")) {
                boolean sent = emailService.sendEvidenceRejectedNotification(
                        recipient,
                        evidenceId,
                        updated.getCaseId(),
                        updated.getFileName(),
                        reason.trim(),
                        updated.getReviewedAt()
                );
                auditLogService.logCustodyEvent(
                        evidenceId,
                        sent ? ChainOfCustodyAction.EMAIL_NOTIFICATION_SENT.name() : ChainOfCustodyAction.EMAIL_NOTIFICATION_FAILED.name(),
                        performedBy,
                        userRole,
                        current.name(),
                        EvidenceStatus.REJECTED.name(),
                        null,
                        (sent ? "Rejection notification email sent to " : "Failed to send rejection notification email to ") + recipient
                );
            }
        } catch (Exception e) {
            // Failure to send email must NOT break primary evidence rejection operation
        }

        return updated;
    }

    public VerificationResponse verifyEvidence(MultipartFile file, String performedBy) throws IOException, NoSuchAlgorithmException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String userRole = getCurrentUserRole();

        // 1. Calculate SHA-256 hash of uploaded file
        String calculatedHash = calculateSHA256(file.getBytes());
        String fileName = file.getOriginalFilename();

        // 2. Query PostgreSQL database for matching evidence by file hash
        Optional<Evidence> evidenceOpt = evidenceRepository.findByFileHash(calculatedHash);

        if (evidenceOpt.isEmpty()) {
            String message = "Verification failed: No matching evidence record found in PostgreSQL database.";
            auditLogService.logCustodyEvent(
                    "UNKNOWN",
                    ChainOfCustodyAction.VERIFICATION_FAILED.name(),
                    performedBy,
                    userRole,
                    null,
                    null,
                    "Unregistered file verification attempt",
                    message
            );

            return new VerificationResponse(
                    "N/A",
                    fileName,
                    calculatedHash,
                    null,
                    null,
                    "NOT VERIFIED",
                    message
            );
        }

        Evidence evidence = evidenceOpt.get();
        String evidenceId = evidence.getEvidenceId();
        String storedHash = evidence.getFileHash();
        String previousStatus = evidence.getStatus();

        // 3. Query Blockchain for corresponding record
        Optional<BlockchainRecord> bcRecordOpt = blockchainService.getRecord(evidenceId);

        if (bcRecordOpt.isEmpty()) {
            String message = "Verification failed: Database hash matched, but no corresponding Blockchain record was found for evidence ID: " + evidenceId;
            auditLogService.logCustodyEvent(
                    evidenceId,
                    ChainOfCustodyAction.VERIFICATION_FAILED.name(),
                    performedBy,
                    userRole,
                    previousStatus,
                    previousStatus,
                    "Missing blockchain record",
                    message
            );

            return new VerificationResponse(
                    evidenceId,
                    fileName,
                    calculatedHash,
                    storedHash,
                    null,
                    "NOT VERIFIED",
                    message
            );
        }

        BlockchainRecord bcRecord = bcRecordOpt.get();
        String blockchainHash = bcRecord.getFileHash();

        // 4. Compare calculated hash against database hash AND blockchain hash
        boolean dbMatch = calculatedHash.equalsIgnoreCase(storedHash);
        boolean bcMatch = calculatedHash.equalsIgnoreCase(blockchainHash);

        if (dbMatch && bcMatch) {
            String message = "Evidence verified successfully against both PostgreSQL database and Blockchain record.";
            boolean alreadyVerified = EvidenceStatus.VERIFIED.name().equalsIgnoreCase(previousStatus);
            String action = alreadyVerified ? ChainOfCustodyAction.RE_VERIFICATION_PERFORMED.name() : ChainOfCustodyAction.EVIDENCE_VERIFIED.name();

            evidence.setStatus(EvidenceStatus.VERIFIED.name());
            evidence.setReviewedBy(performedBy);
            evidence.setReviewedAt(LocalDateTime.now());
            evidenceRepository.save(evidence);

            auditLogService.logCustodyEvent(
                    evidenceId,
                    action,
                    performedBy,
                    userRole,
                    previousStatus,
                    EvidenceStatus.VERIFIED.name(),
                    null,
                    message
            );

            // Email Notification Trigger: EVIDENCE_VERIFIED
            try {
                String recipient = evidence.getUploadedBy();
                if (recipient != null && recipient.contains("@")) {
                    boolean sent = emailService.sendEvidenceVerifiedNotification(
                            recipient,
                            evidenceId,
                            evidence.getCaseId(),
                            calculatedHash,
                            evidence.getReviewedAt(),
                            EvidenceStatus.VERIFIED.name()
                    );
                    auditLogService.logCustodyEvent(
                            evidenceId,
                            sent ? ChainOfCustodyAction.EMAIL_NOTIFICATION_SENT.name() : ChainOfCustodyAction.EMAIL_NOTIFICATION_FAILED.name(),
                            performedBy,
                            userRole,
                            previousStatus,
                            EvidenceStatus.VERIFIED.name(),
                            null,
                            (sent ? "Verification completed notification email sent to " : "Failed to send verification completed notification email to ") + recipient
                    );
                }
            } catch (Exception e) {
                // Failure to send email must NOT break primary evidence verification operation
            }

            return new VerificationResponse(
                    evidenceId,
                    fileName,
                    calculatedHash,
                    storedHash,
                    blockchainHash,
                    "VERIFIED",
                    message
            );
        } else {
            String message;
            if (!dbMatch && !bcMatch) {
                message = "Verification failed: Both database hash and blockchain hash mismatched (Tampering detected).";
            } else if (!dbMatch) {
                message = "Verification failed: PostgreSQL database hash mismatched (Tampering detected).";
            } else {
                message = "Verification failed: Blockchain hash mismatched (Tampering detected on-chain).";
            }

            // CRITICAL INTEGRITY MISMATCH: Update status to TAMPERED (do NOT modify original stored hashes)
            evidence.setStatus(EvidenceStatus.TAMPERED.name());
            evidenceRepository.save(evidence);

            // Audit Event 1: VERIFICATION_FAILED
            auditLogService.logCustodyEvent(
                    evidenceId,
                    ChainOfCustodyAction.VERIFICATION_FAILED.name(),
                    performedBy,
                    userRole,
                    previousStatus,
                    EvidenceStatus.TAMPERED.name(),
                    "Cryptographic hash mismatch",
                    "CRITICAL ALERT: " + message
            );

            // Audit Event 2: INTEGRITY_COMPROMISED
            auditLogService.logCustodyEvent(
                    evidenceId,
                    ChainOfCustodyAction.INTEGRITY_COMPROMISED.name(),
                    performedBy,
                    userRole,
                    previousStatus,
                    EvidenceStatus.TAMPERED.name(),
                    "Cryptographic hash mismatch detected",
                    "CRITICAL ALERT: Evidence integrity compromised. File Hash: " + calculatedHash + " | Database Hash: " + storedHash + " | Blockchain Hash: " + blockchainHash
            );

            // Email Notification Trigger: EVIDENCE_TAMPERED (Prevent Duplicate Spam if already TAMPERED)
            boolean isNewlyTampered = !EvidenceStatus.TAMPERED.name().equalsIgnoreCase(previousStatus);
            if (isNewlyTampered) {
                try {
                    String recipient = evidence.getUploadedBy();
                    if (recipient != null && recipient.contains("@")) {
                        boolean sent = emailService.sendEvidenceTamperedAlert(
                                recipient,
                                evidenceId,
                                evidence.getCaseId(),
                                fileName,
                                calculatedHash,
                                LocalDateTime.now()
                        );
                        auditLogService.logCustodyEvent(
                                evidenceId,
                                sent ? ChainOfCustodyAction.EMAIL_NOTIFICATION_SENT.name() : ChainOfCustodyAction.EMAIL_NOTIFICATION_FAILED.name(),
                                performedBy,
                                userRole,
                                previousStatus,
                                EvidenceStatus.TAMPERED.name(),
                                "Cryptographic hash mismatch alert",
                                (sent ? "Integrity alert email sent to " : "Failed to send integrity alert email to ") + recipient
                        );
                    }
                } catch (Exception e) {
                    // Failure to send email must NOT break primary evidence verification operation
                }
            }

            return new VerificationResponse(
                    evidenceId,
                    fileName,
                    calculatedHash,
                    storedHash,
                    blockchainHash,
                    "TAMPERED",
                    message
            );
        }
    }

    public VerificationResponse verifyEvidence(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        return verifyEvidence(file, "Anonymous");
    }

    private String calculateSHA256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        return HexFormat.of().formatHex(hash);
    }

    private String extractFileExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        String cleanName = filename.trim();
        int lastDot = cleanName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < cleanName.length() - 1) {
            return cleanName.substring(lastDot + 1).toLowerCase();
        }
        return null;
    }

    private String extractMimeType(MultipartFile file, String filename) {
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank() && !"application/octet-stream".equalsIgnoreCase(contentType)) {
            return contentType.trim();
        }
        if (filename != null && !filename.isBlank()) {
            String guessed = java.net.URLConnection.guessContentTypeFromName(filename);
            if (guessed != null && !guessed.isBlank()) {
                return guessed;
            }
        }
        return (contentType != null && !contentType.isBlank()) ? contentType.trim() : "application/octet-stream";
    }

    private LocalDateTime extractCreatedTimestamp(MultipartFile file) {
        // HTTP multipart file upload payload does not reliably transmit original client OS created timestamp.
        // Return null to avoid fake or invented timestamps.
        return null;
    }

    private LocalDateTime extractModifiedTimestamp(MultipartFile file) {
        // HTTP multipart file upload payload does not reliably transmit original client OS modified timestamp.
        // Return null to avoid fake or invented timestamps.
        return null;
    }
}
