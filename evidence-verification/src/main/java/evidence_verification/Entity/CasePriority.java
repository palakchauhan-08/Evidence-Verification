package evidence_verification.Entity;

public enum CasePriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static boolean isValid(String priorityStr) {
        if (priorityStr == null) return false;
        try {
            CasePriority.valueOf(priorityStr.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
