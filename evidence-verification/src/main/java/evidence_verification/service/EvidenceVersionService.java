package evidence_verification.service;

import evidence_verification.Entity.*;
import evidence_verification.dto.EvidenceVersionDTO;
import evidence_verification.dto.VerificationResponse;
import evidence_verification.repository.EvidenceRepository;
import evidence_verification.repository.EvidenceVersionRepository;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EvidenceVersionService {

    private final EvidenceVersionRepository evidenceVersionRepository;
    private final EvidenceRepository evidenceRepository;
    private final AuditLogService auditLogService;
    private final BlockchainService blockchainService;
    private final EmailService emailService;

    private static final List<String> BANNED_EXTENSIONS = Arrays.asList(
            "exe", "bat", "cmd", "sh", "vbs", "js", "jar", "msi", "dll", "scr", "php", "py", "asp", "aspx", "jsp", "ps1"
    );

    public EvidenceVersionService(
            EvidenceVersionRepository evidenceVersionRepository,
            EvidenceRepository evidenceRepository,
            AuditLogService auditLogService,
            BlockchainService blockchainService,
            EmailService emailService) {
        this.evidenceVersionRepository = evidenceVersionRepository;
        this.evidenceRepository = evidenceRepository;
        this.auditLogService = auditLogService;
        this.blockchainService = blockchainService;
        this.emailService = emailService;
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

    private String calculateSHA256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedhash = digest.digest(data);
        StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
        for (byte b : encodedhash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private LocalDateTime parseExifDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        String cleaned = dateStr.trim().replaceAll("\u0000", "");
        String[] formats = {
                "yyyy:MM:dd HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy:MM:dd HH:mm:ss.SSS"
        };

        for (String fmt : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fmt);
                return LocalDateTime.parse(cleaned, formatter);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @Transactional
    public EvidenceVersion ensureVersion1Exists(Evidence evidence) {
        String versionId = evidence.getEvidenceId() + "-V1";

        Optional<EvidenceVersion> existingByVersionId = evidenceVersionRepository.findByVersionId(versionId);
        if (existingByVersionId.isPresent()) {
            return existingByVersionId.get();
        }

        List<EvidenceVersion> existing = evidenceVersionRepository.findByEvidence_EvidenceIdOrderByVersionNumberDesc(evidence.getEvidenceId());
        if (!existing.isEmpty()) {
            for (EvidenceVersion v : existing) {
                if (v.getVersionNumber() == 1) {
                    return v;
                }
            }
            return existing.get(existing.size() - 1);
        }

        EvidenceVersion v1 = new EvidenceVersion(
                versionId,
                evidence,
                1,
                evidence.getFileName(),
                evidence.getFileExtension(),
                evidence.getFileType(),
                evidence.getFileSize(),
                evidence.getFileHash(),
                evidence.getUploadedBy(),
                evidence.getCreatedTimestamp(),
                evidence.getModifiedTimestamp()
        );

        if (evidence.getUploadedAt() != null) {
            v1.setUploadedAt(evidence.getUploadedAt());
        }
        if (evidence.getStatus() != null) {
            v1.setStatus(evidence.getStatus());
        }
        v1.setRejectionReason(evidence.getRejectionReason());
        v1.setReviewedBy(evidence.getReviewedBy());
        v1.setReviewedAt(evidence.getReviewedAt());
        v1.setReviewStartedAt(evidence.getReviewStartedAt());

        EvidenceVersion savedV1 = evidenceVersionRepository.save(v1);

        // Ensure blockchain anchor exists for version 1
        blockchainService.getRecord(versionId).orElseGet(() ->
                blockchainService.anchorHash(versionId, evidence.getFileHash())
        );

        return savedV1;
    }

    @Transactional
    public EvidenceVersionDTO uploadNewVersion(String evidenceId, MultipartFile file, String uploadedBy) throws IOException, NoSuchAlgorithmException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Evidence file cannot be empty.");
        }

        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum permitted limit of 50MB.");
        }

        String rawFileName = file.getOriginalFilename();
        if (rawFileName != null) {
            String lowerName = rawFileName.toLowerCase();
            if (lowerName.contains("..") || lowerName.contains("/") || lowerName.contains("\\")
                    || lowerName.contains("%00") || lowerName.contains("web-inf")) {
                throw new IllegalArgumentException("Illegal characters or path traversal attempt detected in filename.");
            }
        }

        String extension = extractExtension(rawFileName);
        if (BANNED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Prohibited executable or dangerous file extension detected: ." + extension);
        }

        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        ensureVersion1Exists(evidence);

        Optional<EvidenceVersion> latestOpt = evidenceVersionRepository.findTopByEvidence_EvidenceIdOrderByVersionNumberDesc(evidenceId);
        int nextVersionNumber = latestOpt.map(v -> v.getVersionNumber() + 1).orElse(2);
        String versionId = evidenceId + "-V" + nextVersionNumber;

        while (evidenceVersionRepository.findByVersionId(versionId).isPresent()) {
            nextVersionNumber++;
            versionId = evidenceId + "-V" + nextVersionNumber;
        }

        byte[] fileBytes = file.getBytes();
        String fileHash = calculateSHA256(fileBytes);
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        LocalDateTime createdTimestamp = LocalDateTime.now();
        LocalDateTime modifiedTimestamp = LocalDateTime.now();

        EvidenceVersion newVersion = new EvidenceVersion(
                versionId,
                evidence,
                nextVersionNumber,
                rawFileName,
                extension,
                contentType,
                file.getSize(),
                fileHash,
                uploadedBy,
                createdTimestamp,
                modifiedTimestamp
        );

        EvidenceVersion savedVersion = evidenceVersionRepository.save(newVersion);

        // Update logical Evidence status & hash to reflect current latest version
        evidence.setFileName(rawFileName);
        evidence.setFileExtension(extension);
        evidence.setFileType(contentType);
        evidence.setFileSize(file.getSize());
        evidence.setFileHash(fileHash);
        evidence.setStatus(EvidenceStatus.UPLOADED.name());
        evidenceRepository.save(evidence);

        String userRole = getCurrentUserRole();

        // Chain of Custody Event 1: EVIDENCE_VERSION_UPLOADED
        auditLogService.logCustodyEvent(
                evidenceId,
                ChainOfCustodyAction.EVIDENCE_VERSION_UPLOADED.name(),
                uploadedBy,
                userRole,
                EvidenceStatus.UPLOADED.name(),
                EvidenceStatus.UPLOADED.name(),
                null,
                "New evidence Version " + nextVersionNumber + " uploaded (" + rawFileName + "). Version ID: " + versionId
        );

        // Chain of Custody Event 2: HASH_GENERATED
        auditLogService.logCustodyEvent(
                evidenceId,
                ChainOfCustodyAction.HASH_GENERATED.name(),
                uploadedBy,
                userRole,
                EvidenceStatus.UPLOADED.name(),
                EvidenceStatus.UPLOADED.name(),
                null,
                "Cryptographic SHA-256 fingerprint generated for Version " + nextVersionNumber + ": " + fileHash
        );

        // Anchor version hash to Polygon Amoy blockchain
        BlockchainRecord bcRecord = blockchainService.anchorHash(versionId, fileHash);

        if (bcRecord != null && "CONFIRMED".equalsIgnoreCase(bcRecord.getStatus())) {
            auditLogService.logCustodyEvent(
                    evidenceId,
                    ChainOfCustodyAction.BLOCKCHAIN_ANCHORED.name(),
                    uploadedBy,
                    userRole,
                    EvidenceStatus.UPLOADED.name(),
                    EvidenceStatus.UPLOADED.name(),
                    null,
                    "Evidence Version " + nextVersionNumber + " hash anchored on Polygon Amoy blockchain. Transaction: " + bcRecord.getTransactionHash()
            );
        }

        // Trigger upload notification
        try {
            if (uploadedBy != null && uploadedBy.contains("@")) {
                emailService.sendEvidenceUploadedNotification(
                        uploadedBy,
                        evidenceId,
                        evidence.getCaseId(),
                        rawFileName + " (v" + nextVersionNumber + ")",
                        uploadedBy,
                        savedVersion.getUploadedAt(),
                        savedVersion.getStatus()
                );
            }
        } catch (Exception ignored) {
        }

        return new EvidenceVersionDTO(savedVersion, bcRecord);
    }

    public List<EvidenceVersionDTO> getVersionsForEvidence(String evidenceId) {
        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        ensureVersion1Exists(evidence);

        List<EvidenceVersion> versions = evidenceVersionRepository.findByEvidence_EvidenceIdOrderByVersionNumberDesc(evidenceId);
        List<EvidenceVersionDTO> dtoList = new ArrayList<>();

        for (EvidenceVersion v : versions) {
            BlockchainRecord bcRecord = blockchainService.getRecord(v.getVersionId())
                    .orElseGet(() -> blockchainService.getRecord(evidenceId).orElse(null));
            dtoList.add(new EvidenceVersionDTO(v, bcRecord));
        }

        return dtoList;
    }

    public EvidenceVersionDTO getVersion(String evidenceId, Integer versionNumber) {
        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        ensureVersion1Exists(evidence);

        EvidenceVersion version = evidenceVersionRepository.findByEvidence_EvidenceIdAndVersionNumber(evidenceId, versionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Version " + versionNumber + " not found for evidenceId: " + evidenceId));

        BlockchainRecord bcRecord = blockchainService.getRecord(version.getVersionId())
                .orElseGet(() -> blockchainService.getRecord(evidenceId).orElse(null));

        return new EvidenceVersionDTO(version, bcRecord);
    }

    @Transactional
    public VerificationResponse verifyVersion(String evidenceId, Integer versionNumber, MultipartFile file, String performedBy) throws IOException, NoSuchAlgorithmException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty for verification.");
        }

        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        ensureVersion1Exists(evidence);

        EvidenceVersion version = evidenceVersionRepository.findByEvidence_EvidenceIdAndVersionNumber(evidenceId, versionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Version " + versionNumber + " not found for evidenceId: " + evidenceId));

        String userRole = getCurrentUserRole();
        String calculatedHash = calculateSHA256(file.getBytes());
        String storedHash = version.getFileHash();
        String previousStatus = version.getStatus();

        BlockchainRecord bcRecord = blockchainService.getRecord(version.getVersionId())
                .orElseGet(() -> blockchainService.getRecord(evidenceId).orElse(null));
        String blockchainHash = bcRecord != null ? bcRecord.getFileHash() : null;

        boolean dbMatch = calculatedHash.equalsIgnoreCase(storedHash);
        boolean bcMatch = blockchainHash != null && calculatedHash.equalsIgnoreCase(blockchainHash);

        if (dbMatch && (bcRecord == null || bcMatch)) {
            String message = "Evidence Version " + versionNumber + " verified successfully against database and blockchain records.";
            version.setStatus(EvidenceStatus.VERIFIED.name());
            version.setReviewedBy(performedBy);
            version.setReviewedAt(LocalDateTime.now());
            evidenceVersionRepository.save(version);

            auditLogService.logCustodyEvent(
                    evidenceId,
                    ChainOfCustodyAction.EVIDENCE_VERIFIED.name(),
                    performedBy,
                    userRole,
                    previousStatus,
                    EvidenceStatus.VERIFIED.name(),
                    null,
                    "Version " + versionNumber + " verified: " + message
            );

            return new VerificationResponse(
                    evidenceId,
                    version.getFileName(),
                    calculatedHash,
                    storedHash,
                    blockchainHash,
                    "VERIFIED",
                    message
            );
        } else {
            String message = "Verification failed for Version " + versionNumber + ": Cryptographic SHA-256 mismatch detected.";
            version.setStatus(EvidenceStatus.TAMPERED.name());
            evidenceVersionRepository.save(version);

            auditLogService.logCustodyEvent(
                    evidenceId,
                    ChainOfCustodyAction.INTEGRITY_COMPROMISED.name(),
                    performedBy,
                    userRole,
                    previousStatus,
                    EvidenceStatus.TAMPERED.name(),
                    "Cryptographic hash mismatch for Version " + versionNumber,
                    "CRITICAL ALERT: Version " + versionNumber + " integrity compromised."
            );

            return new VerificationResponse(
                    evidenceId,
                    version.getFileName(),
                    calculatedHash,
                    storedHash,
                    blockchainHash,
                    "TAMPERED",
                    message
            );
        }
    }
}
