package evidence_verification.service;

import evidence_verification.Entity.BlockchainRecord;
import evidence_verification.repository.BlockchainRecordRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

public class MockBlockchainServiceImpl implements BlockchainService {

    private final BlockchainRecordRepository repository;

    public MockBlockchainServiceImpl(BlockchainRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public BlockchainRecord anchorHash(String evidenceId, String fileHash) {
        // Generate simulated Ethereum/Polygon style transaction hash (0x + 64 hex chars)
        String mockTxHash = generateMockTransactionHash(evidenceId, fileHash);

        BlockchainRecord record = new BlockchainRecord(
                evidenceId,
                fileHash,
                mockTxHash,
                "CONFIRMED"
        );

        return repository.save(record);
    }

    @Override
    public Optional<BlockchainRecord> getRecord(String evidenceId) {
        return repository.findByEvidenceId(evidenceId);
    }

    private String generateMockTransactionHash(String evidenceId, String fileHash) {
        try {
            String input = evidenceId + ":" + fileHash + ":" + UUID.randomUUID() + ":" + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return "0x" + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return "0x" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        }
    }
}
