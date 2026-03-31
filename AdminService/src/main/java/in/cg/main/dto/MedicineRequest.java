package in.cg.main.dto;


import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MedicineRequest {

    @NotBlank(message = "Medicine name is required")
    private String name;

    private String brand;
    private String description;
    private String dosage;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.1", message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    private boolean requiresPrescription;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private LocalDate expiryDate;

    // Getters
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getDescription() { return description; }
    public String getDosage() { return dosage; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isRequiresPrescription() { return requiresPrescription; }
    public Long getCategoryId() { return categoryId; }
    public LocalDate getExpiryDate() { return expiryDate; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setDescription(String description) { this.description = description; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setRequiresPrescription(boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}