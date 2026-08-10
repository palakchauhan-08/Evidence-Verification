package evidence_verification.service;

import evidence_verification.Entity.AuditLog;
import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.Evidence;
import evidence_verification.dto.EvidenceDetailDTO;
import evidence_verification.dto.EvidenceResponseDTO;
import evidence_verification.dto.VerificationResponse;
import evidence_verification.repository.EvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final AuditLogService auditLogService;
    private final BlockchainService blockchainService;

    public EvidenceService(EvidenceRepository evidenceRepository, AuditLogService auditLogService, BlockchainService blockchainService) {
        this.evidenceRepository = evidenceRepository;
        this.auditLogService = auditLogService;
        this.blockchainService = blockchainService;
    }

    public Evidence uploadEvidence(MultipartFile file, String uploadedBy) throws IOException, NoSuchAlgorithmException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Generate SHA-256 hash of the uploaded file
        String fileHash = calculateSHA256(file.getBytes());

        // Generate a unique Evidence ID
        String evidenceId = "EVI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Build and save Evidence entity
        Evidence evidence = new Evidence();
        evidence.setEvidenceId(evidenceId);
        evidence.setFileName(file.getOriginalFilename());
        evidence.setFileType(file.getContentType());
        evidence.setFileHash(fileHash);
        evidence.setUploadedBy(uploadedBy);

        Evidence savedEvidence = evidenceRepository.save(evidence);

        // Record audit log for evidence upload
        auditLogService.logAction(
                savedEvidence.getEvidenceId(),
                "EVIDENCE_UPLOADED",
                uploadedBy,
                "File '" + savedEvidence.getFileName() + "' uploaded with hash " + fileHash
        );

        // Anchor evidence hash to blockchain
        BlockchainRecord blockchainRecord = blockchainService.anchorHash(
                savedEvidence.getEvidenceId(),
                savedEvidence.getFileHash()
        );

        // Record audit log for blockchain anchoring
        if (blockchainRecord != null && "CONFIRMED".equalsIgnoreCase(blockchainRecord.getStatus())) {
            auditLogService.logAction(
                    savedEvidence.getEvidenceId(),
                    "BLOCKCHAIN_ANCHORED",
                    uploadedBy,
                    "Evidence hash anchored to blockchain with transaction hash: " + blockchainRecord.getTransactionHash()
            );
        } else {
            String statusDetails = blockchainRecord != null ? blockchainRecord.getStatus() : "FAILED";
            auditLogService.logAction(
                    savedEvidence.getEvidenceId(),
                    "BLOCKCHAIN_ANCHORING_FAILED",
                    uploadedBy,
                    "Evidence hash anchoring failed with status: " + statusDetails
            );
        }

        return savedEvidence;
    }

    public List<EvidenceResponseDTO> getUserEvidence(String uploadedBy) {
        return evidenceRepository.findByUploadedBy(uploadedBy).stream()
                .map(EvidenceResponseDTO::new)
                .toList();
    }

    public EvidenceDetailDTO getEvidenceDetail(String evidenceId, String requestedBy) {
        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        if (!evidence.getUploadedBy().equalsIgnoreCase(requestedBy)) {
            throw new SecurityException("Unauthorized access: You do not have permission to view this evidence record.");
        }

        BlockchainRecord blockchainRecord = blockchainService.getRecord(evidenceId).orElse(null);
        List<AuditLog> auditLogs = auditLogService.getAuditLogsForEvidence(evidenceId);

        return new EvidenceDetailDTO(
                evidence.getId(),
                evidence.getEvidenceId(),
                evidence.getFileName(),
                evidence.getFileType(),
                evidence.getFileHash(),
                evidence.getUploadedBy(),
                blockchainRecord,
                auditLogs
        );
    }

    public VerificationResponse verifyEvidence(MultipartFile file, String performedBy) throws IOException, NoSuchAlgorithmException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // 1. Calculate SHA-256 hash of uploaded file
        String calculatedHash = calculateSHA256(file.getBytes());
        String fileName = file.getOriginalFilename();

        // 2. Query PostgreSQL database for matching evidence by file hash
        Optional<Evidence> evidenceOpt = evidenceRepository.findByFileHash(calculatedHash);

        if (evidenceOpt.isEmpty()) {
            String message = "Verification failed: No matching evidence record found in PostgreSQL database.";
            auditLogService.logAction("UNKNOWN", "EVIDENCE_VERIFICATION_FAILED", performedBy, message);

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

        // 3. Query Blockchain for corresponding record
        Optional<BlockchainRecord> bcRecordOpt = blockchainService.getRecord(evidenceId);

        if (bcRecordOpt.isEmpty()) {
            String message = "Verification failed: Database hash matched, but no corresponding Blockchain record was found for evidence ID: " + evidenceId;
            auditLogService.logAction(evidenceId, "EVIDENCE_VERIFICATION_FAILED", performedBy, message);

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
            auditLogService.logAction(evidenceId, "EVIDENCE_VERIFIED", performedBy, message);

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
                message = "Verification failed: Both database hash and blockchain hash mismatched.";
            } else if (!dbMatch) {
                message = "Verification failed: PostgreSQL database hash mismatched.";
            } else {
                message = "Verification failed: Blockchain hash mismatched (tampering detected on-chain).";
            }

            auditLogService.logAction(evidenceId, "EVIDENCE_VERIFICATION_FAILED", performedBy, message);

            return new VerificationResponse(
                    evidenceId,
                    fileName,
                    calculatedHash,
                    storedHash,
                    blockchainHash,
                    "NOT VERIFIED",
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
}
