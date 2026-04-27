package in.cg.main.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.cg.main.dto.InventoryAddRequest;
import in.cg.main.dto.MedicineDTO;
import in.cg.main.dto.InventoryResponse;
import in.cg.main.entities.Inventory;
import in.cg.main.entities.Medicine;
import in.cg.main.repository.InventoryRepository;
import in.cg.main.repository.MedicineRepository;

@Service
@Transactional
public class InventoryServiceImp implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private MedicineService medicineService;

    private static final int LOW_STOCK_THRESHOLD = 10;

   
    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "medicines", allEntries = true)
    public InventoryResponse addBatch(InventoryAddRequest req) {

        // 1. Check batch uniqueness
        if (inventoryRepository.existsByBatchNumber(req.getBatchNumber())) {
            throw new RuntimeException("Batch already exists: " + req.getBatchNumber());
        }

        // 2. Validate medicine via Feign-backed medicine service
      
        Medicine medicine = ensureLocalMedicine(req.getMedicineId());

        // 3. Create inventory
        Inventory batch = new Inventory();
        batch.setMedicine(medicine);
        batch.setQuantity(req.getQuantity());
        batch.setBatchNumber(req.getBatchNumber());
        batch.setManufactureDate(req.getManufactureDate());
        batch.setExpiryDate(req.getExpiryDate());
        batch.setSupplierName(req.getSupplierName());
        batch.setCostPrice(req.getCostPrice());
        batch.setStatus(Inventory.InventoryStatus.ACTIVE);

        Inventory saved = inventoryRepository.save(batch);

        // 4. Sync stock
        syncMedicineStock(req.getMedicineId());

        return InventoryResponse.from(saved);
    }

   
    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "medicines", allEntries = true)
    public void reduceStock(Long medicineId, int quantityNeeded) {

        // Fetch ALL active batches using pagination (large size to simulate full fetch)
        Page<Inventory> page = inventoryRepository.findByMedicineIdAndStatus(
                medicineId,
                Inventory.InventoryStatus.ACTIVE,
                PageRequest.of(0, Integer.MAX_VALUE)
        );

        List<Inventory> batches = new ArrayList<>(page.getContent());

        // FIFO → earliest expiry first
        batches.sort(Comparator.comparing(Inventory::getExpiryDate));

        int remaining = quantityNeeded;

        for (Inventory batch : batches) {

            if (remaining <= 0) break;

            if (batch.getQuantity() >= remaining) {
                batch.setQuantity(batch.getQuantity() - remaining);
                remaining = 0;
            } else {
                remaining -= batch.getQuantity();
                batch.setQuantity(0);
            }

            // Update status
            if (batch.getQuantity() == 0) {
                batch.setStatus(Inventory.InventoryStatus.DISCONTINUED);
            } else if (batch.getQuantity() < LOW_STOCK_THRESHOLD) {
                batch.setStatus(Inventory.InventoryStatus.LOW_STOCK);
            }

            inventoryRepository.save(batch);
        }

        if (remaining > 0) {
            throw new RuntimeException("Insufficient stock for medicine ID: " + medicineId);
        }

        syncMedicineStock(medicineId);
    }


    @Override
    public List<InventoryResponse> getExpiringBatches(int withinDays) {

        LocalDate cutoff = LocalDate.now().plusDays(withinDays);

        Page<Inventory> page = inventoryRepository
                .findByExpiryDateBeforeAndStatusNot(
                        cutoff,
                        Inventory.InventoryStatus.EXPIRED,
                        PageRequest.of(0, 50) // adjustable
                );

        return page.getContent()
                .stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }


    @Override
    public List<InventoryResponse> getLowStockBatches() {

        Page<Inventory> page = inventoryRepository
                .findByQuantityLessThanAndStatus(
                        LOW_STOCK_THRESHOLD,
                        Inventory.InventoryStatus.ACTIVE,
                        PageRequest.of(0, 50)
                );

        return page.getContent()
                .stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }

   
    private void syncMedicineStock(Long medicineId) {

        Page<Inventory> page = inventoryRepository
                .findByMedicineIdAndStatus(
                        medicineId,
                        Inventory.InventoryStatus.ACTIVE,
                        PageRequest.of(0, Integer.MAX_VALUE)
                );

        int total = page.getContent()
                .stream()
                .mapToInt(Inventory::getQuantity)
                .sum();

        Medicine medicine = medicineRepository.findById(medicineId)
                .orElseGet(() -> ensureLocalMedicine(medicineId));

        medicine.setStock(total);
        medicineRepository.save(medicine);
    }

    private Medicine ensureLocalMedicine(Long medicineId) {
        MedicineDTO externalMedicine;
        try {
            externalMedicine = medicineService.getById(medicineId);
        } catch (RuntimeException ex) {
            throw new RuntimeException("Medicine not found");
        }

        Medicine localMedicine = medicineRepository.findById(medicineId).orElseGet(Medicine::new);
        localMedicine.setId(medicineId);
        localMedicine.setName(externalMedicine.getName());
        localMedicine.setBrand(externalMedicine.getBrand());
        localMedicine.setDescription(externalMedicine.getDescription());
        localMedicine.setDosage(externalMedicine.getDosage());
        localMedicine.setPrice(externalMedicine.getPrice());
        localMedicine.setRequiresPrescription(externalMedicine.isRequiresPrescription());
        localMedicine.setExpiryDate(externalMedicine.getExpiryDate());
        localMedicine.setActive(true);

        return medicineRepository.save(localMedicine);
    }
}
