package evidence_verification.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import evidence_verification.Entity.ChainOfCustodyAction;
import evidence_verification.Entity.Evidence;
import evidence_verification.repository.EvidenceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class QrCodeService {

    private final EvidenceRepository evidenceRepository;
    private final AuditLogService auditLogService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public QrCodeService(EvidenceRepository evidenceRepository, AuditLogService auditLogService) {
        this.evidenceRepository = evidenceRepository;
        this.auditLogService = auditLogService;
    }

    public byte[] generateEvidenceQrCode(String evidenceId, String requestedBy) {
        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean canViewAll = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_FORENSIC_ANALYST")
                        || a.getAuthority().equals("ROLE_VIEWER"));

        if (!canViewAll && !evidence.getUploadedBy().equalsIgnoreCase(requestedBy)) {
            // Check if user is associated with the case
            boolean isCaseAuthorized = evidence.getCaseRecord() != null && (
                    requestedBy.equalsIgnoreCase(evidence.getCaseRecord().getCreatedBy())
                            || requestedBy.equalsIgnoreCase(evidence.getCaseRecord().getAssignedInvestigator())
            );
            if (!isCaseAuthorized) {
                throw new SecurityException("Unauthorized access: You do not have permission to generate QR code for this evidence.");
            }
        }

        // Safe verification reference URL (does not contain passwords, JWTs, or raw evidence file bytes)
        String verificationUrl = frontendUrl + "/verify/evidence/" + evidenceId;

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(verificationUrl, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            // Log Custody Audit Event: QR_CODE_GENERATED
            String userRole = getCurrentUserRole();
            auditLogService.logCustodyEvent(
                    evidenceId,
                    ChainOfCustodyAction.QR_CODE_GENERATED.name(),
                    requestedBy,
                    userRole,
                    evidence.getStatus(),
                    evidence.getStatus(),
                    null,
                    "Digital Evidence QR Code generated linking to verification URL: " + verificationUrl
            );

            return pngData;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR Code image: " + e.getMessage(), e);
        }
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
}
