package evidence_verification.Entity;

public enum ChainOfCustodyAction {
    EVIDENCE_UPLOADED,
    HASH_GENERATED,
    BLOCKCHAIN_ANCHORED,
    EVIDENCE_ACCESSED,
    REVIEW_STARTED,
    EVIDENCE_VERIFIED,
    VERIFICATION_FAILED,
    EVIDENCE_REJECTED,
    STATUS_CHANGED,
    RE_VERIFICATION_PERFORMED,
    CASE_CREATED,
    CASE_UPDATED,
    CASE_ASSIGNED,
    CASE_REASSIGNED,
    CASE_STATUS_CHANGED,
    EVIDENCE_ADDED_TO_CASE,
    EVIDENCE_REMOVED_FROM_CASE,
    CASE_CLOSED,
    EMAIL_NOTIFICATION_SENT,
    EMAIL_NOTIFICATION_FAILED,
    QR_CODE_GENERATED,
    INTEGRITY_COMPROMISED,
    INVESTIGATOR_NOTE_ADDED,
    INVESTIGATOR_NOTE_UPDATED,
    INVESTIGATOR_NOTE_DELETED,
    EVIDENCE_VERSION_CREATED,
    EVIDENCE_VERSION_UPLOADED;

    public static boolean isValid(String actionStr) {
        if (actionStr == null) return false;
        try {
            ChainOfCustodyAction.valueOf(actionStr.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
