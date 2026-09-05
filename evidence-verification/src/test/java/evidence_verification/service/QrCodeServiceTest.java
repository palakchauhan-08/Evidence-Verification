package evidence_verification.service;

import evidence_verification.Entity.Case;
import evidence_verification.Entity.Evidence;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private QrCodeService qrCodeService;

    private Evidence sampleEvidence;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(qrCodeService, "frontendUrl", "http://localhost:3000");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("officer@test.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        sampleEvidence = new Evidence();
        sampleEvidence.setEvidenceId("EVI-TEST5678");
        sampleEvidence.setFileName("contract.pdf");
        sampleEvidence.setFileHash("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e");
        sampleEvidence.setUploadedBy("officer@test.com");
        sampleEvidence.setStatus("UPLOADED");
    }

    @Test
    void testGenerateEvidenceQrCode_Success() {
        when(evidenceRepository.findByEvidenceId("EVI-TEST5678")).thenReturn(Optional.of(sampleEvidence));

        byte[] qrBytes = qrCodeService.generateEvidenceQrCode("EVI-TEST5678", "officer@test.com");

        assertNotNull(qrBytes);
        assertTrue(qrBytes.length > 0);

        verify(evidenceRepository, times(1)).findByEvidenceId("EVI-TEST5678");
        verify(auditLogService, times(1)).logCustodyEvent(
                eq("EVI-TEST5678"),
                eq("QR_CODE_GENERATED"),
                eq("officer@test.com"),
                anyString(),
                anyString(),
                anyString(),
                isNull(),
                anyString()
        );
    }

    @Test
    void testGenerateEvidenceQrCode_UnauthorizedUser_ThrowsSecurityException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("unauthorized@test.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_INVESTIGATOR")))
        );

        sampleEvidence.setUploadedBy("original_officer@test.com");
        when(evidenceRepository.findByEvidenceId("EVI-TEST5678")).thenReturn(Optional.of(sampleEvidence));

        assertThrows(SecurityException.class, () ->
                qrCodeService.generateEvidenceQrCode("EVI-TEST5678", "unauthorized@test.com")
        );
    }

    @Test
    void testGenerateEvidenceQrCode_EvidenceNotFound_ThrowsIllegalArgumentException() {
        when(evidenceRepository.findByEvidenceId(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                qrCodeService.generateEvidenceQrCode("EVI-MISSING", "officer@test.com")
        );
    }
}
