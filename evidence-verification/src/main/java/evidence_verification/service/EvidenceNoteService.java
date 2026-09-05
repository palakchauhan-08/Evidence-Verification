package evidence_verification.service;

import evidence_verification.Entity.ChainOfCustodyAction;
import evidence_verification.Entity.Evidence;
import evidence_verification.Entity.EvidenceNote;
import evidence_verification.dto.EvidenceNoteDTO;
import evidence_verification.dto.NoteRequestDTO;
import evidence_verification.repository.EvidenceNoteRepository;
import evidence_verification.repository.EvidenceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EvidenceNoteService {

    private final EvidenceNoteRepository evidenceNoteRepository;
    private final EvidenceRepository evidenceRepository;
    private final AuditLogService auditLogService;

    public EvidenceNoteService(
            EvidenceNoteRepository evidenceNoteRepository,
            EvidenceRepository evidenceRepository,
            AuditLogService auditLogService) {
        this.evidenceNoteRepository = evidenceNoteRepository;
        this.evidenceRepository = evidenceRepository;
        this.auditLogService = auditLogService;
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

    @Transactional
    public EvidenceNoteDTO addNote(String evidenceId, NoteRequestDTO request, String author) {
        String role = getCurrentUserRole();
        if ("VIEWER".equalsIgnoreCase(role)) {
            throw new SecurityException("Users with VIEWER role are not authorized to create investigator notes.");
        }

        if (request == null || request.getContent() == null || request.getContent().trim().length() < 3) {
            throw new IllegalArgumentException("Note content must be at least 3 characters long.");
        }
        String sanitizedContent = request.getContent().trim();
        if (sanitizedContent.length() > 4000) {
            throw new IllegalArgumentException("Note content cannot exceed 4000 characters.");
        }

        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        String noteId = "NOTE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        EvidenceNote note = new EvidenceNote(noteId, evidence, author, role, sanitizedContent);

        EvidenceNote saved = evidenceNoteRepository.save(note);

        // Audit Event: INVESTIGATOR_NOTE_ADDED
        auditLogService.logCustodyEvent(
                evidenceId,
                ChainOfCustodyAction.INVESTIGATOR_NOTE_ADDED.name(),
                author,
                role,
                evidence.getStatus(),
                evidence.getStatus(),
                null,
                "Investigator note added by " + author + " (" + role + "): " + (sanitizedContent.length() > 60 ? sanitizedContent.substring(0, 60) + "..." : sanitizedContent)
        );

        return new EvidenceNoteDTO(saved);
    }

    public List<EvidenceNoteDTO> getNotesForEvidence(String evidenceId, String requestedBy) {
        Evidence evidence = evidenceRepository.findByEvidenceId(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence record not found for evidenceId: " + evidenceId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean canViewAll = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_FORENSIC_ANALYST")
                        || a.getAuthority().equals("ROLE_VIEWER")
                        || a.getAuthority().equals("ROLE_INVESTIGATOR"));

        if (!canViewAll && !evidence.getUploadedBy().equalsIgnoreCase(requestedBy)) {
            throw new SecurityException("Unauthorized access: You do not have permission to view notes for this evidence record.");
        }

        return evidenceNoteRepository.findByEvidence_EvidenceIdOrderByCreatedAtDesc(evidenceId).stream()
                .map(EvidenceNoteDTO::new)
                .toList();
    }

    @Transactional
    public EvidenceNoteDTO updateNote(String evidenceId, String noteId, NoteRequestDTO request, String requestedBy) {
        String role = getCurrentUserRole();
        if ("VIEWER".equalsIgnoreCase(role)) {
            throw new SecurityException("Users with VIEWER role are not authorized to edit investigator notes.");
        }

        if (request == null || request.getContent() == null || request.getContent().trim().length() < 3) {
            throw new IllegalArgumentException("Note content must be at least 3 characters long.");
        }
        String sanitizedContent = request.getContent().trim();
        if (sanitizedContent.length() > 4000) {
            throw new IllegalArgumentException("Note content cannot exceed 4000 characters.");
        }

        EvidenceNote note = evidenceNoteRepository.findByNoteId(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note record not found for noteId: " + noteId));

        if (!note.getEvidence().getEvidenceId().equalsIgnoreCase(evidenceId)) {
            throw new IllegalArgumentException("Note ID " + noteId + " does not belong to evidence ID " + evidenceId);
        }

        boolean isAuthor = note.getAuthor().equalsIgnoreCase(requestedBy);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        if (!isAuthor && !isAdmin) {
            throw new SecurityException("Unauthorized access: You can only edit your own investigator notes.");
        }

        note.setContent(sanitizedContent);
        note.setUpdatedAt(LocalDateTime.now());
        EvidenceNote updated = evidenceNoteRepository.save(note);

        auditLogService.logCustodyEvent(
                evidenceId,
                ChainOfCustodyAction.INVESTIGATOR_NOTE_UPDATED.name(),
                requestedBy,
                role,
                note.getEvidence().getStatus(),
                note.getEvidence().getStatus(),
                null,
                "Investigator note " + noteId + " updated by " + requestedBy + " (" + role + ")"
        );

        return new EvidenceNoteDTO(updated);
    }

    @Transactional
    public void deleteNote(String evidenceId, String noteId, String requestedBy) {
        String role = getCurrentUserRole();
        if ("VIEWER".equalsIgnoreCase(role)) {
            throw new SecurityException("Users with VIEWER role are not authorized to delete investigator notes.");
        }

        EvidenceNote note = evidenceNoteRepository.findByNoteId(noteId)
                .orElseThrow(() -> new IllegalArgumentException("Note record not found for noteId: " + noteId));

        if (!note.getEvidence().getEvidenceId().equalsIgnoreCase(evidenceId)) {
            throw new IllegalArgumentException("Note ID " + noteId + " does not belong to evidence ID " + evidenceId);
        }

        boolean isAuthor = note.getAuthor().equalsIgnoreCase(requestedBy);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        if (!isAuthor && !isAdmin) {
            throw new SecurityException("Unauthorized access: You can only delete your own investigator notes.");
        }

        evidenceNoteRepository.delete(note);

        auditLogService.logCustodyEvent(
                evidenceId,
                ChainOfCustodyAction.INVESTIGATOR_NOTE_DELETED.name(),
                requestedBy,
                role,
                note.getEvidence().getStatus(),
                note.getEvidence().getStatus(),
                null,
                "Investigator note " + noteId + " deleted by " + requestedBy + " (" + role + ")"
        );
    }
}
