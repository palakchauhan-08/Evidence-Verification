package evidence_verification.service;

import evidence_verification.Entity.AuditLog;
import evidence_verification.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void testLogAction_Success() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditLog log = auditLogService.logAction("EVI-100", "EVIDENCE_UPLOADED", "officer1@test.com", "Uploaded test file");

        assertNotNull(log);
        assertEquals("EVI-100", log.getEvidenceId());
        assertEquals("EVIDENCE_UPLOADED", log.getAction());
        assertEquals("officer1@test.com", log.getPerformedBy());
        assertEquals("Uploaded test file", log.getDetails());
        assertNotNull(log.getTimestamp());

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testGetAuditLogsForEvidence_Success() {
        AuditLog log1 = new AuditLog("EVI-100", "EVIDENCE_UPLOADED", "officer1@test.com", "Upload");
        AuditLog log2 = new AuditLog("EVI-100", "EVIDENCE_VERIFIED", "verifier1@test.com", "Verified");

        when(auditLogRepository.findByEvidenceIdOrderByTimestampDesc("EVI-100")).thenReturn(List.of(log2, log1));

        List<AuditLog> logs = auditLogService.getAuditLogsForEvidence("EVI-100");

        assertNotNull(logs);
        assertEquals(2, logs.size());
        assertEquals("EVIDENCE_VERIFIED", logs.get(0).getAction());
        assertEquals("EVIDENCE_UPLOADED", logs.get(1).getAction());

        verify(auditLogRepository, times(1)).findByEvidenceIdOrderByTimestampDesc("EVI-100");
    }
}
