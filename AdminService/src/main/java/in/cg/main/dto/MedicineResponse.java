package in.cg.main.dto;


import java.math.BigDecimal;
import java.time.LocalDate;

import in.cg.main.entities.Medicine;

public class MedicineResponse {

    private Long id;
    private String name;
    private String brand;
    private String description;
    private String dosage;
    private BigDecimal price;
    private int stock;
    private boolean requiresPrescription;
    private boolean isActive;
    private Long categoryId;
    private String categoryName;
    private LocalDate expiryDate;

    // Static factory — maps entity to DTO cleanly
    public static MedicineResponse from(Medicine m) {
        MedicineResponse dto = new MedicineResponse();
        dto.setId(m.getId());
        dto.setName(m.getName());
        dto.setBrand(m.getBrand());
        dto.setDescription(m.getDescription());
        dto.setDosage(m.getDosage());
        dto.setPrice(m.getPrice());
        dto.setStock(m.getStock());
        dto.setRequiresPrescription(m.isRequiresPrescription());
        dto.setActive(m.isActive());
        dto.setCategoryId(m.getCategory() != null ? m.getCategory().getId() : null);
        dto.setCategoryName(m.getCategory() != null ? m.getCategory().getName() : null);
        dto.setExpiryDate(m.getExpiryDate());
        return dto;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getDescription() { return description; }
    public String getDosage() { return dosage; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isRequiresPrescription() { return requiresPrescription; }
    public boolean isActive() { return isActive; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public LocalDate getExpiryDate() { return expiryDate; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setDescription(String description) { this.description = description; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setRequiresPrescription(boolean v) { this.requiresPrescription = v; }
    public void setActive(boolean active) { isActive = active; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}
