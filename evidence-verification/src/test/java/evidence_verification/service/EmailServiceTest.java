package evidence_verification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testSendVerificationEmail_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = emailService.sendVerificationEmail("user@test.com", "token123");

        assertTrue(result);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEvidenceUploadedNotification_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = emailService.sendEvidenceUploadedNotification(
                "investigator@test.com",
                "EVI-1001",
                "CASE-2026-001",
                "evidence.pdf",
                "officer@test.com",
                LocalDateTime.now(),
                "UPLOADED"
        );

        assertTrue(result);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEvidenceVerifiedNotification_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = emailService.sendEvidenceVerifiedNotification(
                "investigator@test.com",
                "EVI-1001",
                "CASE-2026-001",
                "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e",
                LocalDateTime.now(),
                "VERIFIED"
        );

        assertTrue(result);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEvidenceRejectedNotification_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = emailService.sendEvidenceRejectedNotification(
                "investigator@test.com",
                "EVI-1001",
                "CASE-2026-001",
                "evidence.pdf",
                "Blurry image quality",
                LocalDateTime.now()
        );

        assertTrue(result);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEvidenceTamperedAlert_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = emailService.sendEvidenceTamperedAlert(
                "investigator@test.com",
                "EVI-1001",
                "CASE-2026-001",
                "evidence.pdf",
                "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e",
                LocalDateTime.now()
        );

        assertTrue(result);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendCaseAssignedNotification_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = emailService.sendCaseAssignedNotification(
                "investigator@test.com",
                "CASE-2026-001",
                "Financial Fraud",
                "HIGH",
                "OPEN",
                "admin@test.com"
        );

        assertTrue(result);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendNotification_MailExceptionHandledGracefully() {
        doThrow(new MailSendException("SMTP connection timeout")).when(mailSender).send(any(SimpleMailMessage.class));

        boolean result = emailService.sendEvidenceUploadedNotification(
                "investigator@test.com",
                "EVI-1001",
                "CASE-2026-001",
                "evidence.pdf",
                "officer@test.com",
                LocalDateTime.now(),
                "UPLOADED"
        );

        assertFalse(result); // Handled safely, returns false, does NOT throw exception
    }
}
