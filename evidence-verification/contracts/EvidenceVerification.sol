// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

/**
 * @title EvidenceVerification
 * @dev Smart contract for anchoring and verifying digital evidence SHA-256 hashes on Polygon Amoy testnet.
 */
contract EvidenceVerification {

    struct EvidenceRecord {
        string evidenceId;
        string fileHash;
        uint256 timestamp;
        address submitter;
    }

    // Mapping from unique evidenceId (e.g., "EVI-8A2F9B1C") to EvidenceRecord
    mapping(string => EvidenceRecord) private records;

    // Total count of anchored evidence records
    uint256 public totalRecords;

    // Event emitted when evidence is successfully anchored on-chain
    event EvidenceAnchored(
        string indexed evidenceId,
        string fileHash,
        uint256 timestamp,
        address indexed submitter
    );

    /**
     * @notice Anchor a new digital evidence hash on-chain.
     * @param evidenceId The unique identifier of the evidence record.
     * @param fileHash The SHA-256 cryptographic hash of the evidence file.
     */
    function storeEvidence(string memory evidenceId, string memory fileHash) external {
        require(bytes(evidenceId).length > 0, "Evidence ID cannot be empty");
        require(bytes(fileHash).length > 0, "File hash cannot be empty");
        require(records[evidenceId].timestamp == 0, "Evidence ID already exists on-chain");

        records[evidenceId] = EvidenceRecord({
            evidenceId: evidenceId,
            fileHash: fileHash,
            timestamp: block.timestamp,
            submitter: msg.sender
        });

        totalRecords += 1;

        emit EvidenceAnchored(evidenceId, fileHash, block.timestamp, msg.sender);
    }

    /**
     * @notice Retrieve stored evidence details by evidenceId.
     * @param evidenceId The unique identifier of the evidence record.
     * @return fileHash The SHA-256 hash stored on-chain.
     * @return timestamp The block timestamp when anchored.
     * @return submitter The Ethereum/Polygon wallet address that submitted the transaction.
     */
    function getEvidence(string memory evidenceId)
        external
        view
        returns (
            string memory fileHash,
            uint256 timestamp,
            address submitter
        )
    {
        require(records[evidenceId].timestamp > 0, "Evidence ID not found on-chain");
        EvidenceRecord memory record = records[evidenceId];
        return (record.fileHash, record.timestamp, record.submitter);
    }

    /**
     * @notice Verify if a given file hash matches the hash stored for an evidence ID.
     * @param evidenceId The unique identifier of the evidence.
     * @param fileHash The SHA-256 hash to verify.
     * @return matchResult True if stored hash matches provided fileHash, false otherwise.
     */
    function verifyEvidence(string memory evidenceId, string memory fileHash)
        external
        view
        returns (bool matchResult)
    {
        if (records[evidenceId].timestamp == 0) {
            return false;
        }
        return keccak256(bytes(records[evidenceId].fileHash)) == keccak256(bytes(fileHash));
    }
}
