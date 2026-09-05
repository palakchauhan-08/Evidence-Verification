package evidence_verification.Entity;

public enum Role {
    ADMIN,
    INVESTIGATOR,
    FORENSIC_ANALYST,
    VIEWER;

    public static boolean isValid(String roleStr) {
        if (roleStr == null) return false;
        try {
            Role.valueOf(roleStr.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
