package in.cg.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean mailEnabled;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.enabled:false}") boolean mailEnabled,
                        @Value("${app.mail.from:no-reply@pharmacy.local}") String fromAddress) {
        this.mailSender = mailSender;
        this.mailEnabled = mailEnabled;
        this.fromAddress = fromAddress;
    }

    public void sendRegistrationEmail(String recipient, String customerName) {
        if (recipient == null || recipient.isBlank()) {
            return;
        }

        if (!mailEnabled) {
            log.info("Registration email skipped because mail is disabled for {}", recipient);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject("Welcome to Pharmacy App");
        message.setText("Hi " + customerName + ",\n\nYour account has been created successfully.\n\nRegards,\nPharmacy App Team");

        try {
            mailSender.send(message);
            log.info("Registration email sent from {} to {}", fromAddress, recipient);
        } catch (Exception ex) {
            log.error("Failed to send registration email to {}: {}", recipient, ex.getMessage(), ex);
        }
    }
}
