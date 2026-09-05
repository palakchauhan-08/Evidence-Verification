package evidence_verification.controller;

import evidence_verification.dto.AssignCaseRequest;
import evidence_verification.dto.CaseDetailDTO;
import evidence_verification.dto.CaseResponseDTO;
import evidence_verification.dto.CreateCaseRequest;
import evidence_verification.dto.UpdateCaseStatusRequest;
import evidence_verification.service.CaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "Anonymous";
    }

    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !auth.getAuthorities().isEmpty()) {
            return auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        }
        return "USER";
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR')")
    public ResponseEntity<?> createCase(@RequestBody CreateCaseRequest request) {
        try {
            String createdBy = getCurrentUserEmail();
            String role = getCurrentUserRole();
            CaseResponseDTO caseDto = caseService.createCase(request, createdBy, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(caseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create case: " + e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getCases(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "investigator", required = false) String investigator
    ) {
        try {
            String userEmail = getCurrentUserEmail();
            String role = getCurrentUserRole();
            List<CaseResponseDTO> list = caseService.getCases(userEmail, role, search, status, priority, investigator);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch cases: " + e.getMessage()));
        }
    }

    @GetMapping("/{caseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')")
    public ResponseEntity<?> getCaseDetail(@PathVariable("caseId") String caseId) {
        try {
            String userEmail = getCurrentUserEmail();
            String role = getCurrentUserRole();
            CaseDetailDTO detail = caseService.getCaseDetail(caseId, userEmail, role);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch case details: " + e.getMessage()));
        }
    }

    @PatchMapping("/{caseId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST')")
    public ResponseEntity<?> updateCaseStatus(
            @PathVariable("caseId") String caseId,
            @RequestBody UpdateCaseStatusRequest request
    ) {
        try {
            String userEmail = getCurrentUserEmail();
            String role = getCurrentUserRole();
            CaseResponseDTO updated = caseService.updateCaseStatus(caseId, request, userEmail, role);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update case status: " + e.getMessage()));
        }
    }

    @PatchMapping("/{caseId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR')")
    public ResponseEntity<?> assignInvestigator(
            @PathVariable("caseId") String caseId,
            @RequestBody AssignCaseRequest request
    ) {
        try {
            String userEmail = getCurrentUserEmail();
            String role = getCurrentUserRole();
            CaseResponseDTO updated = caseService.assignInvestigator(caseId, request, userEmail, role);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to assign investigator: " + e.getMessage()));
        }
    }

    @PostMapping("/{caseId}/evidence/{evidenceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR')")
    public ResponseEntity<?> addEvidenceToCase(
            @PathVariable("caseId") String caseId,
            @PathVariable("evidenceId") String evidenceId
    ) {
        try {
            String userEmail = getCurrentUserEmail();
            String role = getCurrentUserRole();
            CaseDetailDTO detail = caseService.addEvidenceToCase(caseId, evidenceId, userEmail, role);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add evidence to case: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{caseId}/evidence/{evidenceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVESTIGATOR')")
    public ResponseEntity<?> removeEvidenceFromCase(
            @PathVariable("caseId") String caseId,
            @PathVariable("evidenceId") String evidenceId
    ) {
        try {
            String userEmail = getCurrentUserEmail();
            String role = getCurrentUserRole();
            CaseDetailDTO detail = caseService.removeEvidenceFromCase(caseId, evidenceId, userEmail, role);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to remove evidence from case: " + e.getMessage()));
        }
    }
}
