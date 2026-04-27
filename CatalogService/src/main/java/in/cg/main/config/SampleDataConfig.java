package in.cg.main.config;

import in.cg.main.entities.Category;
import in.cg.main.entities.Inventory;
import in.cg.main.entities.Medicine;
import in.cg.main.entities.Prescription;
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
    CommandLineRunner seedCatalogSampleData(CategoryRepository categoryRepository,
                                            MedicineRepository medicineRepository,
                                            InventoryRepository inventoryRepository,
                                            PrescriptionRepository prescriptionRepository) {
        return args -> {
            if (categoryRepository.count() == 0 && medicineRepository.count() == 0) {
                Category otc = new Category();
                otc.setName("OTC");
                otc.setDescription("Over the counter medicines");
                otc = categoryRepository.save(otc);

                Category antibiotics = new Category();
                antibiotics.setName("Antibiotics");
                antibiotics.setDescription("Prescription medicines");
                antibiotics = categoryRepository.save(antibiotics);

                Medicine paracetamol = buildMedicine("Paracetamol 650", "Cipla", false, 90.00, 120, otc);
                Medicine amoxicillin = buildMedicine("Amoxicillin 500", "Mankind", true, 145.00, 60, antibiotics);
                paracetamol = medicineRepository.save(paracetamol);
                amoxicillin = medicineRepository.save(amoxicillin);

                inventoryRepository.save(buildInventory(paracetamol, "BATCH-PARA-001", 120, new BigDecimal("55.00")));
                inventoryRepository.save(buildInventory(amoxicillin, "BATCH-AMOX-001", 60, new BigDecimal("98.00")));
            }

            if (prescriptionRepository.count() == 0) {
                Prescription prescription = new Prescription();
                prescription.setCustomerId(1L);
                prescription.setFileName("demo-prescription.pdf");
                prescription.setFilePath("sample/demo-prescription.pdf");
                prescription.setFileType("application/pdf");
                prescription.setStatus(Prescription.PrescriptionStatus.APPROVED);
                prescription.setUploadedAt(LocalDateTime.now().minusDays(1));
                prescription.setReviewedAt(LocalDateTime.now().minusHours(20));
                prescriptionRepository.save(prescription);
            }
        };
    }

    private Medicine buildMedicine(String name, String brand, boolean requiresPrescription,
                                   double price, int stock, Category category) {
        Medicine medicine = new Medicine();
        medicine.setName(name);
        medicine.setBrand(brand);
        medicine.setDescription(name + " sample medicine");
        medicine.setDosage("1 tablet");
        medicine.setPrice(price);
        medicine.setStock(stock);
        medicine.setRequiresPrescription(requiresPrescription);
        medicine.setActive(true);
        medicine.setExpiryDate(LocalDate.now().plusMonths(10));
        medicine.setCategory(category);
        return medicine;
    }

    private Inventory buildInventory(Medicine medicine, String batchNumber, int quantity, BigDecimal costPrice) {
        Inventory inventory = new Inventory();
        inventory.setMedicine(medicine);
        inventory.setQuantity(quantity);
        inventory.setBatchNumber(batchNumber);
        inventory.setManufactureDate(LocalDate.now().minusMonths(2));
        inventory.setExpiryDate(LocalDate.now().plusMonths(10));
        inventory.setSupplierName("Demo Supplier");
        inventory.setCostPrice(costPrice);
        inventory.setStatus(Inventory.InventoryStatus.ACTIVE);
        return inventory;
    }
}
