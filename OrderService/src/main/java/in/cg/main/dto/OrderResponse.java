package in.cg.main.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private String orderNumber;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal deliveryCharge;
    private BigDecimal totalAmount;
    private String deliverySlot;
    private LocalDateTime placedAt;
    private LocalDateTime deliveredAt;
    private List<OrderItemDto> items;
    private AddressDto deliveryAddress;
    private PaymentDto payment;

    // 🔹 Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(BigDecimal deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDeliverySlot() {
        return deliverySlot;
    }

    public void setDeliverySlot(String deliverySlot) {
        this.deliverySlot = deliverySlot;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(LocalDateTime placedAt) {
        this.placedAt = placedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }

    public AddressDto getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(AddressDto deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public PaymentDto getPayment() {
        return payment;
    }

    public void setPayment(PaymentDto payment) {
        this.payment = payment;
    }

    // 🔥 Builder

    public static class Builder {
        private OrderResponse obj = new OrderResponse();

        public Builder id(Long id) { obj.setId(id); return this; }
        public Builder orderNumber(String val) { obj.setOrderNumber(val); return this; }
        public Builder status(String val) { obj.setStatus(val); return this; }
        public Builder subtotal(BigDecimal val) { obj.setSubtotal(val); return this; }
        public Builder deliveryCharge(BigDecimal val) { obj.setDeliveryCharge(val); return this; }
        public Builder totalAmount(BigDecimal val) { obj.setTotalAmount(val); return this; }
        public Builder deliverySlot(String val) { obj.setDeliverySlot(val); return this; }
        public Builder placedAt(LocalDateTime val) { obj.setPlacedAt(val); return this; }
        public Builder deliveredAt(LocalDateTime val) { obj.setDeliveredAt(val); return this; }
        public Builder items(List<OrderItemDto> val) { obj.setItems(val); return this; }
        public Builder deliveryAddress(AddressDto val) { obj.setDeliveryAddress(val); return this; }
        public Builder payment(PaymentDto val) { obj.setPayment(val); return this; }

        public OrderResponse build() { return obj; }
    }

    // ================= INNER DTOs =================

    public static class OrderItemDto {
        private String medicineName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        // Getters & Setters
        public String getMedicineName() { return medicineName; }
        public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    }

    public static class AddressDto {
        private String fullName;
        private String addressLine1;
        private String city;
        private String pincode;

        // Getters & Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getAddressLine1() { return addressLine1; }
        public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getPincode() { return pincode; }
        public void setPincode(String pincode) { this.pincode = pincode; }
    }

    public static class PaymentDto {
        private String method;
        private String status;
        private String transactionId;

        // Getters & Setters
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    }
}
