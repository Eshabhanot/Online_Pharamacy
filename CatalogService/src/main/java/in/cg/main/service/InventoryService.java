package in.cg.main.service;

import java.util.List;

import in.cg.main.dto.InventoryAddRequest;
import in.cg.main.dto.InventoryResponse;

public interface InventoryService {

   
    InventoryResponse addBatch(InventoryAddRequest request);
    void reduceStock(Long medicineId, int quantity);
    List<InventoryResponse> getExpiringBatches(int withinDays);
    List<InventoryResponse> getLowStockBatches();
}