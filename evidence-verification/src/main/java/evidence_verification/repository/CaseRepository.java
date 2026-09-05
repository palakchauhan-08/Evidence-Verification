package evidence_verification.repository;

import evidence_verification.Entity.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    Optional<Case> findByCaseId(String caseId);

    List<Case> findByAssignedInvestigator(String assignedInvestigator);

    List<Case> findByCreatedBy(String createdBy);

    @Query("SELECT c FROM Case c WHERE " +
           "(CAST(:search AS string) IS NULL OR LOWER(c.caseId) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) AND " +
           "(CAST(:status AS string) IS NULL OR UPPER(c.status) = UPPER(CAST(:status AS string))) AND " +
           "(CAST(:priority AS string) IS NULL OR UPPER(c.priority) = UPPER(CAST(:priority AS string))) AND " +
           "(CAST(:investigator AS string) IS NULL OR LOWER(c.assignedInvestigator) LIKE LOWER(CONCAT('%', CAST(:investigator AS string), '%'))) " +
           "ORDER BY c.createdAt DESC")
    List<Case> searchAndFilterCases(
            @Param("search") String search,
            @Param("status") String status,
            @Param("priority") String priority,
            @Param("investigator") String investigator
    );
}
