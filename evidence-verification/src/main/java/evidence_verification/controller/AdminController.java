package evidence_verification.controller;

import evidence_verification.Entity.Role;
import evidence_verification.Entity.User;
import evidence_verification.dto.UserDTO;
import evidence_verification.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userRepository.findAll().stream()
                .map(UserDTO::new)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable("id") Long id, @RequestBody Map<String, String> request) {
        String newRoleStr = request.get("role");
        if (newRoleStr == null || !Role.isValid(newRoleStr)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid role specified. Allowed roles: ADMIN, INVESTIGATOR, FORENSIC_ANALYST, VIEWER"));
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        String currentRole = user.getRole();
        String targetRole = newRoleStr.toUpperCase();

        // Protection: Prevent demoting the last remaining ADMIN
        if (Role.ADMIN.name().equalsIgnoreCase(currentRole) && !Role.ADMIN.name().equalsIgnoreCase(targetRole)) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> Role.ADMIN.name().equalsIgnoreCase(u.getRole()))
                    .count();
            if (adminCount <= 1) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Cannot demote the last remaining ADMIN user."));
            }
        }

        user.setRole(targetRole);
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(new UserDTO(savedUser));
    }
}
