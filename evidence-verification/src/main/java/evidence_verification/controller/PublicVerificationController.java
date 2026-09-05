package evidence_verification.controller;

import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.Evidence;
import evidence_verification.dto.PublicVerificationResponseDTO;
import evidence_verification.repository.EvidenceRepository;
import evidence_verification.service.BlockchainService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public")
public class PublicVerificationController {

    private final EvidenceRepository evidenceRepository;
    private final BlockchainService blockchainService;

    public PublicVerificationController(EvidenceRepository evidenceRepository, BlockchainService blockchainService) {
        this.evidenceRepository = evidenceRepository;
        this.blockchainService = blockchainService;
    }

    @GetMapping("/verify/evidence/{evidenceId}")
    public ResponseEntity<?> getPublicEvidenceVerification(@PathVariable("evidenceId") String evidenceId) {
        try {
            Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                    .orElse(null);

            if (evidence == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Evidence verification record not found for Evidence ID: " + evidenceId));
            }

            Optional<BlockchainRecord> blockchainOpt = blockchainService.getRecord(evidenceId);
            String txHash = blockchainOpt.map(BlockchainRecord::getTransactionHash).orElse(null);
            String explorerUrl = (txHash != null && txHash.startsWith("0x"))
                    ? "https://amoy.polygonscan.com/tx/" + txHash
                    : null;

            String verificationStatus = evidence.getStatus();
            String message;
            if ("VERIFIED".equalsIgnoreCase(verificationStatus)) {
                message = "✅ Digital Evidence integrity verified successfully. Hash matches PostgreSQL and Polygon Amoy Blockchain.";
            } else if ("TAMPERED".equalsIgnoreCase(verificationStatus) || "COMPROMISED".equalsIgnoreCase(verificationStatus)) {
                message = "⚠️ Digital Evidence integrity compromised! Hash mismatch detected between stored hash and original record.";
            } else {
                message = "Digital Evidence record registered with status: " + verificationStatus;
            }

            String caseId = (evidence.getCaseRecord() != null) ? evidence.getCaseRecord().getCaseId() : null;

            PublicVerificationResponseDTO dto = new PublicVerificationResponseDTO(
                    evidence.getEvidenceId(),
                    evidence.getFileName(),
                    evidence.getFileExtension(),
                    evidence.getFileType(),
                    evidence.getFileSize(),
                    evidence.getFileHash(),
                    verificationStatus,
                    LocalDateTime.now(),
                    "Polygon Amoy (Testnet)",
                    txHash,
                    explorerUrl,
                    caseId,
                    message
            );

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Public evidence verification service encountered an internal error."));
        }
    }
}
