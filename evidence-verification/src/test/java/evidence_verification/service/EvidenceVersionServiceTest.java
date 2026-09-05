package evidence_verification.service;

import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.Evidence;
import evidence_verification.Entity.EvidenceVersion;
import evidence_verification.dto.EvidenceVersionDTO;
import evidence_verification.dto.VerificationResponse;
import evidence_verification.repository.EvidenceRepository;
import evidence_verification.repository.EvidenceVersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenceVersionServiceTest {

    @Mock
    private EvidenceVersionRepository evidenceVersionRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private BlockchainService blockchainService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EvidenceVersionService evidenceVersionService;

    private Evidence sampleEvidence;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("investigator@test.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_INVESTIGATOR")))
        );

        sampleEvidence = new Evidence();
        sampleEvidence.setEvidenceId("EVI-VER123");
        sampleEvidence.setFileName("v1_document.pdf");
        sampleEvidence.setFileHash("hash_v1_original");
        sampleEvidence.setUploadedBy("officer@test.com");
        sampleEvidence.setStatus("UPLOADED");
    }

    @Test
    void testEnsureVersion1Exists_CreatesVersion1IfMissing() {
        when(evidenceVersionRepository.findByEvidence_EvidenceIdOrderByVersionNumberDesc("EVI-VER123"))
                .thenReturn(Collections.emptyList());
        when(evidenceVersionRepository.save(any(EvidenceVersion.class)))
                .thenAnswer(i -> i.getArgument(0));

        EvidenceVersion v1 = evidenceVersionService.ensureVersion1Exists(sampleEvidence);

        assertNotNull(v1);
        assertEquals(1, v1.getVersionNumber());
        assertEquals("v1_document.pdf", v1.getFileName());
        assertEquals("EVI-VER123-V1", v1.getVersionId());
        verify(evidenceVersionRepository, times(1)).save(any(EvidenceVersion.class));
    }

    @Test
    void testUploadNewVersion_SuccessIncrementsVersionNumber() throws Exception {
        EvidenceVersion v1 = new EvidenceVersion("EVI-VER123-V1", sampleEvidence, 1, "v1_doc.pdf", "pdf", "application/pdf", 100L, "hash_v1", "officer@test.com", null, null);
        when(evidenceRepository.findByEvidenceId("EVI-VER123")).thenReturn(Optional.of(sampleEvidence));
        when(evidenceVersionRepository.findByEvidence_EvidenceIdOrderByVersionNumberDesc("EVI-VER123"))
                .thenReturn(List.of(v1));
        when(evidenceVersionRepository.findTopByEvidence_EvidenceIdOrderByVersionNumberDesc("EVI-VER123"))
                .thenReturn(Optional.of(v1));
        when(evidenceVersionRepository.save(any(EvidenceVersion.class))).thenAnswer(i -> i.getArgument(0));
        when(blockchainService.anchorHash(anyString(), anyString()))
                .thenReturn(new BlockchainRecord("EVI-VER123-V2", "calculated_v2_hash", "0xversion2tx", "CONFIRMED"));

        MockMultipartFile fileV2 = new MockMultipartFile(
                "file",
                "v2_updated_document.pdf",
                "application/pdf",
                "Version 2 updated content bytes".getBytes(StandardCharsets.UTF_8)
        );

        EvidenceVersionDTO result = evidenceVersionService.uploadNewVersion("EVI-VER123", fileV2, "investigator@test.com");

        assertNotNull(result);
        assertEquals(2, result.getVersionNumber());
        assertEquals("v2_updated_document.pdf", result.getFileName());
        assertEquals("EVI-VER123-V2", result.getVersionId());
        assertNotNull(result.getBlockchainRecord());

        verify(evidenceVersionRepository, times(1)).save(any(EvidenceVersion.class));
        verify(auditLogService, times(1)).logCustodyEvent(eq("EVI-VER123"), eq("EVIDENCE_VERSION_UPLOADED"), eq("investigator@test.com"), anyString(), anyString(), anyString(), isNull(), anyString());
    }

    @Test
    void testGetVersionsForEvidence_ReturnsList() {
        EvidenceVersion v2 = new EvidenceVersion("EVI-VER123-V2", sampleEvidence, 2, "v2_doc.pdf", "pdf", "application/pdf", 200L, "hash_v2", "investigator@test.com", null, null);
        EvidenceVersion v1 = new EvidenceVersion("EVI-VER123-V1", sampleEvidence, 1, "v1_doc.pdf", "pdf", "application/pdf", 100L, "hash_v1", "officer@test.com", null, null);

        when(evidenceRepository.findByEvidenceId("EVI-VER123")).thenReturn(Optional.of(sampleEvidence));
        when(evidenceVersionRepository.findByEvidence_EvidenceIdOrderByVersionNumberDesc("EVI-VER123"))
                .thenReturn(List.of(v2, v1));

        List<EvidenceVersionDTO> versions = evidenceVersionService.getVersionsForEvidence("EVI-VER123");

        assertEquals(2, versions.size());
        assertEquals(2, versions.get(0).getVersionNumber());
        assertEquals(1, versions.get(1).getVersionNumber());
    }
}
