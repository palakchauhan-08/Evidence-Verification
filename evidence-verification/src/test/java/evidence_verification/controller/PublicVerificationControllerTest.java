package evidence_verification.controller;

import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.Evidence;
import evidence_verification.repository.EvidenceRepository;
import evidence_verification.service.BlockchainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicVerificationControllerTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private BlockchainService blockchainService;

    @InjectMocks
    private PublicVerificationController controller;

    private Evidence sampleEvidence;
    private BlockchainRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleEvidence = new Evidence();
        sampleEvidence.setEvidenceId("EVI-PUBLIC123");
        sampleEvidence.setFileName("forensic_image.png");
        sampleEvidence.setFileExtension("png");
        sampleEvidence.setFileType("image/png");
        sampleEvidence.setFileSize(204800L);
        sampleEvidence.setFileHash("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e");
        sampleEvidence.setStatus("VERIFIED");

        sampleRecord = new BlockchainRecord(
                "EVI-PUBLIC123",
                "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e",
                "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef",
                "CONFIRMED"
        );
    }

    @Test
    void testGetPublicEvidenceVerification_Success() {
        when(evidenceRepository.findByEvidenceId("EVI-PUBLIC123")).thenReturn(Optional.of(sampleEvidence));
        when(blockchainService.getRecord("EVI-PUBLIC123")).thenReturn(Optional.of(sampleRecord));

        ResponseEntity<?> response = controller.getPublicEvidenceVerification("EVI-PUBLIC123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(evidenceRepository, times(1)).findByEvidenceId("EVI-PUBLIC123");
        verify(blockchainService, times(1)).getRecord("EVI-PUBLIC123");
    }

    @Test
    void testGetPublicEvidenceVerification_NotFound() {
        when(evidenceRepository.findByEvidenceId(anyString())).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getPublicEvidenceVerification("EVI-NONEXISTENT");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
