package evidence_verification.config;

import evidence_verification.Entity.Evidence;
import evidence_verification.Entity.EvidenceStatus;
import evidence_verification.Entity.Role;
import evidence_verification.Entity.User;
import evidence_verification.repository.EvidenceRepository;
import evidence_verification.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EvidenceRepository evidenceRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, EvidenceRepository evidenceRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.evidenceRepository = evidenceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Migrate legacy roles in existing users to standard INVESTIGATOR role
        List<User> users = userRepository.findAll();
        for (User user : users) {
            String roleStr = user.getRole();
            if (roleStr == null || roleStr.trim().isEmpty() || "VERIFIER".equalsIgnoreCase(roleStr)) {
                user.setRole(Role.INVESTIGATOR.name());
                userRepository.save(user);
            }
        }

        // 2. Ensure a default ADMIN account exists for system governance
        var existingAdminOpt = userRepository.findByEmail("admin@example.com");
        if (existingAdminOpt.isPresent()) {
            User adminUser = existingAdminOpt.get();
            adminUser.setRole(Role.ADMIN.name());
            adminUser.setPassword(passwordEncoder.encode("Admin123!"));
            adminUser.setEmailVerified(true);
            userRepository.save(adminUser);
        } else {
            User defaultAdmin = new User();
            defaultAdmin.setName("System Admin");
            defaultAdmin.setEmail("admin@example.com");
            defaultAdmin.setPassword(passwordEncoder.encode("Admin123!"));
            defaultAdmin.setRole(Role.ADMIN.name());
            defaultAdmin.setEmailVerified(true);
            userRepository.save(defaultAdmin);
        }

        // 3. Migrate any legacy evidence records to default UPLOADED status
        List<Evidence> evidenceList = evidenceRepository.findAll();
        for (Evidence ev : evidenceList) {
            if (ev.getStatus() == null || ev.getStatus().trim().isEmpty()) {
                ev.setStatus(EvidenceStatus.UPLOADED.name());
                evidenceRepository.save(ev);
            }
        }
    }
}
