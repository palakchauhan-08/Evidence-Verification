package evidence_verification.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import evidence_verification.Entity.EmailVerificationToken;
import evidence_verification.Entity.User;
import evidence_verification.dto.LoginRequest;
import evidence_verification.dto.LoginResponse;
import evidence_verification.repository.EmailVerificationTokenRepository;
import evidence_verification.repository.UserRepository;
import evidence_verification.service.EmailService;
import evidence_verification.service.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailVerificationTokenRepository tokenRepository,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (!isPasswordStrong(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Password must be at least 8 characters long and contain an uppercase letter, lowercase letter, number, and special character."));
        }

        var existingUserOpt = userRepository.findByEmail(user.getEmail());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.isEmailVerified()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Email already registered");
            } else {
                existingUser.setName(user.getName());
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                String reqRole = user.getRole();
                if (reqRole == null || reqRole.isEmpty() || !evidence_verification.Entity.Role.isValid(reqRole) || "ADMIN".equalsIgnoreCase(reqRole)) {
                    existingUser.setRole(evidence_verification.Entity.Role.INVESTIGATOR.name());
                } else {
                    existingUser.setRole(reqRole.toUpperCase());
                }
                User savedUser = userRepository.save(existingUser);

                List<EmailVerificationToken> oldTokens = tokenRepository.findAllByUser(savedUser);
                for (EmailVerificationToken oldToken : oldTokens) {
                    oldToken.setUsed(true);
                    tokenRepository.save(oldToken);
                }

                String tokenString = UUID.randomUUID().toString();
                EmailVerificationToken verificationToken = new EmailVerificationToken(
                        tokenString,
                        savedUser,
                        LocalDateTime.now().plusHours(24)
                );
                tokenRepository.save(verificationToken);

                boolean sent = emailService.sendVerificationEmail(savedUser.getEmail(), tokenString);
                if (!sent) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Registration updated, but failed to send verification email. Please check SMTP configuration.");
                }

                return ResponseEntity.ok("User registered successfully. Please check your email to verify your account before logging in.");
            }
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String reqRole = user.getRole();
        if (reqRole == null || reqRole.isEmpty() || !evidence_verification.Entity.Role.isValid(reqRole) || "ADMIN".equalsIgnoreCase(reqRole)) {
            user.setRole(evidence_verification.Entity.Role.INVESTIGATOR.name());
        } else {
            user.setRole(reqRole.toUpperCase());
        }

        user.setEmailVerified(false);
        user.setFailedLoginAttempts(0);
        User savedUser = userRepository.save(user);

        String tokenString = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken(
                tokenString,
                savedUser,
                LocalDateTime.now().plusHours(24)
        );
        tokenRepository.save(verificationToken);

        boolean sent = emailService.sendVerificationEmail(savedUser.getEmail(), tokenString);
        if (!sent) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("User account created, but failed to send verification email. Please check SMTP configuration.");
        }

        return ResponseEntity.ok("User registered successfully. Please check your email to verify your account before logging in.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        // Account Lockout check
        if (user.isAccountLocked()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Account is temporarily locked due to multiple failed login attempts. Please try again after 15 minutes."));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            int currentAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(currentAttempts);
            if (currentAttempts >= 5) {
                user.setLockoutExpiration(LocalDateTime.now().plusMinutes(15));
                userRepository.save(user);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Account is temporarily locked due to 5 consecutive failed login attempts. Try again after 15 minutes."));
            }
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        // Reset failed login attempts on successful password match
        user.setFailedLoginAttempts(0);
        user.setLockoutExpiration(null);
        userRepository.save(user);

        if (!user.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Please verify your email before logging in."));
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(new LoginResponse(
                token,
                "Login successful"
        ));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {

        var tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid verification token."));
        }

        EmailVerificationToken verificationToken = tokenOpt.get();

        if (verificationToken.isUsed()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Verification token has already been used."));
        }

        if (verificationToken.isExpired()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Verification token has expired."));
        }

        // Mark token as used and verify user
        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Email verified successfully. You can now login."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email address is required."));
        }

        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User with this email does not exist."));
        }

        User user = userOpt.get();
        if (user.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email is already verified."));
        }

        // Invalidate old tokens
        List<EmailVerificationToken> existingTokens = tokenRepository.findAllByUser(user);
        for (EmailVerificationToken oldToken : existingTokens) {
            oldToken.setUsed(true);
            tokenRepository.save(oldToken);
        }

        // Generate fresh token
        String newTokenString = UUID.randomUUID().toString();
        EmailVerificationToken newToken = new EmailVerificationToken(
                newTokenString,
                user,
                LocalDateTime.now().plusHours(24)
        );
        tokenRepository.save(newToken);

        // Send SMTP verification email
        boolean sent = emailService.sendVerificationEmail(user.getEmail(), newTokenString);
        if (!sent) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to send verification email. Please check SMTP configuration."));
        }

        return ResponseEntity.ok(Map.of("message", "Verification email resent successfully. Please check your inbox."));
    }
}