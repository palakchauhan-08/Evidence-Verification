package evidence_verification.Entity;

public enum CaseStatus {
    OPEN,
    IN_PROGRESS,
    ON_HOLD,
    CLOSED;

    public static boolean isValid(String statusStr) {
        if (statusStr == null) return false;
        try {
            CaseStatus.valueOf(statusStr.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidTransition(CaseStatus from, CaseStatus to) {
        if (from == null || to == null) return false;
        if (from == to) return true;

        switch (from) {
            case OPEN:
                return to == CaseStatus.IN_PROGRESS || to == CaseStatus.ON_HOLD || to == CaseStatus.CLOSED;
            case IN_PROGRESS:
                return to == CaseStatus.ON_HOLD || to == CaseStatus.CLOSED || to == CaseStatus.OPEN;
            case ON_HOLD:
                return to == CaseStatus.IN_PROGRESS || to == CaseStatus.CLOSED || to == CaseStatus.OPEN;
            case CLOSED:
                // Reopening a closed case is allowed via explicit authorized action
                return to == CaseStatus.IN_PROGRESS || to == CaseStatus.OPEN;
            default:
                return false;
        }
    }
}
