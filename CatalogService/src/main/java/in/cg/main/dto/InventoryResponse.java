package in.cg.main.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import in.cg.main.entities.Inventory;

public class InventoryResponse {

    private Long id;
    private Long medicineId;
    private String medicineName;
    private int quantity;
    private String batchNumber;
    private LocalDate expiryDate;
    private String supplierName;
    private String status;
    private long daysUntilExpiry;

    // ✅ Static converter
    public static InventoryResponse from(Inventory inv) {
        InventoryResponse dto = new InventoryResponse();

        dto.setId(inv.getId());
        dto.setMedicineId(inv.getMedicine().getId());
        dto.setMedicineName(inv.getMedicine().getName());
        dto.setQuantity(inv.getQuantity());
        dto.setBatchNumber(inv.getBatchNumber());
        dto.setExpiryDate(inv.getExpiryDate());
        dto.setSupplierName(inv.getSupplierName());
        dto.setStatus(inv.getStatus().name());
        dto.setDaysUntilExpiry(calculateDays(inv.getExpiryDate()));

        return dto;
    }

    // ✅ Safe expiry calculation
    private static long calculateDays(LocalDate expiryDate) {
        if (expiryDate == null) return 0;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        return Math.max(days, 0);
    }

    // ✅ Getters and Setters

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

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDaysUntilExpiry() {
        return daysUntilExpiry;
    }

    public void setDaysUntilExpiry(long daysUntilExpiry) {
        this.daysUntilExpiry = daysUntilExpiry;
    }
}