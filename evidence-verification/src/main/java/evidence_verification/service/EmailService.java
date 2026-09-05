package evidence_verification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendVerificationEmail(String toEmail, String token) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            
            SimpleMailMessage message = new SimpleMailMessage();
            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom);
            }
            message.setTo(toEmail);
            message.setSubject("Verify your email - Blockchain Digital Evidence Verification System");
            message.setText("Welcome to the Blockchain Digital Evidence Verification System!\n\n" +
                    "Please verify your email address by clicking the link below:\n" +
                    verificationUrl + "\n\n" +
                    "If you did not register for an account, please ignore this email.\n\n" +
                    "This link will expire in 24 hours.");

            mailSender.send(message);
            logger.info("Verification email successfully sent to {}", toEmail);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }

    public boolean sendEvidenceUploadedNotification(String recipientEmail, String evidenceId, String caseId, String fileName, String uploadedBy, java.time.LocalDateTime uploadedAt, String status) {
        try {
            SimpleMailMessage message = prepareMail(recipientEmail, "Evidence Uploaded — " + evidenceId);
            message.setText("Blockchain Digital Evidence Verification System\n\n" +
                    "NOTIFICATION: Digital Evidence File Uploaded\n\n" +
                    "Evidence ID: " + evidenceId + "\n" +
                    "Case ID: " + (caseId != null ? caseId : "Unassigned") + "\n" +
                    "File Name: " + fileName + "\n" +
                    "Uploaded By: " + uploadedBy + "\n" +
                    "Upload Timestamp: " + (uploadedAt != null ? uploadedAt.toString() : "N/A") + "\n" +
                    "Current Status: " + status + "\n\n" +
                    "Please log into the system to view evidence details.");
            mailSender.send(message);
            logger.info("Evidence uploaded notification email sent to {}", recipientEmail);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send evidence uploaded notification email to {}: {}", recipientEmail, e.getMessage());
            return false;
        }
    }

    public boolean sendEvidenceVerifiedNotification(String recipientEmail, String evidenceId, String caseId, String fileHash, java.time.LocalDateTime timestamp, String status) {
        try {
            SimpleMailMessage message = prepareMail(recipientEmail, "Evidence Verification Completed — " + evidenceId);
            message.setText("Blockchain Digital Evidence Verification System\n\n" +
                    "NOTIFICATION: Evidence Verification Completed\n\n" +
                    "Evidence ID: " + evidenceId + "\n" +
                    "Case ID: " + (caseId != null ? caseId : "Unassigned") + "\n" +
                    "Verification Result: VERIFIED (3-Way Hash Match Confirmed)\n" +
                    "SHA-256 Hash: " + fileHash + "\n" +
                    "Verification Timestamp: " + (timestamp != null ? timestamp.toString() : "N/A") + "\n" +
                    "Current Status: " + status + "\n\n" +
                    "The cryptographic integrity of this evidence has been verified against PostgreSQL storage and Polygon Amoy blockchain ledger.");
            mailSender.send(message);
            logger.info("Evidence verified notification email sent to {}", recipientEmail);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send evidence verified notification email to {}: {}", recipientEmail, e.getMessage());
            return false;
        }
    }

    public boolean sendEvidenceRejectedNotification(String recipientEmail, String evidenceId, String caseId, String fileName, String rejectionReason, java.time.LocalDateTime timestamp) {
        try {
            SimpleMailMessage message = prepareMail(recipientEmail, "Evidence Rejected — " + evidenceId);
            message.setText("Blockchain Digital Evidence Verification System\n\n" +
                    "NOTIFICATION: Evidence Status Changed to REJECTED\n\n" +
                    "Evidence ID: " + evidenceId + "\n" +
                    "Case ID: " + (caseId != null ? caseId : "Unassigned") + "\n" +
                    "File Name: " + fileName + "\n" +
                    "Status: REJECTED\n" +
                    "Rejection Reason: " + (rejectionReason != null ? rejectionReason : "N/A") + "\n" +
                    "Timestamp: " + (timestamp != null ? timestamp.toString() : "N/A") + "\n\n" +
                    "Please log into the system to review rejection details.");
            mailSender.send(message);
            logger.info("Evidence rejected notification email sent to {}", recipientEmail);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send evidence rejected notification email to {}: {}", recipientEmail, e.getMessage());
            return false;
        }
    }

    public boolean sendEvidenceTamperedAlert(String recipientEmail, String evidenceId, String caseId, String fileName, String fileHash, java.time.LocalDateTime timestamp) {
        try {
            SimpleMailMessage message = prepareMail(recipientEmail, "Evidence Integrity Alert — " + evidenceId);
            message.setText("Blockchain Digital Evidence Verification System\n\n" +
                    "CRITICAL ALERT: Evidence Integrity Mismatch / Tampering Detected\n\n" +
                    "Evidence ID: " + evidenceId + "\n" +
                    "Case ID: " + (caseId != null ? caseId : "Unassigned") + "\n" +
                    "File Name: " + fileName + "\n" +
                    "Integrity Result: TAMPERED / COMPROMISED\n" +
                    "SHA-256 Hash: " + fileHash + "\n" +
                    "Detection Timestamp: " + (timestamp != null ? timestamp.toString() : "N/A") + "\n" +
                    "Current Status: TAMPERED\n\n" +
                    "WARNING: Cryptographic hash mismatch was detected during verification. Immediate forensic investigation is required.");
            mailSender.send(message);
            logger.info("Evidence tampered alert email sent to {}", recipientEmail);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send evidence tampered alert email to {}: {}", recipientEmail, e.getMessage());
            return false;
        }
    }

    public boolean sendCaseAssignedNotification(String recipientEmail, String caseId, String caseTitle, String priority, String status, String createdBy) {
        try {
            SimpleMailMessage message = prepareMail(recipientEmail, "Case Assigned — " + caseId);
            message.setText("Blockchain Digital Evidence Verification System\n\n" +
                    "NOTIFICATION: Investigation Case Assignment\n\n" +
                    "Case ID: " + caseId + "\n" +
                    "Case Title: " + caseTitle + "\n" +
                    "Priority: " + priority + "\n" +
                    "Status: " + status + "\n" +
                    "Assigned Investigator: " + recipientEmail + "\n" +
                    "Created By: " + createdBy + "\n" +
                    "Assignment Timestamp: " + java.time.LocalDateTime.now() + "\n\n" +
                    "You have been assigned to this investigation case. Please log into the portal to review case details and associated evidence.");
            mailSender.send(message);
            logger.info("Case assigned notification email sent to {}", recipientEmail);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send case assigned notification email to {}: {}", recipientEmail, e.getMessage());
            return false;
        }
    }

    private SimpleMailMessage prepareMail(String toEmail, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setTo(toEmail);
        message.setSubject(subject);
        return message;
    }
}
