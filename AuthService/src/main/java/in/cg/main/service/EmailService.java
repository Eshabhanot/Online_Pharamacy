package in.cg.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String fromAddress;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendPasswordResetOtp(String toEmail, String otp, int validityMinutes) {
        if (!isMailConfigured()) {
            logger.info("Mail delivery is disabled. OTP email to {} was skipped.", toEmail);
            return false;
        }

        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException("Mail sender address is not configured");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your Pharmacy App password reset OTP");
        message.setText(buildPasswordResetBody(otp, validityMinutes));

        try {
            mailSender.send(message);
            logger.info("Password reset OTP email sent to {}", toEmail);
            return true;
        } catch (MailException ex) {
            logger.error("Failed to send OTP email to {} via {}:{} with username {}. Reason: {}",
                    toEmail, "smtp.gmail.com", "587", mailUsername, ex.getMessage(), ex);
            throw new IllegalStateException("Failed to send OTP email", ex);
        }
    }

    public boolean sendRegistrationSuccessEmail(String toEmail, String customerName) {
        if (!isMailConfigured()) {
            logger.info("Mail delivery is disabled. Registration email to {} was skipped.", toEmail);
            return false;
        }

        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException("Mail sender address is not configured");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Registration successful");
        message.setText(buildRegistrationSuccessBody(customerName));

        try {
            mailSender.send(message);
            logger.info("Registration email sent to {}", toEmail);
            return true;
        } catch (MailException ex) {
            logger.error("Failed to send registration email to {} via {}:{} with username {}. Reason: {}",
                    toEmail, "smtp.gmail.com", "587", mailUsername, ex.getMessage(), ex);
            throw new IllegalStateException("Failed to send registration email", ex);
        }
    }

    private String buildPasswordResetBody(String otp, int validityMinutes) {
        return """
                Hello,

                Your password reset OTP is: %s

                This OTP is valid for %d minutes.
                If you did not request a password reset, please ignore this email.

                Regards,
                Pharmacy App
                """.formatted(otp, validityMinutes);
    }

    private String buildRegistrationSuccessBody(String customerName) {
        String resolvedName = customerName == null || customerName.isBlank() ? "Customer" : customerName;
        return """
                Hello %s,

                Your Pharmacy App account has been created successfully.

                You can now log in and start placing orders.

                Regards,
                Pharmacy App
                """.formatted(resolvedName);
    }

    private boolean isMailConfigured() {
        return mailEnabled
                && mailUsername != null
                && !mailUsername.isBlank()
                && mailPassword != null
                && !mailPassword.isBlank();
    }
}
