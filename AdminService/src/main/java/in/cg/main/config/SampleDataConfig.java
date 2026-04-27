package in.cg.main.config;

import in.cg.main.entities.Category;
import in.cg.main.entities.Inventory;
import in.cg.main.entities.Medicine;
import in.cg.main.entities.Prescription;
import in.cg.main.enums.InventoryStatus;
import in.cg.main.enums.PrescriptionStatus;
import in.cg.main.repository.CategoryRepository;
import in.cg.main.repository.InventoryRepository;
import in.cg.main.repository.MedicineRepository;
import in.cg.main.repository.PrescriptionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class SampleDataConfig {

    @Bean
    CommandLineRunner seedAdminSampleData(CategoryRepository categoryRepository,
                                          MedicineRepository medicineRepository,
                                          InventoryRepository inventoryRepository,
                                          PrescriptionRepository prescriptionRepository) {
        return args -> {
            if (categoryRepository.count() == 0 && medicineRepository.count() == 0) {
                Category category = new Category();
                category.setName("Operations");
                category.setDescription("Dashboard sample category");
                category = categoryRepository.save(category);

                Medicine medicine = new Medicine();
                medicine.setName("Paracetamol 650");
                medicine.setBrand("Cipla");
                medicine.setDescription("Dashboard sample medicine");
                medicine.setDosage("1 tablet");
                medicine.setPrice(new BigDecimal("90.00"));
                medicine.setStock(8);
                medicine.setRequiresPrescription(false);
                medicine.setActive(true);
                medicine.setExpiryDate(LocalDate.now().plusMonths(6));
                medicine.setCategory(category);
                medicine = medicineRepository.save(medicine);

                Inventory inventory = new Inventory();
                inventory.setMedicine(medicine);
                inventory.setQuantity(8);
                inventory.setBatchNumber("ADMIN-BATCH-001");
                inventory.setManufactureDate(LocalDate.now().minusMonths(1));
                inventory.setExpiryDate(LocalDate.now().plusMonths(6));
                inventory.setSupplierName("Demo Supplier");
                inventory.setCostPrice(new BigDecimal("50.00"));
                inventory.setStatus(InventoryStatus.ACTIVE);
                inventoryRepository.save(inventory);
            }

            if (prescriptionRepository.count() == 0) {
                Prescription prescription = new Prescription();
                prescription.setCustomerId(1L);
                prescription.setOrderId(1L);
                prescription.setFileName("pending-review.pdf");
                prescription.setFilePath("sample/pending-review.pdf");
                prescription.setFileType("application/pdf");
                prescription.setStatus(PrescriptionStatus.PENDING);
                prescription.setUploadedAt(LocalDateTime.now().minusHours(3));
                prescriptionRepository.save(prescription);
            }
        };
    }
}
