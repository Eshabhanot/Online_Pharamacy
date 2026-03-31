package in.cg.main.service;

import java.util.List;

import in.cg.main.dto.InventoryAddRequest;
import in.cg.main.dto.InventoryResponse;

public interface InventoryService {

    // ✅ Add new inventory batch (Admin)
    InventoryResponse addBatch(InventoryAddRequest request);

    // ✅ Reduce stock (used by Order Service)
    void reduceStock(Long medicineId, int quantity);

    // ✅ Get batches expiring within given days
    List<InventoryResponse> getExpiringBatches(int withinDays);

    // ✅ Get low stock batches
    List<InventoryResponse> getLowStockBatches();
}