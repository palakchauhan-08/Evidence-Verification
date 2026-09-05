package evidence_verification.dto;

import evidence_verification.Entity.EvidenceNote;
import java.time.LocalDateTime;

public class EvidenceNoteDTO {

    private String noteId;
    private String evidenceId;
    private String caseId;
    private String author;
    private String authorRole;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EvidenceNoteDTO() {
    }

    public EvidenceNoteDTO(EvidenceNote note) {
        if (note != null) {
            this.noteId = note.getNoteId();
            if (note.getEvidence() != null) {
                this.evidenceId = note.getEvidence().getEvidenceId();
                this.caseId = note.getEvidence().getCaseId();
            }
            this.author = note.getAuthor();
            this.authorRole = note.getAuthorRole();
            this.content = note.getContent();
            this.createdAt = note.getCreatedAt();
            this.updatedAt = note.getUpdatedAt();
        }
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
