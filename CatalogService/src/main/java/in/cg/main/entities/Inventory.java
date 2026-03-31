package in.cg.main.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="inventory")
public class Inventory {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "medicine_id", nullable = false)
	    private Medicine medicine;

	    @Column(nullable = false)
	    private int quantity;

	    @Column(name = "batch_number", nullable = false, unique = true)
	    private String batchNumber;

	    @Column(name = "manufacture_date")
	    private LocalDate manufactureDate;

	    @Column(name = "expiry_date", nullable = false)
	    private LocalDate expiryDate;

	    @Column(name = "supplier_name")
	    private String supplierName;

	    @Column(name = "cost_price", precision = 10, scale = 2)
	    private BigDecimal costPrice;

	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false)
	    private InventoryStatus status = InventoryStatus.ACTIVE;

	    @Column(name = "created_at", updatable = false)
	    @CreationTimestamp
	    private LocalDateTime createdAt;

	    @Column(name = "updated_at")
	    @UpdateTimestamp
	    private LocalDateTime updatedAt;

	    public enum InventoryStatus {
	        ACTIVE, LOW_STOCK, EXPIRED, DISCONTINUED
	    }

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Medicine getMedicine() {
			return medicine;
		}

		public void setMedicine(Medicine medicine) {
			this.medicine = medicine;
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

		public LocalDate getManufactureDate() {
			return manufactureDate;
		}

		public void setManufactureDate(LocalDate manufactureDate) {
			this.manufactureDate = manufactureDate;
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

		public BigDecimal getCostPrice() {
			return costPrice;
		}

		public void setCostPrice(BigDecimal costPrice) {
			this.costPrice = costPrice;
		}

		public InventoryStatus getStatus() {
			return status;
		}

		public void setStatus(InventoryStatus status) {
			this.status = status;
		}

		public LocalDateTime getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
		}

		public LocalDateTime getUpdatedAt() {
			return updatedAt;
		}

		public void setUpdatedAt(LocalDateTime updatedAt) {
			this.updatedAt = updatedAt;
		}
	    
	    
}
