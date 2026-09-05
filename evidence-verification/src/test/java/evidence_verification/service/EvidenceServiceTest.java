package evidence_verification.service;

import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.Entity.Evidence;
import evidence_verification.dto.EvidenceDetailDTO;
import evidence_verification.dto.EvidenceResponseDTO;
import evidence_verification.dto.VerificationResponse;
import evidence_verification.repository.EvidenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private BlockchainService blockchainService;

    @Mock
    private EmailService emailService;

    @Mock
    private EvidenceVersionService evidenceVersionService;

    @InjectMocks
    private EvidenceService evidenceService;

    private MockMultipartFile testFile;
    private String expectedHash;

    @BeforeEach
    void setUp() {
        // "Hello World" -> SHA-256: a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e
        testFile = new MockMultipartFile(
                "file",
                "hello.txt",
                "text/plain",
                "Hello World".getBytes(StandardCharsets.UTF_8)
        );
        expectedHash = "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e";
    }

    @Test
    void testUploadEvidence_Success() throws Exception {
        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(blockchainService.anchorHash(anyString(), anyString()))
                .thenReturn(new BlockchainRecord("EVI-TEST123", expectedHash, "0xmocktxhash", "CONFIRMED"));

        Evidence result = evidenceService.uploadEvidence(testFile, "officer@test.com");

        assertNotNull(result);
        assertEquals("hello.txt", result.getFileName());
        assertEquals("txt", result.getFileExtension());
        assertEquals("text/plain", result.getFileType());
        assertEquals(11L, result.getFileSize());
        assertEquals("officer@test.com", result.getUploadedBy());
        assertEquals(expectedHash, result.getFileHash());
        assertNotNull(result.getEvidenceId());
        assertTrue(result.getEvidenceId().startsWith("EVI-"));
        assertNull(result.getCreatedTimestamp());
        assertNull(result.getModifiedTimestamp());

        verify(evidenceRepository, times(1)).save(any(Evidence.class));
        verify(blockchainService, times(1)).anchorHash(anyString(), eq(expectedHash));
        verify(auditLogService, times(3)).logCustodyEvent(anyString(), anyString(), eq("officer@test.com"), any(), any(), any(), any(), anyString());
    }

    @Test
    void testGetUserEvidence_Success() {
        Evidence e1 = new Evidence();
        e1.setEvidenceId("EVI-1");
        e1.setFileName("file1.pdf");
        e1.setUploadedBy("officer@test.com");

        when(evidenceRepository.findByUploadedBy("officer@test.com")).thenReturn(List.of(e1));

        List<EvidenceResponseDTO> list = evidenceService.getUserEvidence("officer@test.com");

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("EVI-1", list.get(0).getEvidenceId());
        verify(evidenceRepository, times(1)).findByUploadedBy("officer@test.com");
    }

    @Test
    void testGetEvidenceDetail_Success() {
        Evidence e1 = new Evidence();
        e1.setEvidenceId("EVI-1");
        e1.setFileName("file1.pdf");
        e1.setUploadedBy("officer@test.com");

        BlockchainRecord bcRecord = new BlockchainRecord("EVI-1", "hash1", "0xtx", "CONFIRMED");

        when(evidenceRepository.findByEvidenceId("EVI-1")).thenReturn(Optional.of(e1));
        when(blockchainService.getRecord("EVI-1")).thenReturn(Optional.of(bcRecord));
        when(auditLogService.getChainOfCustodyForEvidence("EVI-1")).thenReturn(List.of());

        EvidenceDetailDTO detail = evidenceService.getEvidenceDetail("EVI-1", "officer@test.com");

        assertNotNull(detail);
        assertEquals("EVI-1", detail.getEvidenceId());
        assertEquals("officer@test.com", detail.getUploadedBy());
        assertNotNull(detail.getBlockchainRecord());
        verify(evidenceRepository, times(1)).findByEvidenceId("EVI-1");
    }

    @Test
    void testGetEvidenceDetail_Unauthorized_ThrowsSecurityException() {
        Evidence e1 = new Evidence();
        e1.setEvidenceId("EVI-1");
        e1.setUploadedBy("officer@test.com");

        when(evidenceRepository.findByEvidenceId("EVI-1")).thenReturn(Optional.of(e1));

        assertThrows(SecurityException.class, () -> evidenceService.getEvidenceDetail("EVI-1", "unauthorized@test.com"));
    }

    @Test
    void testVerifyEvidence_DualMatch_Success() throws Exception {
        Evidence storedEvidence = new Evidence();
        storedEvidence.setEvidenceId("EVI-12345678");
        storedEvidence.setFileName("hello.txt");
        storedEvidence.setFileType("text/plain");
        storedEvidence.setFileHash(expectedHash);
        storedEvidence.setUploadedBy("officer@test.com");

        BlockchainRecord bcRecord = new BlockchainRecord("EVI-12345678", expectedHash, "0x123tx", "CONFIRMED");

        when(evidenceRepository.findByFileHash(expectedHash)).thenReturn(Optional.of(storedEvidence));
        when(blockchainService.getRecord("EVI-12345678")).thenReturn(Optional.of(bcRecord));

        VerificationResponse response = evidenceService.verifyEvidence(testFile, "verifier@test.com");

        assertNotNull(response);
        assertEquals("VERIFIED", response.getVerificationStatus());
        assertEquals("EVI-12345678", response.getEvidenceId());
        assertEquals("hello.txt", response.getFileName());
        assertEquals(expectedHash, response.getCalculatedHash());
        assertEquals(expectedHash, response.getStoredHash());
        assertEquals(expectedHash, response.getBlockchainHash());
        assertTrue(response.getVerificationMessage().contains("verified successfully"));

        verify(auditLogService, times(1)).logCustodyEvent(eq("EVI-12345678"), eq("EVIDENCE_VERIFIED"), eq("verifier@test.com"), any(), any(), any(), any(), eq(response.getVerificationMessage()));
    }

    @Test
    void testVerifyEvidence_DatabaseRecordNotFound_Failure() throws Exception {
        when(evidenceRepository.findByFileHash(expectedHash)).thenReturn(Optional.empty());

        VerificationResponse response = evidenceService.verifyEvidence(testFile, "verifier@test.com");

        assertNotNull(response);
        assertEquals("NOT VERIFIED", response.getVerificationStatus());
        assertEquals("N/A", response.getEvidenceId());
        assertNull(response.getStoredHash());
        assertNull(response.getBlockchainHash());
        assertTrue(response.getVerificationMessage().contains("No matching evidence record found in PostgreSQL database"));

        verify(auditLogService, times(1)).logCustodyEvent(eq("UNKNOWN"), eq("VERIFICATION_FAILED"), eq("verifier@test.com"), any(), any(), any(), any(), eq(response.getVerificationMessage()));
    }

    @Test
    void testVerifyEvidence_BlockchainHashMismatch_Failure() throws Exception {
        Evidence storedEvidence = new Evidence();
        storedEvidence.setEvidenceId("EVI-87654321");
        storedEvidence.setFileName("hello.txt");
        storedEvidence.setFileHash(expectedHash);

        // Blockchain record has tampered hash
        String tamperedHash = "9999999999999999999999999999999999999999999999999999999999999999";
        BlockchainRecord bcRecord = new BlockchainRecord("EVI-87654321", tamperedHash, "0xtampertx", "CONFIRMED");

        when(evidenceRepository.findByFileHash(expectedHash)).thenReturn(Optional.of(storedEvidence));
        when(blockchainService.getRecord("EVI-87654321")).thenReturn(Optional.of(bcRecord));

        VerificationResponse response = evidenceService.verifyEvidence(testFile, "verifier@test.com");

        assertNotNull(response);
        assertEquals("TAMPERED", response.getVerificationStatus());
        assertEquals("EVI-87654321", response.getEvidenceId());
        assertEquals(expectedHash, response.getCalculatedHash());
        assertEquals(expectedHash, response.getStoredHash());
        assertEquals(tamperedHash, response.getBlockchainHash());
        assertTrue(response.getVerificationMessage().contains("Blockchain hash mismatched"));

        verify(auditLogService, times(1)).logCustodyEvent(eq("EVI-87654321"), eq("VERIFICATION_FAILED"), eq("verifier@test.com"), any(), any(), any(), any(), anyString());
    }

    @Test
    void testUploadEvidence_BlockchainAnchoringFailed_LogsFailedAudit() throws Exception {
        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(blockchainService.anchorHash(anyString(), anyString()))
                .thenReturn(new BlockchainRecord("EVI-TEST123", expectedHash, "0xfailedtx", "FAILED: RPC error"));

        Evidence result = evidenceService.uploadEvidence(testFile, "officer@test.com");

        assertNotNull(result);
        assertEquals("hello.txt", result.getFileName());
        verify(evidenceRepository, times(1)).save(any(Evidence.class));
        verify(blockchainService, times(1)).anchorHash(anyString(), eq(expectedHash));
        verify(auditLogService, times(3)).logCustodyEvent(anyString(), anyString(), eq("officer@test.com"), any(), any(), any(), any(), anyString());
    }

    @Test
    void testVerifyEvidence_BlockchainRecordMissing_Failure() throws Exception {
        Evidence storedEvidence = new Evidence();
        storedEvidence.setEvidenceId("EVI-99999999");
        storedEvidence.setFileName("hello.txt");
        storedEvidence.setFileHash(expectedHash);

        when(evidenceRepository.findByFileHash(expectedHash)).thenReturn(Optional.of(storedEvidence));
        when(blockchainService.getRecord("EVI-99999999")).thenReturn(Optional.empty());

        VerificationResponse response = evidenceService.verifyEvidence(testFile, "verifier@test.com");

        assertNotNull(response);
        assertEquals("NOT VERIFIED", response.getVerificationStatus());
        assertEquals("EVI-99999999", response.getEvidenceId());
        assertNull(response.getBlockchainHash());
        assertTrue(response.getVerificationMessage().contains("no corresponding Blockchain record was found"));

        verify(auditLogService, times(1)).logCustodyEvent(eq("EVI-99999999"), eq("VERIFICATION_FAILED"), eq("verifier@test.com"), any(), any(), any(), any(), eq(response.getVerificationMessage()));
    }

    @Test
    void testUploadEvidence_PathTraversal_ThrowsException() {
        MockMultipartFile traversalFile = new MockMultipartFile(
                "file",
                "../../etc/passwd",
                "text/plain",
                "malicious content".getBytes(StandardCharsets.UTF_8)
        );
        assertThrows(IllegalArgumentException.class, () ->
                evidenceService.uploadEvidence(traversalFile, "officer@test.com")
        );
    }

    @Test
    void testUploadEvidence_BannedExtension_ThrowsException() {
        MockMultipartFile exeFile = new MockMultipartFile(
                "file",
                "payload.exe",
                "application/x-msdownload",
                "binary payload".getBytes(StandardCharsets.UTF_8)
        );
        assertThrows(IllegalArgumentException.class, () ->
                evidenceService.uploadEvidence(exeFile, "officer@test.com")
        );
    }

    @Test
    void testVerifyEvidence_TamperDetected_UpdatesStatusAndLogsCustody() throws Exception {
        Evidence storedEvidence = new Evidence();
        storedEvidence.setEvidenceId("EVI-TAMPER1");
        storedEvidence.setFileName("hello.txt");
        storedEvidence.setFileHash(expectedHash);
        storedEvidence.setStatus("UPLOADED");
        storedEvidence.setUploadedBy("officer@test.com");

        BlockchainRecord mismatchedRecord = new BlockchainRecord("EVI-TAMPER1", "different_hash_on_chain", "0xabc", "CONFIRMED");

        when(evidenceRepository.findByFileHash(expectedHash)).thenReturn(Optional.of(storedEvidence));
        when(blockchainService.getRecord("EVI-TAMPER1")).thenReturn(Optional.of(mismatchedRecord));
        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(i -> i.getArgument(0));

        VerificationResponse response = evidenceService.verifyEvidence(testFile, "verifier@test.com");

        assertNotNull(response);
        assertEquals("TAMPERED", storedEvidence.getStatus());
        verify(emailService, times(1)).sendEvidenceTamperedAlert(anyString(), eq("EVI-TAMPER1"), any(), anyString(), anyString(), any());
        verify(auditLogService, times(1)).logCustodyEvent(eq("EVI-TAMPER1"), eq("INTEGRITY_COMPROMISED"), eq("verifier@test.com"), any(), any(), any(), any(), anyString());
    }

    @Test
    void testVerifyEvidence_AlreadyTampered_SuppressesDuplicateEmail() throws Exception {
        Evidence storedEvidence = new Evidence();
        storedEvidence.setEvidenceId("EVI-TAMPER2");
        storedEvidence.setFileName("hello.txt");
        storedEvidence.setFileHash(expectedHash);
        storedEvidence.setStatus("TAMPERED"); // Already TAMPERED
        storedEvidence.setUploadedBy("officer@test.com");

        BlockchainRecord mismatchedRecord = new BlockchainRecord("EVI-TAMPER2", "different_hash_on_chain", "0xabc", "CONFIRMED");

        when(evidenceRepository.findByFileHash(expectedHash)).thenReturn(Optional.of(storedEvidence));
        when(blockchainService.getRecord("EVI-TAMPER2")).thenReturn(Optional.of(mismatchedRecord));
        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(i -> i.getArgument(0));

        VerificationResponse response = evidenceService.verifyEvidence(testFile, "verifier@test.com");

        assertNotNull(response);
        assertEquals("TAMPERED", storedEvidence.getStatus());
        // Duplicate email alert suppressed when already in TAMPERED status
        verify(emailService, never()).sendEvidenceTamperedAlert(anyString(), anyString(), any(), anyString(), anyString(), any());
    }
}
