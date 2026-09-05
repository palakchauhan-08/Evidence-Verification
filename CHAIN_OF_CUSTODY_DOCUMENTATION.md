# Digital Evidence Chain of Custody Documentation

This document describes the design, controlled action types, immutability model, deduplication logic, and testing guide for **Feature 3: Digital Evidence Chain of Custody** in the **Blockchain-Based Digital Evidence Verification System**.

---

## 1. Definition of Chain of Custody

The **Chain of Custody** is a chronological, tamper-proof record of every significant event performed on digital evidence from the instant of upload until final verification or disposition.

In courtrooms and legal proceedings, digital evidence must be backed by an unalterable history showing:
- **Who** performed the action (actor identity + security role).
- **What** action was performed (controlled action enum).
- **When** it was performed (server-side ISO timestamp).
- **Which** evidence was affected (unique `evidenceId`).
- **Status Transition** (previous status → new status).
- **Rationale / Reason** (e.g. rejection rationale, integrity mismatch cause).

---

## 2. Tracked Event Types (`ChainOfCustodyAction`)

| Action Enum | Description | Previous → New Status | Trigger Point |
|---|---|---|---|
| `EVIDENCE_UPLOADED` | Initial file upload to evidence repository | `— → UPLOADED` | `EvidenceService.uploadEvidence` |
| `HASH_GENERATED` | SHA-256 fingerprint generated | `UPLOADED → UPLOADED` | `EvidenceService.uploadEvidence` |
| `BLOCKCHAIN_ANCHORED` | On-chain ledger transaction confirmed | `UPLOADED → UPLOADED` | `EvidenceService.uploadEvidence` |
| `EVIDENCE_ACCESSED` | Authorized detail view (5-min deduplication) | `— → —` | `EvidenceController.getEvidenceDetail` |
| `REVIEW_STARTED` | Forensic examination initiated | `UPLOADED → UNDER_REVIEW` | `EvidenceService.startReview` |
| `EVIDENCE_VERIFIED` | 3-Way hash match success | `UNDER_REVIEW → VERIFIED` | `EvidenceService.verifyEvidence` |
| `RE_VERIFICATION_PERFORMED` | Re-checking an already verified item | `VERIFIED → VERIFIED` | `EvidenceService.verifyEvidence` |
| `VERIFICATION_FAILED` | Integrity mismatch detected | `[State] → TAMPERED` | `EvidenceService.verifyEvidence` |
| `EVIDENCE_REJECTED` | Rejection with logged reason | `[State] → REJECTED` | `EvidenceService.rejectEvidence` |
| `STATUS_CHANGED` | Generic status transition | `Previous → New` | Workflow actions |

---

## 3. Data Structure of a Custody Record

Each record stored in PostgreSQL `audit_logs` contains:

```json
{
  "id": 104,
  "evidenceId": "EVI-8A2F9B1C",
  "action": "EVIDENCE_VERIFIED",
  "performedBy": "analyst@example.com",
  "actorRole": "FORENSIC_ANALYST",
  "previousStatus": "UNDER_REVIEW",
  "newStatus": "VERIFIED",
  "reason": null,
  "timestamp": "2026-08-21T10:14:00",
  "details": "Evidence verified successfully against both PostgreSQL database and Blockchain record."
}
```

---

## 4. Immutability & Security Architecture

1. **Append-Only Ledger**:
   - `AuditLog` entries are append-only.
   - The backend exposes **NO** `PUT` or `DELETE` endpoints for custody records.
2. **Server-Side Event Generation**:
   - Custody records are created exclusively by trusted Spring Boot business logic (`AuditLogService`).
   - The frontend cannot submit arbitrary audit events.
3. **Role & Identity Binding**:
   - Actor email and role are extracted directly from the verified Spring Security JWT authentication context (`SecurityContextHolder`).

---

## 5. Access Event Deduplication Strategy

To prevent automatic frontend re-renders, route switches, or polling calls from polluting the audit timeline with duplicate `EVIDENCE_ACCESSED` logs:

- When `GET /api/evidence/{evidenceId}` is invoked, `AuditLogService.logAccessEvent()` queries `findFirstByEvidenceIdAndActionAndPerformedByOrderByTimestampDesc`.
- If the same user accessed the same evidence item within the past **5 minutes** (300 seconds), duplicate log creation is suppressed.
- If the last access was > 5 minutes ago or by a different user, a new `EVIDENCE_ACCESSED` event is logged.

---

## 6. Role-Based Access Rules for Custody History

| User Role | View Custody History | Modify / Delete Logs |
|---|:---:|:---:|
| **ADMIN** | ✅ View all system custody logs | ❌ Denied |
| **INVESTIGATOR** | ✅ View custody logs for authorized evidence | ❌ Denied |
| **FORENSIC_ANALYST** | ✅ View custody logs for evidence | ❌ Denied |
| **VIEWER** | ✅ Read-only view for authorized evidence | ❌ Denied |

---

## 7. API Endpoints

| Endpoint | Method | Security | Description |
|---|---|---|---|
| `/api/evidence/{id}/chain-of-custody` | `GET` | `hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')` | Returns chronological Chain of Custody entries |
| `/api/evidence/{id}/audit-logs` | `GET` | `hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')` | Returns raw audit logs (backward compatible) |

---

## 8. Database Schema Changes

Added columns to `audit_logs` table:
- `actor_role`: `VARCHAR(255)`
- `previous_status`: `VARCHAR(255)`
- `new_status`: `VARCHAR(255)`
- `reason`: `VARCHAR(1000)`
- `actor_user_id`: `BIGINT`

---

## 9. Testing Instructions

1. **Upload Sequence**:
   - Upload evidence as `INVESTIGATOR` -> Check timeline: `EVIDENCE_UPLOADED`, `HASH_GENERATED`, and `BLOCKCHAIN_ANCHORED` are automatically recorded.
2. **Access Deduplication**:
   - Open detail view -> `EVIDENCE_ACCESSED` logged. Refresh 5 times -> No duplicate logs created.
3. **Workflow & Rejection**:
   - Start review as `FORENSIC_ANALYST` -> `REVIEW_STARTED` logged.
   - Reject evidence with reason -> `EVIDENCE_REJECTED` logged with reason text.
4. **Integrity Failure**:
   - Verify a tampered file -> `VERIFICATION_FAILED` logged, status updated to `TAMPERED`.
