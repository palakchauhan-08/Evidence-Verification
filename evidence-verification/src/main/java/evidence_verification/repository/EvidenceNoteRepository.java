package evidence_verification.repository;

import evidence_verification.Entity.EvidenceNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvidenceNoteRepository extends JpaRepository<EvidenceNote, Long> {

    List<EvidenceNote> findByEvidence_EvidenceIdOrderByCreatedAtDesc(String evidenceId);

    Optional<EvidenceNote> findByNoteId(String noteId);
}
