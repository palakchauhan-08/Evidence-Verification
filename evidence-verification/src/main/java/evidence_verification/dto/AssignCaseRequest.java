package evidence_verification.dto;

public class AssignCaseRequest {

    private String assignedInvestigator;

    public AssignCaseRequest() {
    }

    public String getAssignedInvestigator() {
        return assignedInvestigator;
    }

    public void setAssignedInvestigator(String assignedInvestigator) {
        this.assignedInvestigator = assignedInvestigator;
    }
}
