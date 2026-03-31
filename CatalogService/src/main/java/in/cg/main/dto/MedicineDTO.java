package in.cg.main.dto;

import java.time.LocalDate;

import in.cg.main.entities.Medicine;

public class MedicineDTO {
	    private Long id;
	    private String name;
	    private String brand;
	    private String description;
	    private String dosage;
	    private double price;
	    private int stock;
	    private boolean requiresPrescription;
	    private String categoryName;
	    private LocalDate expiryDate;
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getBrand() {
			return brand;
		}
		public void setBrand(String brand) {
			this.brand = brand;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getDosage() {
			return dosage;
		}
		public void setDosage(String dosage) {
			this.dosage = dosage;
		}
		public double getPrice() {
			return price;
		}
		public void setPrice(double price) {
			this.price = price;
		}
		public int getStock() {
			return stock;
		}
		public void setStock(int stock) {
			this.stock = stock;
		}
		public boolean isRequiresPrescription() {
			return requiresPrescription;
		}
		public void setRequiresPrescription(boolean requiresPrescription) {
			this.requiresPrescription = requiresPrescription;
		}
		public String getCategoryName() {
			return categoryName;
		}
		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}
		public LocalDate getExpiryDate() {
			return expiryDate;
		}
		public void setExpiryDate(LocalDate expiryDate) {
			this.expiryDate = expiryDate;
		}

	    
	    
}
