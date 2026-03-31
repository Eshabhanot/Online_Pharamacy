package in.cg.main.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class OrderNotification implements Serializable {

    private Long orderId;
    private String customerEmail;
    private String status;
    private String message;
    private LocalDateTime timestamp;

    public OrderNotification() {
    }

    public OrderNotification(Long orderId, String customerEmail, String status, String message) {
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "OrderNotification{" +
                "orderId=" + orderId +
                ", customerEmail='" + customerEmail + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
