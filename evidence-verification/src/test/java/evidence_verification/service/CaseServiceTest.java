package evidence_verification.service;

import evidence_verification.Entity.Case;
import evidence_verification.Entity.CasePriority;
import evidence_verification.Entity.CaseStatus;
import evidence_verification.dto.AssignCaseRequest;
import evidence_verification.dto.CaseDetailDTO;
import evidence_verification.dto.CaseResponseDTO;
import evidence_verification.dto.CreateCaseRequest;
import evidence_verification.dto.UpdateCaseStatusRequest;
import evidence_verification.repository.CaseRepository;
import evidence_verification.repository.EvidenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CaseService caseService;

    private Case sampleCase;

    @BeforeEach
    void setUp() {
        sampleCase = new Case();
        sampleCase.setCaseId("CASE-2026-001");
        sampleCase.setTitle("Financial Fraud Investigation");
        sampleCase.setDescription("Investigating corporate asset misappropriation");
        sampleCase.setPriority(CasePriority.HIGH.name());
        sampleCase.setStatus(CaseStatus.OPEN.name());
        sampleCase.setCreatedBy("admin@test.com");
        sampleCase.setAssignedInvestigator("investigator@test.com");
    }

    @Test
    void testCreateCase_Success() {
        CreateCaseRequest request = new CreateCaseRequest();
        request.setTitle("Cyber attack analysis");
        request.setDescription("Investigating unauthorized server intrusion");
        request.setPriority("HIGH");
        request.setAssignedInvestigator("investigator@test.com");

        when(caseRepository.count()).thenReturn(0L);
        when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaseResponseDTO result = caseService.createCase(request, "admin@test.com", "ADMIN");

        assertNotNull(result);
        assertEquals("Cyber attack analysis", result.getTitle());
        assertEquals("HIGH", result.getPriority());
        assertEquals("OPEN", result.getStatus());
        assertEquals("admin@test.com", result.getCreatedBy());
        assertEquals("investigator@test.com", result.getAssignedInvestigator());
        verify(caseRepository, times(1)).save(any(Case.class));
        verify(auditLogService, atLeast(2)).logCustodyEvent(anyString(), anyString(), eq("admin@test.com"), eq("ADMIN"), any(), any(), any(), anyString());
    }

    @Test
    void testGetCases_AllFiltersNull_Success() {
        when(caseRepository.searchAndFilterCases(null, null, null, null)).thenReturn(List.of(sampleCase));

        List<CaseResponseDTO> list = caseService.getCases("admin@test.com", "ADMIN", null, null, null, null);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("CASE-2026-001", list.get(0).getCaseId());
        verify(caseRepository, times(1)).searchAndFilterCases(null, null, null, null);
    }

    @Test
    void testGetCases_WithSearchAndFilter_Success() {
        when(caseRepository.searchAndFilterCases("Fraud", "OPEN", "HIGH", "investigator@test.com"))
                .thenReturn(List.of(sampleCase));

        List<CaseResponseDTO> list = caseService.getCases("admin@test.com", "ADMIN", "Fraud", "OPEN", "HIGH", "investigator@test.com");

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Financial Fraud Investigation", list.get(0).getTitle());
        verify(caseRepository, times(1)).searchAndFilterCases("Fraud", "OPEN", "HIGH", "investigator@test.com");
    }

    @Test
    void testGetCaseDetail_Success() {
        when(caseRepository.findByCaseId("CASE-2026-001")).thenReturn(Optional.of(sampleCase));
        when(auditLogService.getChainOfCustodyForEvidence("CASE-2026-001")).thenReturn(List.of());

        CaseDetailDTO detail = caseService.getCaseDetail("CASE-2026-001", "admin@test.com", "ADMIN");

        assertNotNull(detail);
        assertNotNull(detail.getCaseDetails());
        assertEquals("CASE-2026-001", detail.getCaseDetails().getCaseId());
        assertEquals("Financial Fraud Investigation", detail.getCaseDetails().getTitle());
    }

    @Test
    void testUpdateCaseStatus_Success() {
        UpdateCaseStatusRequest request = new UpdateCaseStatusRequest();
        request.setStatus("IN_PROGRESS");
        request.setReason("Investigation underway");

        when(caseRepository.findByCaseId("CASE-2026-001")).thenReturn(Optional.of(sampleCase));
        when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaseResponseDTO updated = caseService.updateCaseStatus("CASE-2026-001", request, "admin@test.com", "ADMIN");

        assertNotNull(updated);
        assertEquals("IN_PROGRESS", updated.getStatus());
        verify(auditLogService, times(1)).logCustodyEvent(eq("CASE-2026-001"), eq("CASE_STATUS_CHANGED"), eq("admin@test.com"), eq("ADMIN"), eq("OPEN"), eq("IN_PROGRESS"), eq("Investigation underway"), anyString());
    }

    @Test
    void testAssignInvestigator_Success() {
        AssignCaseRequest request = new AssignCaseRequest();
        request.setAssignedInvestigator("new_investigator@test.com");

        when(caseRepository.findByCaseId("CASE-2026-001")).thenReturn(Optional.of(sampleCase));
        when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CaseResponseDTO updated = caseService.assignInvestigator("CASE-2026-001", request, "admin@test.com", "ADMIN");

        assertNotNull(updated);
        assertEquals("new_investigator@test.com", updated.getAssignedInvestigator());
        verify(auditLogService, times(1)).logCustodyEvent(eq("CASE-2026-001"), eq("CASE_REASSIGNED"), eq("admin@test.com"), eq("ADMIN"), eq("OPEN"), eq("OPEN"), isNull(), anyString());
    }
}
