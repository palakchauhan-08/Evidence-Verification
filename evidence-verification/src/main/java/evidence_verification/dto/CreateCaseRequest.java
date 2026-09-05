package evidence_verification.dto;

public class CreateCaseRequest {

    private String title;
    private String description;
    private String priority;
    private String assignedInvestigator;

    public CreateCaseRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAssignedInvestigator() {
        return assignedInvestigator;
    }

    public void setAssignedInvestigator(String assignedInvestigator) {
        this.assignedInvestigator = assignedInvestigator;
    }
}
