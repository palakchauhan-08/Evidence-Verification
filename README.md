# 🛡️ Blockchain-Based Digital Evidence Verification System

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.0-blue.svg)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Solidity](https://img.shields.io/badge/Solidity-0.8.20-black.svg)](https://soliditylang.org/)
[![Polygon](https://img.shields.io/badge/Blockchain-Polygon%20Amoy-purple.svg)](https://polygon.technology/)
[![Docker](https://img.shields.io/badge/Docker-Supported-blue.svg)](https://www.docker.com/)

A high-integrity, enterprise-grade digital forensics and chain of custody management platform. Built using **Spring Boot**, **React**, **PostgreSQL**, and **Solidity Smart Contracts on Polygon Amoy**, the system guarantees the authenticity, non-repudiation, and immutability of digital evidence throughout its lifecycle.

---

## 📌 Table of Contents
1. [Project Overview](#-project-overview)
2. [Key Features](#-key-features)
3. [System Architecture & Workflow](#-system-architecture--workflow)
4. [Technology Stack](#-technology-stack)
5. [How Evidence Verification Works (3-Way Hash Check)](#-how-evidence-verification-works-3-way-hash-check)
6. [Evidence Versioning](#-evidence-versioning)
7. [Smart Contract & Blockchain Integration](#-smart-contract--blockchain-integration)
8. [Security Architecture](#-security-architecture)
9. [Project Structure](#-project-structure)
10. [Docker Setup & Execution](#-docker-setup--execution)
11. [Local Development Setup](#-local-development-setup)
12. [Public & QR Code Verification](#-public--qr-code-verification)
13. [Screenshots](#-screenshots)
14. [Future Scope](#-future-scope)
15. [Author & Acknowledgments](#-author--acknowledgments)

---

## 🚀 Project Overview

In digital forensics and criminal investigations, maintaining an unalterable **Chain of Custody** is critical. Traditional evidence management solutions rely on centralized databases vulnerable to insider tampering, unauthorized modification, or data corruption.

This platform bridges enterprise software with public blockchain technology:
- Every uploaded evidence file is cryptographically hashed using **SHA-256**.
- The hash is anchored immutably to the **Polygon Amoy Testnet** via a custom **Solidity Smart Contract**.
- Complete audit trails, user actions, investigator notes, and evidence status changes are recorded in an immutable ledger.
- A **3-Way Verification Engine** compares stored database records, live re-computed file hashes, and on-chain smart contract data to instantly flag any illegal tampering.

---

## ✨ Key Features

### 🔑 Authentication & Access Control
* **JWT Authentication**: Secure stateless authentication using JSON Web Tokens.
* **Email Verification**: User registration requires OTP token verification via email.
* **Role-Based Access Control (RBAC)**: Fine-grained authorization enforcing 3 system roles:
  * `ADMIN`: User management, system configuration, global audit logs.
  * `INVESTIGATOR`: Case creation, evidence upload, versioning, notes, and chain of custody updates.
  * `AUDITOR`: Read-only access to audit logs, evidence verification, and compliance reports.

### 📁 Case & Evidence Lifecycle Management
* **Case Management**: Organize evidence into distinct legal cases with priority ratings (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) and statuses (`OPEN`, `IN_PROGRESS`, `CLOSED`, `ARCHIVED`).
* **Evidence Workflow**: Lifecycle states (`SUBMITTED`, `UNDER_REVIEW`, `VERIFIED`, `REJECTED`, `ARCHIVED`).
* **Automatic Metadata Extraction**: Captures file size, MIME type, original file name, uploader details, and timestamp.
* **Investigator Notes**: Case officers can attach timestamped, signed notes to evidence files.

### 🔗 Blockchain & Cryptographic Integrity
* **SHA-256 Hashing**: Instant client-side & server-side digest calculation.
* **Smart Contract Anchoring**: Records `evidenceId`, `fileHash`, `timestamp`, and `submitter` address on the Polygon Amoy Testnet.
* **3-Way Verification**: Compares Database Hash ↔ Live Computed File Hash ↔ On-Chain Hash.
* **Tamper Alert System**: Flags hash mismatches with high-visibility alerts.
* **Polygon Explorer Link**: Direct redirection to Polygon Amoy Block Explorer (`amoy.polygonscan.com`) for verification transparency.

### 📄 Reporting & Verification Tools
* **PDF Verification Report**: One-click generation of official cryptographic audit certificates containing metadata, hashes, verification history, and embedded QR codes (built with iText PDF).
* **QR Code Verification**: Generates unique QR codes for instant verification on mobile devices.
* **Public Verification Portal**: Allows external parties (e.g., judges, defense counsel) to verify evidence authenticity without requiring system login.

---

## 🏗️ System Architecture & Workflow

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   CLIENT LAYER                                         │
│                      React 18 + Vite SPA (Tailwind CSS)                                │
└──────────────────────────────────────────┬─────────────────────────────────────────────┘
                                           │  REST API / JWT
                                           ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                  BACKEND LAYER                                         │
│                   Spring Boot 3.4.1 (Java 17) + Spring Security                        │
│ ┌───────────────────────┬─────────────────────────┬──────────────────────────────────┐ │
│ │ Auth & Security       │ Evidence & Case Service │ Cryptographic Engine (SHA-256)   │ │
│ ├───────────────────────┼─────────────────────────┼──────────────────────────────────┤ │
│ │ Audit & Chain of Cust.│ PDF Report Generator    │ Web3j Blockchain Service         │ │
│ └───────────────────────┴─────────────────────────┴──────────────────────────────────┘ │
└──────────────┬───────────────────────────┬──────────────────────────────┬──────────────┘
               │                           │                              │
               ▼                           ▼                              ▼
 ┌───────────────────────────┐ ┌──────────────────────────┐ ┌──────────────────────────┐
 │     RELATIONAL DATA       │ │      FILE STORAGE        │ │    PUBLIC BLOCKCHAIN     │
 │       PostgreSQL 15       │ │ (Configured Local Storage│ │   Polygon Amoy Testnet   │
 │ (Metadata, Users, Cases,  │ │   Evidence File Store)   │ │ (Solidity Smart Contract │
 │  Audit Logs, Versioning)  │ │                          │ │   Hash Anchoring)        │
 └───────────────────────────┘ └──────────────────────────┘ └──────────────────────────┘
```

---

## 🛠️ Technology Stack

| Domain | Technology / Framework | Version / Details |
| :--- | :--- | :--- |
| **Frontend** | React, Vite | React 18, Vite 5, Tailwind CSS, Lucide Icons, Axios |
| **Backend** | Java, Spring Boot | Java 17, Spring Boot 3.4.1 |
| **Security** | Spring Security | JWT (jjwt), BCrypt Password Encoder, Custom Rate Limiter |
| **Database** | PostgreSQL | PostgreSQL 15, Spring Data JPA / Hibernate |
| **Storage Layer** | Local File Storage | Configured file-storage layer for raw evidence files |
| **Blockchain** | Solidity, Web3j | Solidity `^0.8.20`, Web3j Java Library, Polygon Amoy Testnet |
| **Utilities** | iText PDF, ZXing | PDF Audit Certificate Generation & QR Code Generation |
| **DevOps** | Docker, Docker Compose | Containerized Multi-tier Setup (Nginx + Spring Boot + Postgres) |
| **Testing** | JUnit 5, Mockito | Comprehensive Service & Controller Unit Tests |
| **API Tools** | Postman | REST API Collection for Testing |

---

## 🔍 How Evidence Verification Works (3-Way Hash Check)

When an auditor or investigator requests verification of an evidence file:

```
                          VERIFICATION INITIATED
                                    │
                                    ▼
       ┌────────────────────────────┼────────────────────────────┐
       │                            │                            │
       ▼                            ▼                            ▼
[ 1. DATABASE HASH ]     [ 2. LIVE FILE HASH ]        [ 3. ON-CHAIN HASH ]
Hash stored in DB        SHA-256 computed live        Retrieved from Polygon
at upload time.          from file storage layer.     Smart Contract.
       │                            │                            │
       └────────────────────────────┼────────────────────────────┘
                                    │
                                    ▼
                      3-WAY COMPARISON ENGINE
                                    │
             ┌──────────────────────┴──────────────────────┐
             │                                             │
      All 3 Hashes Match                         Any Hash Differs
             │                                             │
             ▼                                             ▼
  ┌──────────────────────┐                      ┌──────────────────────┐
  │  STATUS: VERIFIED    │                      │   STATUS: TAMPERED   │
  │ Cryptographic        │                      │ High-Priority System │
  │ Integrity Confirmed  │                      │ Security Alert Issued│
  └──────────────────────┘                      └──────────────────────┘
```

---

## 📚 Evidence Versioning

Evidence files often require updates, re-analysis, or conversion during an investigation. This system enforces strict **Evidence Versioning**:

* **Immutable Version History**: Updating evidence creates a new incremental version (e.g., `v1.0` ➔ `v2.0`) rather than overwriting existing records.
* **Distinct Cryptographic Digests**: Each version maintains its own unique SHA-256 hash, file size, upload timestamp, uploader ID, and verification status.
* **Independent Anchoring**: Every version is anchored independently on the Polygon blockchain.
* **Audit Lineage**: Full lineage tracking allows investigators to trace the evolution of an evidence file from initial seizure to final presentation.

---

## 📜 Smart Contract & Blockchain Information

The smart contract `EvidenceVerification.sol` is deployed on the **Polygon Amoy Testnet** (Chain ID: `80002`).

### Key Smart Contract Functions
* `storeEvidence(string evidenceId, string fileHash)`: Anchors a new evidence record with block timestamp and submitter wallet address.
* `getEvidence(string evidenceId)`: Retrieves stored hash, timestamp, and wallet address.
* `verifyEvidence(string evidenceId, string fileHash)`: Returns a boolean verification result on-chain.
* `storeEvidenceHash()` / `retrieveEvidenceHash()`: Java Web3j wrapper methods for backend service interaction.

### Contract Artifacts
* Contract Code: [`evidence-verification/contracts/EvidenceVerification.sol`](file:///c:/Users/Palak/Downloads/evidence-verification/evidence-verification/contracts/EvidenceVerification.sol)

---

## 🔒 Security Features

1. **Authentication & Authorization**:
   - Stateless JWT tokens passed via Authorization headers.
   - Fine-grained role checks (`@PreAuthorize("hasRole('ADMIN')")`).
2. **Data Protection & Password Hashing**:
   - Passwords hashed using `BCryptPasswordEncoder` with salt.
   - Sensitive environment variables handled via configuration properties (no hardcoded secrets).
3. **Traffic Rate Limiting**:
   - Built-in `RateLimiterFilter` preventing brute-force login attempts and DDoS attacks.
4. **Global Exception Handling**:
   - Centralized `GlobalExceptionHandler` ensures stack traces are never exposed in production responses.
5. **Chain of Custody Immutability**:
   - Audit log entries append-only; database integrity enforced with unique cryptographic identifiers.

---

## 📂 Project Structure

```text
evidence-verification/
├── .github/                       # GitHub workflow & CI configuration
├── docker-compose.yml             # Docker Compose orchestration
├── README.md                      # Project documentation
├── CASE_MANAGEMENT_DOCUMENTATION.md
├── CHAIN_OF_CUSTODY_DOCUMENTATION.md
├── EVIDENCE_WORKFLOW_DOCUMENTATION.md
├── RBAC_DOCUMENTATION.md
├── evidence-verification/         # BACKEND (Spring Boot)
│   ├── Dockerfile                 # Backend container spec
│   ├── pom.xml                    # Maven dependencies
│   ├── contracts/                 # Solidity Smart Contracts
│   │   └── EvidenceVerification.sol
│   └── src/
│       ├── main/
│       │   ├── java/evidence_verification/
│       │   │   ├── config/        # Security, CORS, Rate Limiter, Initializers
│       │   │   ├── controller/    # Auth, Case, Evidence, Admin, Public Verification
│       │   │   ├── dto/           # Data Transfer Objects
│       │   │   ├── Entity/        # JPA Entities (User, Case, Evidence, AuditLog)
│       │   │   ├── repository/    # Spring Data JPA Repositories
│       │   │   └── service/       # Business logic, Web3j, PDF & QR generators
│       │   └── resources/
│       │       └── application.properties
│       └── test/                  # Unit & Integration Tests
└── frontend/                      # FRONTEND (React + Vite)
    ├── Dockerfile                 # Frontend multi-stage container build
    ├── nginx.conf                 # Production Nginx reverse proxy configuration
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── components/            # Reusable UI components & navigation
        ├── context/               # Auth Context Provider
        ├── pages/                 # Dashboard, Cases, Evidence, Audit Trail, Public Portal
        └── services/              # Axios API service handlers
```

---

## 🐳 Docker Setup & Execution

The system is fully containerized using **Docker** and **Docker Compose**, encapsulating PostgreSQL, the Spring Boot Backend, and the Nginx-served React Frontend.

### 1. Build and Run All Services
To build images and start containers:
```bash
docker compose up --build
```

To run in detached mode (background):
```bash
docker compose up --build -d
```

### 2. Check Running Containers
```bash
docker compose ps
```

### 3. View Logs
```bash
docker compose logs -f
```

### 4. Stop and Remove Containers
```bash
docker compose down
```

---

## 💻 Local Development Setup

### Prerequisites
- **Java**: JDK 17 or higher
- **Node.js**: v18.x or higher
- **Database**: PostgreSQL 15 (running on port `5432`)
- **Maven**: 3.8+

### Step 1: Database Setup
Create a PostgreSQL database named `evidence_db`:
```sql
CREATE DATABASE evidence_db;
```

### Step 2: Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd evidence-verification
   ```
2. Configure environment variables or edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/evidence_db
   spring.datasource.username=postgres
   spring.datasource.password=YOUR_DB_PASSWORD
   ```
3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
   The backend will start at `http://localhost:8080`.

### Step 3: Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
   The frontend application will be accessible at `http://localhost:3000` or `http://localhost:5173`.

---

## 🌐 Verification Workflow

```text
1. Investigator logs in with JWT credentials.
2. Creates a Case and uploads digital evidence file.
3. System generates SHA-256 hash digest & stores raw file in configured file storage.
4. Evidence hash is anchored to Polygon Amoy Testnet via Smart Contract.
5. System generates a downloadable PDF Certificate & QR Code for the evidence.
6. Public user / Auditor scans QR code or enters Evidence ID on the Public Portal.
7. System performs 3-Way Hash Check (DB ↔ Storage ↔ Polygon Smart Contract).
8. Instant verification badge is rendered (VERIFIED / TAMPERED).
```

---

## 🖼️ Screenshots

> *Placeholder section for interface walkthrough screenshots.*

| Feature | Preview |
| :--- | :--- |
| **System Dashboard** | *![Dashboard Placeholder](docs/screenshots/dashboard.png)* |
| **Case Management & Evidence Details** | *![Cases Placeholder](docs/screenshots/case-details.png)* |
| **Public Verification Portal** | *![Public Verification Placeholder](docs/screenshots/public-verification.png)* |
| **PDF Audit Certificate & QR Code** | *![PDF Report Placeholder](docs/screenshots/pdf-report.png)* |

---

## 🔮 Future Scope

- **Decentralized Object Storage**: Integration with AWS S3 / IPFS for decentralized evidence file redundancy.
- **Multi-Signature Destruction Workflows**: Requiring multiple judicial approvals before archiving/deleting expired evidence.
- **Zero-Knowledge Proofs (ZKP)**: Verifying evidence attributes without disclosing underlying sensitive content.
- **Mobile Field Agent App**: React Native app for field officers to capture and hash evidence directly at crime scenes.

---

## 👤 Author & Contact

**Palak Chauhan**  
* **Email**: palakchauhan824@gmail.com  
* **GitHub**: [@palakchauhan-08](https://github.com/palakchauhan-08)  
* **Project Repository**: [Evidence-Verification](https://github.com/palakchauhan-08/Evidence-Verification)

---
*Built with ❤️ for Digital Forensics Integrity & Cryptographic Security.*
