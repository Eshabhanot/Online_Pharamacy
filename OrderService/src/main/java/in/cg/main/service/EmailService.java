package in.cg.main.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import in.cg.main.entities.Order;
import in.cg.main.entities.OrderItem;
import in.cg.main.util.OrderStatusFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

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

    public void sendOrderEmail(String recipient, String subject, String body) {
        if (!isDeliverableRecipient(recipient)) {
            log.info("Order email skipped because recipient is not deliverable: {}", recipient);
            return;
        }

        if (!mailEnabled) {
            log.info("Order email skipped because mail is disabled for {}", recipient);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("Order email sent from {} to {} with subject {}", fromAddress, recipient, subject);
        } catch (Exception ex) {
            log.error("Failed to send order email to {}: {}", recipient, ex.getMessage(), ex);
        }
    }

    public void sendOrderConfirmationEmail(Order order, String customerEmail) {
        if (!isDeliverableRecipient(customerEmail)) {
            log.info("Order confirmation email skipped because recipient is not deliverable: {}", customerEmail);
            return;
        }

        if (!mailEnabled) {
            log.info("Order confirmation email skipped because mail is disabled for {}", customerEmail);
            return;
        }

        String subject = "Order Confirmed - " + order.getOrderNumber();
        String body = buildOrderConfirmationEmailBody(order);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(customerEmail);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("Order confirmation email sent to {}", customerEmail);
        } catch (Exception ex) {
            log.error("Failed to send order confirmation email to {}: {}", customerEmail, ex.getMessage(), ex);
        }
    }

    private String buildOrderConfirmationEmailBody(Order order) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Dear Customer,\n\n");
        sb.append("Thank you for your order! Your order has been confirmed.\n\n");
        
        sb.append("========================================\n");
        sb.append("ORDER DETAILS\n");
        sb.append("========================================\n");
        sb.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        sb.append("Order Date: ").append(formatDateTime(order.getPlacedAt())).append("\n");
        sb.append("Status: ").append(OrderStatusFormatter.toDisplayName(order.getStatus())).append("\n");
        if (order.getDeliverySlot() != null) {
            sb.append("Delivery Slot: ").append(order.getDeliverySlot()).append("\n");
        }
        sb.append("\n");

        sb.append("========================================\n");
        sb.append("DELIVERY ADDRESS\n");
        sb.append("========================================\n");
        if (order.getAddress() != null) {
            sb.append(order.getAddress().getFullName()).append("\n");
            sb.append(order.getAddress().getAddressLine1()).append("\n");
            if (order.getAddress().getAddressLine2() != null) {
                sb.append(order.getAddress().getAddressLine2()).append("\n");
            }
            sb.append(order.getAddress().getCity()).append(", ").append(order.getAddress().getPincode()).append("\n");
            sb.append("Phone: ").append(order.getAddress().getPhone()).append("\n");
        }
        sb.append("\n");

        sb.append("========================================\n");
        sb.append("ORDER ITEMS\n");
        sb.append("========================================\n");
        
        for (OrderItem item : order.getItems()) {
            sb.append("\n");
            sb.append("Medicine: ").append(item.getMedicineName()).append("\n");
            sb.append("Quantity: ").append(item.getQuantity()).append("\n");
            sb.append("Unit Price: Rs.").append(formatPrice(item.getUnitPrice())).append("\n");
            sb.append("Total: Rs.").append(formatPrice(item.getTotalPrice())).append("\n");
            sb.append("----------------------------------------\n");
        }
        sb.append("\n");

        sb.append("========================================\n");
        sb.append("PAYMENT SUMMARY\n");
        sb.append("========================================\n");
        sb.append("Subtotal: Rs.").append(formatPrice(order.getSubtotal())).append("\n");
        sb.append("Delivery Charge: Rs.").append(formatPrice(order.getDeliveryCharge())).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("TOTAL AMOUNT: Rs.").append(formatPrice(order.getTotalAmount())).append("\n");
        sb.append("========================================\n");

        if (order.getPayment() != null) {
            sb.append("\n");
            sb.append("Payment Method: ").append(order.getPayment().getMethod()).append("\n");
            sb.append("Transaction ID: ").append(order.getPayment().getTransactionId()).append("\n");
            sb.append("Payment Status: ").append(OrderStatusFormatter.toDisplayName(order.getPayment().getStatus())).append("\n");
        }

        sb.append("\n");
        sb.append("Your order will be processed shortly. You will receive updates on the order status.\n\n");
        sb.append("Thank you for choosing Pharmacy App!\n\n");
        sb.append("Regards,\n");
        sb.append("Pharmacy App Team\n");

        return sb.toString();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.format(DATE_FORMATTER);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0.00";
        }
        return String.format("%.2f", price);
    }

    private boolean isDeliverableRecipient(String recipient) {
        if (recipient == null || recipient.isBlank()) {
            return false;
        }

        String normalized = recipient.trim().toLowerCase();
        if (!normalized.contains("@")) {
            return false;
        }

        return !normalized.endsWith("@customer.local")
                && !normalized.endsWith(".local")
                && !normalized.equals("unknown@customer.local");
    }
}
