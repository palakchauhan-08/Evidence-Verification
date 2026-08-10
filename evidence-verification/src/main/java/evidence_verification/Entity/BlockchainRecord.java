package evidence_verification.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_records")
public class BlockchainRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String evidenceId;

    @Column(nullable = false)
    private String fileHash;

    @Column(nullable = false, unique = true)
    private String transactionHash;

    private LocalDateTime blockchainTimestamp;

    private String status;

    public BlockchainRecord() {
        this.blockchainTimestamp = LocalDateTime.now();
    }

    public BlockchainRecord(String evidenceId, String fileHash, String transactionHash, String status) {
        this.evidenceId = evidenceId;
        this.fileHash = fileHash;
        this.transactionHash = transactionHash;
        this.blockchainTimestamp = LocalDateTime.now();
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public LocalDateTime getBlockchainTimestamp() {
        return blockchainTimestamp;
    }

    public void setBlockchainTimestamp(LocalDateTime blockchainTimestamp) {
        this.blockchainTimestamp = blockchainTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
