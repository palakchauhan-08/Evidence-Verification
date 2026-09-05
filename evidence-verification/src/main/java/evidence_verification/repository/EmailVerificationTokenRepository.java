package evidence_verification.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import evidence_verification.Entity.EmailVerificationToken;
import evidence_verification.Entity.User;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUser(User user);

    List<EmailVerificationToken> findAllByUser(User user);

    void deleteByUser(User user);
}
