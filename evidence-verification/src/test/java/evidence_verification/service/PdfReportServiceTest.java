package evidence_verification.service;

import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.Case;
import evidence_verification.Entity.Evidence;
import evidence_verification.repository.CaseRepository;
import evidence_verification.repository.EvidenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfReportServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private BlockchainService blockchainService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private PdfReportService pdfReportService;

    private Evidence sampleEvidence;
    private BlockchainRecord sampleBcRecord;
    private Case sampleCase;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("officer@test.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        sampleCase = new Case();
        sampleCase.setCaseId("CASE-2026-001");
        sampleCase.setTitle("Financial Fraud Investigation");
        sampleCase.setPriority("HIGH");
        sampleCase.setStatus("OPEN");

        sampleEvidence = new Evidence();
        sampleEvidence.setEvidenceId("EVI-TEST1234");
        sampleEvidence.setFileName("financial_statement.pdf");
        sampleEvidence.setFileExtension("pdf");
        sampleEvidence.setFileType("application/pdf");
        sampleEvidence.setFileSize(2048L);
        sampleEvidence.setFileHash("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e");
        sampleEvidence.setUploadedBy("officer@test.com");
        sampleEvidence.setStatus("VERIFIED");
        sampleEvidence.setUploadedAt(LocalDateTime.now());
        sampleEvidence.setCaseRecord(sampleCase);

        sampleBcRecord = new BlockchainRecord("EVI-TEST1234", "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e", "0x1234567890abcdef", "CONFIRMED");
    }

    @Test
    void testGenerateVerificationReport_AdminSuccess() {
        when(evidenceRepository.findByEvidenceId("EVI-TEST1234")).thenReturn(Optional.of(sampleEvidence));
        when(blockchainService.getRecord("EVI-TEST1234")).thenReturn(Optional.of(sampleBcRecord));
        when(auditLogService.getChainOfCustodyForEvidence("EVI-TEST1234")).thenReturn(List.of());

        byte[] pdfBytes = pdfReportService.generateVerificationReport("EVI-TEST1234", "officer@test.com");

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Header %PDF- check
        String pdfString = new String(pdfBytes, 0, Math.min(pdfBytes.length, 10));
        assertTrue(pdfString.startsWith("%PDF"));

        verify(evidenceRepository, times(1)).findByEvidenceId("EVI-TEST1234");
        verify(blockchainService, times(1)).getRecord("EVI-TEST1234");
    }

    @Test
    void testGenerateVerificationReport_InvestigatorUnassignedUnauthorized() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("other_investigator@test.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_INVESTIGATOR")))
        );

        sampleEvidence.setCaseRecord(null); // No case
        sampleEvidence.setUploadedBy("original_officer@test.com");

        when(evidenceRepository.findByEvidenceId("EVI-TEST1234")).thenReturn(Optional.of(sampleEvidence));

        assertThrows(SecurityException.class, () ->
                pdfReportService.generateVerificationReport("EVI-TEST1234", "other_investigator@test.com")
        );
    }

    @Test
    void testGenerateVerificationReport_EvidenceNotFound_ThrowsIllegalArgumentException() {
        when(evidenceRepository.findByEvidenceId(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                pdfReportService.generateVerificationReport("EVI-MISSING", "officer@test.com")
        );
    }
}
