package in.cg.main.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private Long cartId;
    private List<CartItemDto> items;
    private BigDecimal subtotal;
    private int totalItems;

    // 🔹 Getters & Setters

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    // 🔥 Manual Builder
    public static class Builder {
        private Long cartId;
        private List<CartItemDto> items;
        private BigDecimal subtotal;
        private int totalItems;

        public Builder cartId(Long cartId) {
            this.cartId = cartId;
            return this;
        }

        public Builder items(List<CartItemDto> items) {
            this.items = items;
            return this;
        }

        public Builder subtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
            return this;
        }

        public Builder totalItems(int totalItems) {
            this.totalItems = totalItems;
            return this;
        }

        public CartResponse build() {
            CartResponse response = new CartResponse();
            response.setCartId(cartId);
            response.setItems(items);
            response.setSubtotal(subtotal);
            response.setTotalItems(totalItems);
            return response;
        }
    }

    // ================== INNER DTO ==================

    public static class CartItemDto {

        private Long id;
        private Long medicineId;
        private String medicineName;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
        private boolean requiresPrescription;
        private boolean prescriptionUploaded;

        // Getters & Setters

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getMedicineId() {
            return medicineId;
        }

        public void setMedicineId(Long medicineId) {
            this.medicineId = medicineId;
        }

        public String getMedicineName() {
            return medicineName;
        }

        public void setMedicineName(String medicineName) {
            this.medicineName = medicineName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getLineTotal() {
            return lineTotal;
        }

        public void setLineTotal(BigDecimal lineTotal) {
            this.lineTotal = lineTotal;
        }

        public boolean isRequiresPrescription() {
            return requiresPrescription;
        }

        public void setRequiresPrescription(boolean requiresPrescription) {
            this.requiresPrescription = requiresPrescription;
        }

        public boolean isPrescriptionUploaded() {
            return prescriptionUploaded;
        }

        public void setPrescriptionUploaded(boolean prescriptionUploaded) {
            this.prescriptionUploaded = prescriptionUploaded;
        }
    }
}