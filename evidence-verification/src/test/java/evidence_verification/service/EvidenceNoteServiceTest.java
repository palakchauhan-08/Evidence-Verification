package evidence_verification.service;

import evidence_verification.Entity.Evidence;
import evidence_verification.Entity.EvidenceNote;
import evidence_verification.dto.EvidenceNoteDTO;
import evidence_verification.dto.NoteRequestDTO;
import evidence_verification.repository.EvidenceNoteRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenceNoteServiceTest {

    @Mock
    private EvidenceNoteRepository evidenceNoteRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private EvidenceNoteService evidenceNoteService;

    private Evidence sampleEvidence;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("investigator@test.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_INVESTIGATOR")))
        );

        sampleEvidence = new Evidence();
        sampleEvidence.setEvidenceId("EVI-NOTE123");
        sampleEvidence.setFileName("document.pdf");
        sampleEvidence.setUploadedBy("officer@test.com");
        sampleEvidence.setStatus("UPLOADED");
    }

    @Test
    void testAddNote_Success() {
        when(evidenceRepository.findByEvidenceId("EVI-NOTE123")).thenReturn(Optional.of(sampleEvidence));
        when(evidenceNoteRepository.save(any(EvidenceNote.class))).thenAnswer(i -> i.getArgument(0));

        NoteRequestDTO req = new NoteRequestDTO("Initial forensic observation on document metadata.");
        EvidenceNoteDTO result = evidenceNoteService.addNote("EVI-NOTE123", req, "investigator@test.com");

        assertNotNull(result);
        assertEquals("EVI-NOTE123", result.getEvidenceId());
        assertEquals("investigator@test.com", result.getAuthor());
        assertEquals("Initial forensic observation on document metadata.", result.getContent());
        assertNotNull(result.getNoteId());

        verify(evidenceNoteRepository, times(1)).save(any(EvidenceNote.class));
        verify(auditLogService, times(1)).logCustodyEvent(eq("EVI-NOTE123"), eq("INVESTIGATOR_NOTE_ADDED"), eq("investigator@test.com"), anyString(), anyString(), anyString(), isNull(), anyString());
    }

    @Test
    void testAddNote_ViewerRole_ThrowsSecurityException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("viewer@test.com", "pass", List.of(new SimpleGrantedAuthority("ROLE_VIEWER")))
        );

        NoteRequestDTO req = new NoteRequestDTO("Viewer comment");
        assertThrows(SecurityException.class, () ->
                evidenceNoteService.addNote("EVI-NOTE123", req, "viewer@test.com")
        );
    }

    @Test
    void testAddNote_EmptyContent_ThrowsIllegalArgumentException() {
        NoteRequestDTO req = new NoteRequestDTO("  ");
        assertThrows(IllegalArgumentException.class, () ->
                evidenceNoteService.addNote("EVI-NOTE123", req, "investigator@test.com")
        );
    }

    @Test
    void testUpdateNote_AuthorSuccess() {
        EvidenceNote note = new EvidenceNote("NOTE-100", sampleEvidence, "investigator@test.com", "INVESTIGATOR", "Original content");
        when(evidenceNoteRepository.findByNoteId("NOTE-100")).thenReturn(Optional.of(note));
        when(evidenceNoteRepository.save(any(EvidenceNote.class))).thenAnswer(i -> i.getArgument(0));

        NoteRequestDTO req = new NoteRequestDTO("Updated observation content.");
        EvidenceNoteDTO result = evidenceNoteService.updateNote("EVI-NOTE123", "NOTE-100", req, "investigator@test.com");

        assertNotNull(result);
        assertEquals("Updated observation content.", result.getContent());
        verify(auditLogService, times(1)).logCustodyEvent(eq("EVI-NOTE123"), eq("INVESTIGATOR_NOTE_UPDATED"), eq("investigator@test.com"), anyString(), anyString(), anyString(), isNull(), anyString());
    }

    @Test
    void testUpdateNote_UnauthorizedUser_ThrowsSecurityException() {
        EvidenceNote note = new EvidenceNote("NOTE-100", sampleEvidence, "original_investigator@test.com", "INVESTIGATOR", "Original content");
        when(evidenceNoteRepository.findByNoteId("NOTE-100")).thenReturn(Optional.of(note));

        NoteRequestDTO req = new NoteRequestDTO("Hacker attempt to edit note.");
        assertThrows(SecurityException.class, () ->
                evidenceNoteService.updateNote("EVI-NOTE123", "NOTE-100", req, "other_user@test.com")
        );
    }

    @Test
    void testDeleteNote_AuthorSuccess() {
        EvidenceNote note = new EvidenceNote("NOTE-100", sampleEvidence, "investigator@test.com", "INVESTIGATOR", "Original content");
        when(evidenceNoteRepository.findByNoteId("NOTE-100")).thenReturn(Optional.of(note));

        evidenceNoteService.deleteNote("EVI-NOTE123", "NOTE-100", "investigator@test.com");

        verify(evidenceNoteRepository, times(1)).delete(note);
        verify(auditLogService, times(1)).logCustodyEvent(eq("EVI-NOTE123"), eq("INVESTIGATOR_NOTE_DELETED"), eq("investigator@test.com"), anyString(), anyString(), anyString(), isNull(), anyString());
    }
}
