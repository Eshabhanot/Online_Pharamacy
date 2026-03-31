package in.cg.main.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.cg.main.entities.Inventory;
import in.cg.main.entities.Inventory.InventoryStatus;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {

	// ✅ All batches for one medicine (paginated)
    Page<Inventory> findByMedicineIdOrderByExpiryDateAsc(Long medicineId, Pageable pageable);

    // ✅ Active batches
    Page<Inventory> findByMedicineIdAndStatus(Long medicineId, InventoryStatus status, Pageable pageable);

    // ✅ Expiry alerts
    Page<Inventory> findByExpiryDateBeforeAndStatusNot(LocalDate date, InventoryStatus status, Pageable pageable);

    // ✅ Low stock alert
    Page<Inventory> findByQuantityLessThanAndStatus(int threshold, InventoryStatus status, Pageable pageable);

    // ✅ Unique batch check
    boolean existsByBatchNumber(String batchNumber);
	
}
