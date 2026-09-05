package evidence_verification.dto;

public class NoteRequestDTO {

    private String content;

    public NoteRequestDTO() {
    }

    public NoteRequestDTO(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
