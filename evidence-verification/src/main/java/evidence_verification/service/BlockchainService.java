package evidence_verification.service;

import evidence_verification.Entity.BlockchainRecord;

import java.util.Optional;

public interface BlockchainService {

    BlockchainRecord anchorHash(String evidenceId, String fileHash);

    Optional<BlockchainRecord> getRecord(String evidenceId);
}
