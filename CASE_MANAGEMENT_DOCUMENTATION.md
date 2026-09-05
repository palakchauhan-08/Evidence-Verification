# Case Management Module Documentation

This document describes the design, data model, RBAC rules, REST APIs, database schema, and testing guide for **Feature 4: Case Management** in the **Blockchain-Based Digital Evidence Verification System**.

---

## 1. Objective & Purpose

The **Case Management Module** transforms the system architecture from isolated file uploads to structured digital investigations:

```
Case (CASE-2026-001) → Multiple Evidence Items → Investigation → Verification → Chain of Custody
```

It enables digital forensic examiners and investigators to organize multiple evidence files (e.g. disk images, memory dumps, network logs, emails) under a unified case envelope while preserving evidence integrity, cryptographic SHA-256 hashes, and Polygon blockchain records.

---

## 2. Case Entity Data Structure (`cases` table)

| Column Name | Type | Constraints | Description |
|---|---|---|---|
| `id` | `BIGINT` | Primary Key, Auto-Increment | Database surrogate key |
| `case_id` | `VARCHAR(255)` | Unique, Not Null, Indexed | Server-generated identifier (`CASE-YYYY-XXX`) |
| `title` | `VARCHAR(255)` | Not Null | Investigation case title |
| `description` | `VARCHAR(2000)` | Nullable | Detailed incident & examination summary |
| `priority` | `VARCHAR(50)` | Default `'MEDIUM'` | Controlled priority: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `status` | `VARCHAR(50)` | Default `'OPEN'` | Controlled status: `OPEN`, `IN_PROGRESS`, `ON_HOLD`, `CLOSED` |
| `assigned_investigator` | `VARCHAR(255)` | Nullable | Email of assigned investigator |
| `created_by` | `VARCHAR(255)` | Not Null | Email of creating user from JWT |
| `created_at` | `TIMESTAMP` | Not Null | Server-generated creation timestamp |
| `updated_at` | `TIMESTAMP` | Not Null | Last modification timestamp |
| `closed_at` | `TIMESTAMP` | Nullable | Timestamp when case status became `CLOSED` |

---

## 3. Controlled Case Lifecycle & Transitions

```stateDiagram-v2
    [*] --> OPEN : Case Created
    OPEN --> IN_PROGRESS : Investigation Initiated
    OPEN --> ON_HOLD : Investigation Paused
    OPEN --> CLOSED : Case Resolved
    IN_PROGRESS --> ON_HOLD : Investigation Paused
    IN_PROGRESS --> CLOSED : Case Resolved
    ON_HOLD --> IN_PROGRESS : Investigation Resumed
    CLOSED --> IN_PROGRESS : Explicit Re-Open Action (Logged)
```

---

## 4. Evidence ↔ Case Relationship

- **`@ManyToOne` / `@OneToMany`**: Each case can contain multiple evidence records. Each evidence item belongs to at most one case.
- **Nullable Foreign Key**: The `case_id` column on the `evidence` table is nullable, ensuring all pre-existing evidence records continue operating without interruption.
- **Non-Destructive Association**: Adding or removing evidence from a case alters only metadata. It does **NOT** regenerate SHA-256 hashes, re-anchor to blockchain, alter original upload timestamps, or delete files.

---

## 5. Role-Based Access Control (RBAC) Matrix

| Workflow Action | ADMIN | INVESTIGATOR | FORENSIC_ANALYST | VIEWER |
|---|:---:|:---:|:---:|:---:|
| **Create Case** | ✅ | ✅ | ❌ (403) | ❌ (403) |
| **View Cases** | ✅ (All) | ✅ (Assigned/Created) | ✅ (All) | ✅ (All) |
| **Assign Investigator** | ✅ | ✅ | ❌ (403) | ❌ (403) |
| **Update Case Status / Close** | ✅ | ✅ | ✅ | ❌ (403) |
| **Associate / Remove Evidence** | ✅ | ✅ | ❌ (403) | ❌ (403) |
| **View Custody Timeline** | ✅ | ✅ | ✅ | ✅ |

---

## 6. Case-Related Audit Events (`ChainOfCustodyAction`)

Every case management action automatically generates an immutable Chain of Custody entry:

1. `CASE_CREATED`: Recorded when a case is created.
2. `CASE_UPDATED`: Recorded when case title, description, or priority is updated.
3. `CASE_ASSIGNED`: Recorded when an investigator is assigned.
4. `CASE_REASSIGNED`: Recorded when a case is reassigned to another investigator.
5. `CASE_STATUS_CHANGED`: Recorded during status transitions (e.g. `OPEN → IN_PROGRESS`).
6. `EVIDENCE_ADDED_TO_CASE`: Recorded when an evidence item is associated with a case.
7. `EVIDENCE_REMOVED_FROM_CASE`: Recorded when an evidence item is disassociated from a case.
8. `CASE_CLOSED`: Recorded when a case is closed.

---

## 7. REST API Endpoints

| Method & Path | Security Rule | Description |
|---|---|---|
| `POST /api/cases` | `hasAnyRole('ADMIN', 'INVESTIGATOR')` | Create new investigation case |
| `GET /api/cases` | `hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')` | List cases with search & filter params |
| `GET /api/cases/{caseId}` | `hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST', 'VIEWER')` | Get case detail & associated evidence (IDOR protected) |
| `PATCH /api/cases/{caseId}/status` | `hasAnyRole('ADMIN', 'INVESTIGATOR', 'FORENSIC_ANALYST')` | Update status (e.g. `OPEN → IN_PROGRESS`, `CLOSED`) |
| `PATCH /api/cases/{caseId}/assign` | `hasAnyRole('ADMIN', 'INVESTIGATOR')` | Assign/reassign investigator |
| `POST /api/cases/{caseId}/evidence/{evidenceId}` | `hasAnyRole('ADMIN', 'INVESTIGATOR')` | Associate evidence to case |
| `DELETE /api/cases/{caseId}/evidence/{evidenceId}` | `hasAnyRole('ADMIN', 'INVESTIGATOR')` | Disassociate evidence from case |

---

## 8. Testing Instructions

1. **Case Creation**:
   - Log in as `investigator@example.com` -> Click "Create Case" -> Enter Title & Priority -> Server generates `CASE-2026-001` with `status = OPEN` and logs `CASE_CREATED`.
2. **Evidence Association**:
   - Open case `CASE-2026-001` -> Click "Add Evidence" -> Select evidence item -> Item appears in case evidence table, metrics summary updates, and `EVIDENCE_ADDED_TO_CASE` is logged.
3. **Status Workflow & Closure**:
   - Update status to `IN_PROGRESS` -> Logs `CASE_STATUS_CHANGED`.
   - Close case -> Status becomes `CLOSED`, `closedAt` timestamp is recorded, and `CASE_CLOSED` is logged.
4. **Security & IDOR Enforcement**:
   - Log in as another investigator and attempt to query an unassigned case ID directly -> Backend returns **HTTP 403 Forbidden**.
