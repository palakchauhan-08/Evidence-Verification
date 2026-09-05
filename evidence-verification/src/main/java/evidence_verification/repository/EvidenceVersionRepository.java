package evidence_verification.repository;

import evidence_verification.Entity.EvidenceVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvidenceVersionRepository extends JpaRepository<EvidenceVersion, Long> {

    List<EvidenceVersion> findByEvidence_EvidenceIdOrderByVersionNumberDesc(String evidenceId);

    Optional<EvidenceVersion> findByEvidence_EvidenceIdAndVersionNumber(String evidenceId, Integer versionNumber);

    Optional<EvidenceVersion> findByVersionId(String versionId);

    Optional<EvidenceVersion> findTopByEvidence_EvidenceIdOrderByVersionNumberDesc(String evidenceId);
}
