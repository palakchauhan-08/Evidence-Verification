# Polygon Amoy Real Blockchain Integration & Deployment Guide

## Status Summary

- **Real Blockchain Integration**: Fully implemented and **deployment-ready** using Web3j, Spring Boot 3.4.1, and Solidity `^0.8.20`.
- **Smart Contract**: Ready in `evidence-verification/contracts/EvidenceVerification.sol`.
- **Live Deployment Status**: **Intentionally Pending**. No smart contract deployment has been executed, and no live testnet MATIC/POL is required to run the project.
- **Default Mode**: **Mock Blockchain Mode** (`BLOCKCHAIN_MODE=mock`) is active by default. The complete application runs out-of-the-box locally without requiring MetaMask, a funded wallet, or live network connection.
- **Security**: Zero private keys or credentials are included or hardcoded in the codebase.

---

## 1. Local Development (Default Mock Mode)

By default, the application uses `MockBlockchainServiceImpl`, which simulates on-chain anchoring and transaction hashes locally in PostgreSQL. No setup or external services are needed.

To run locally in Mock Mode:
- Keep `BLOCKCHAIN_MODE=mock` (or leave it unset).
- Start the Spring Boot backend (`mvnw spring-boot:run`).
- Start the React frontend (`npm run dev`).

---

## 2. Activating Real Blockchain Mode (Polygon Amoy Testnet)

When you are ready to deploy to the live **Polygon Amoy Testnet (Chain ID 80002)**, follow these steps:

### Step 1: Deploy Smart Contract via Remix IDE
1. Open [Remix Ethereum IDE](https://remix.ethereum.org/).
2. Create a file named `EvidenceVerification.sol` and paste code from `evidence-verification/contracts/EvidenceVerification.sol`.
3. In the **Solidity Compiler** tab:
   - Select compiler version `0.8.20`.
   - Click **Compile EvidenceVerification.sol**.
4. In the **Deploy & Run Transactions** tab:
   - Environment: Select **Injected Provider - MetaMask** (connected to Polygon Amoy Testnet, Chain ID 80002).
   - Ensure your wallet has test MATIC from the [Polygon Faucet](https://faucet.polygon.technology/).
   - Click **Deploy** and approve the transaction in MetaMask.
5. Copy the **Deployed Contract Address**.

### Step 2: Configure Environment Variables
Set the following environment variables on your system or in your launch environment:

```env
BLOCKCHAIN_MODE=real
BLOCKCHAIN_RPC_URL=https://rpc-amoy.polygon.technology
BLOCKCHAIN_PRIVATE_KEY=0x<your_amoy_testnet_private_key>
BLOCKCHAIN_CONTRACT_ADDRESS=0x<your_deployed_contract_address>
BLOCKCHAIN_CHAIN_ID=80002
```

> [!WARNING]
> Never commit your real private key or wallet secrets to Git source control!

---

## 3. How the Smart Contract & Verification Work

1. **Anchoring Hash**:
   `storeEvidence(evidenceId, fileHash)`:
   - Verifies parameters and checks that `records[evidenceId].timestamp == 0` (prevents overwriting).
   - Saves `fileHash`, block timestamp, and submitter address on Polygon Amoy.
   - Emits `EvidenceAnchored` event.

2. **3-Way Verification**:
   `verifyEvidence(file)`:
   - Calculates local SHA-256 hash of the evidence file.
   - Reads database SHA-256 hash from PostgreSQL.
   - Queries smart contract view function `getEvidence(evidenceId)` on Polygon Amoy.
   - Returns `VERIFIED` only when `calculatedHash == databaseHash == blockchainHash`.
