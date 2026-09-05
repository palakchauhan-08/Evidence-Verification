package evidence_verification.controller;

import evidence_verification.Entity.EmailVerificationToken;
import evidence_verification.Entity.User;
import evidence_verification.dto.LoginRequest;
import evidence_verification.repository.EmailVerificationTokenRepository;
import evidence_verification.repository.UserRepository;
import evidence_verification.service.EmailService;
import evidence_verification.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailVerificationTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthController authController;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setEmail("investigator@test.com");
        sampleUser.setPassword("$2a$10$hashedPasswordHere");
        sampleUser.setRole("INVESTIGATOR");
        sampleUser.setEmailVerified(true);
        sampleUser.setFailedLoginAttempts(0);
    }

    @Test
    void testRegister_WeakPassword_FailsValidation() {
        User weakUser = new User();
        weakUser.setEmail("weak@test.com");
        weakUser.setPassword("12345"); // Weak password

        ResponseEntity<?> response = authController.register(weakUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_AccountLockout_After5Failures() {
        when(userRepository.findByEmail("investigator@test.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest req = new LoginRequest();
        req.setEmail("investigator@test.com");
        req.setPassword("WrongPassword123!");

        // Simulate 4 failed attempts first
        for (int i = 0; i < 4; i++) {
            ResponseEntity<?> res = authController.login(req);
            assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        }

        // 5th failed attempt should trigger lockout
        ResponseEntity<?> fifthResponse = authController.login(req);

        assertEquals(HttpStatus.BAD_REQUEST, fifthResponse.getStatusCode());
        assertTrue(sampleUser.getFailedLoginAttempts() >= 5);
        assertNotNull(sampleUser.getLockoutExpiration());
    }

    @Test
    void testLogin_LockedAccount_RejectsAttempt() {
        sampleUser.setFailedLoginAttempts(5);
        sampleUser.setLockoutExpiration(LocalDateTime.now().plusMinutes(15));
        when(userRepository.findByEmail("investigator@test.com")).thenReturn(Optional.of(sampleUser));

        LoginRequest req = new LoginRequest();
        req.setEmail("investigator@test.com");
        req.setPassword("CorrectPassword123!");

        ResponseEntity<?> response = authController.login(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
