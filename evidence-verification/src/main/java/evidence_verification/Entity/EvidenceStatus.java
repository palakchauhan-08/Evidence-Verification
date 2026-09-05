package evidence_verification.Entity;

public enum EvidenceStatus {
    UPLOADED,
    UNDER_REVIEW,
    VERIFIED,
    REJECTED,
    TAMPERED;

    public static boolean isValid(String statusStr) {
        if (statusStr == null) return false;
        try {
            EvidenceStatus.valueOf(statusStr.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidTransition(EvidenceStatus current, EvidenceStatus target) {
        if (current == null || target == null) return false;
        if (current == target) return true; // Re-verifying or staying in same state

        switch (current) {
            case UPLOADED:
                return target == UNDER_REVIEW || target == REJECTED || target == TAMPERED;
            case UNDER_REVIEW:
                return target == VERIFIED || target == REJECTED || target == TAMPERED;
            case VERIFIED:
                return target == TAMPERED; // Can be flagged as TAMPERED if subsequent hash check fails
            case REJECTED:
                return target == UNDER_REVIEW; // Re-review allowed
            case TAMPERED:
                return target == UNDER_REVIEW; // Re-investigation allowed
            default:
                return false;
        }
    }
}
