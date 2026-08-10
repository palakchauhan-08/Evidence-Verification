package evidence_verification.repository;

import evidence_verification.Entity.BlockchainRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockchainRecordRepository extends JpaRepository<BlockchainRecord, Long> {

    Optional<BlockchainRecord> findByEvidenceId(String evidenceId);
}
