package in.cg.main.service;

import in.cg.main.dto.InventoryAddRequest;
import in.cg.main.dto.InventoryResponse;
import in.cg.main.dto.MedicineDTO;
import in.cg.main.entities.Inventory;
import in.cg.main.entities.Medicine;
import in.cg.main.repository.InventoryRepository;
import in.cg.main.repository.MedicineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImpTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private MedicineService medicineService;

    @InjectMocks
    private InventoryServiceImp inventoryService;

    private Medicine medicine;
    private MedicineDTO medicineDTO;

    @BeforeEach
    void setup() {
        medicine = new Medicine();
        medicine.setId(1L);
        medicine.setName("Paracetamol");

        medicineDTO = new MedicineDTO();
        medicineDTO.setId(1L);
        medicineDTO.setName("Paracetamol");
    }

    @Test
    void addBatch_success() {
        InventoryAddRequest req = new InventoryAddRequest();
        req.setMedicineId(1L);
        req.setQuantity(20);
        req.setBatchNumber("B1");
        req.setExpiryDate(LocalDate.now().plusDays(20));
        req.setManufactureDate(LocalDate.now().minusDays(1));
        req.setSupplierName("ABC");
        req.setCostPrice(BigDecimal.TEN);

        Inventory saved = new Inventory();
        saved.setId(11L);
        saved.setMedicine(medicine);
        saved.setQuantity(20);
        saved.setBatchNumber("B1");
        saved.setExpiryDate(req.getExpiryDate());
        saved.setSupplierName("ABC");
        saved.setStatus(Inventory.InventoryStatus.ACTIVE);

        when(inventoryRepository.existsByBatchNumber("B1")).thenReturn(false);
        when(medicineService.getById(1L)).thenReturn(medicineDTO);
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(medicine);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(saved);
        when(inventoryRepository.findByMedicineIdAndStatus(eq(1L), eq(Inventory.InventoryStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(saved)));

        InventoryResponse response = inventoryService.addBatch(req);

        assertEquals(11L, response.getId());
        assertEquals("B1", response.getBatchNumber());
        verify(medicineRepository, atLeastOnce()).save(any(Medicine.class));
    }

    @Test
    void addBatch_duplicateBatch_throws() {
        InventoryAddRequest req = new InventoryAddRequest();
        req.setBatchNumber("B1");

        when(inventoryRepository.existsByBatchNumber("B1")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> inventoryService.addBatch(req));

        assertTrue(ex.getMessage().contains("Batch already exists"));
        verify(medicineService, never()).getById(anyLong());
    }

    @Test
    void addBatch_medicineNotFound_throws() {
        InventoryAddRequest req = new InventoryAddRequest();
        req.setBatchNumber("B1");
        req.setMedicineId(99L);

        when(inventoryRepository.existsByBatchNumber("B1")).thenReturn(false);
        when(medicineService.getById(99L)).thenThrow(new RuntimeException("Medicine Not Found!!"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> inventoryService.addBatch(req));

        assertEquals("Medicine not found", ex.getMessage());
    }

    @Test
    void reduceStock_marksDiscontinuedAndSyncsStock() {
        Inventory batch = new Inventory();
        batch.setMedicine(medicine);
        batch.setQuantity(5);
        batch.setExpiryDate(LocalDate.now().plusDays(2));
        batch.setStatus(Inventory.InventoryStatus.ACTIVE);

        when(inventoryRepository.findByMedicineIdAndStatus(eq(1L), eq(Inventory.InventoryStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>(List.of(batch))), new PageImpl<>(new ArrayList<>(List.of(batch))));
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));

        inventoryService.reduceStock(1L, 5);

        assertEquals(0, batch.getQuantity());
        assertEquals(Inventory.InventoryStatus.DISCONTINUED, batch.getStatus());
        verify(inventoryRepository, atLeastOnce()).save(batch);
        verify(medicineRepository).save(any(Medicine.class));
    }

    @Test
    void reduceStock_marksLowStock() {
        Inventory batch = new Inventory();
        batch.setMedicine(medicine);
        batch.setQuantity(12);
        batch.setExpiryDate(LocalDate.now().plusDays(2));
        batch.setStatus(Inventory.InventoryStatus.ACTIVE);

        when(inventoryRepository.findByMedicineIdAndStatus(eq(1L), eq(Inventory.InventoryStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>(List.of(batch))), new PageImpl<>(new ArrayList<>(List.of(batch))));
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));

        inventoryService.reduceStock(1L, 5);

        assertEquals(7, batch.getQuantity());
        assertEquals(Inventory.InventoryStatus.LOW_STOCK, batch.getStatus());
    }

    @Test
    void reduceStock_insufficient_throws() {
        Inventory batch = new Inventory();
        batch.setMedicine(medicine);
        batch.setQuantity(2);
        batch.setExpiryDate(LocalDate.now().plusDays(2));
        batch.setStatus(Inventory.InventoryStatus.ACTIVE);

        when(inventoryRepository.findByMedicineIdAndStatus(eq(1L), eq(Inventory.InventoryStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>(List.of(batch))));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> inventoryService.reduceStock(1L, 5));

        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void getExpiringBatches_returnsMappedList() {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setMedicine(medicine);
        inv.setQuantity(4);
        inv.setBatchNumber("B-exp");
        inv.setExpiryDate(LocalDate.now().plusDays(3));
        inv.setStatus(Inventory.InventoryStatus.ACTIVE);

        when(inventoryRepository.findByExpiryDateBeforeAndStatusNot(any(LocalDate.class), eq(Inventory.InventoryStatus.EXPIRED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(inv)));

        List<InventoryResponse> result = inventoryService.getExpiringBatches(7);

        assertEquals(1, result.size());
        assertEquals("B-exp", result.get(0).getBatchNumber());
    }

    @Test
    void getLowStockBatches_returnsMappedList() {
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setMedicine(medicine);
        inv.setQuantity(3);
        inv.setBatchNumber("B-low");
        inv.setExpiryDate(LocalDate.now().plusDays(10));
        inv.setStatus(Inventory.InventoryStatus.ACTIVE);

        when(inventoryRepository.findByQuantityLessThanAndStatus(eq(10), eq(Inventory.InventoryStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(inv)));

        List<InventoryResponse> result = inventoryService.getLowStockBatches();

        assertEquals(1, result.size());
        assertEquals("B-low", result.get(0).getBatchNumber());
    }

    @Test
    void reduceStock_syncMedicineMissing_throws() {
        Inventory batch = new Inventory();
        batch.setMedicine(medicine);
        batch.setQuantity(8);
        batch.setExpiryDate(LocalDate.now().plusDays(5));
        batch.setStatus(Inventory.InventoryStatus.ACTIVE);

        when(medicineService.getById(1L)).thenThrow(new RuntimeException("Medicine Not Found!!"));
        when(inventoryRepository.findByMedicineIdAndStatus(eq(1L), eq(Inventory.InventoryStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>(List.of(batch))), new PageImpl<>(new ArrayList<>(List.of(batch))));
        when(medicineRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> inventoryService.reduceStock(1L, 1));

        assertEquals("Medicine not found", ex.getMessage());
    }

    @Test
    void reduceStock_breaksAfterRequirementMet_andLeavesNextBatchUntouched() {
        Inventory first = new Inventory();
        first.setMedicine(medicine);
        first.setQuantity(50);
        first.setExpiryDate(LocalDate.now().plusDays(1));
        first.setStatus(Inventory.InventoryStatus.ACTIVE);

        Inventory second = new Inventory();
        second.setMedicine(medicine);
        second.setQuantity(30);
        second.setExpiryDate(LocalDate.now().plusDays(2));
        second.setStatus(Inventory.InventoryStatus.ACTIVE);

        when(inventoryRepository.findByMedicineIdAndStatus(eq(1L), eq(Inventory.InventoryStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>(List.of(first, second))), new PageImpl<>(new ArrayList<>(List.of(first, second))));
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));

        inventoryService.reduceStock(1L, 5);

        assertEquals(45, first.getQuantity());
        assertEquals(30, second.getQuantity());
        assertEquals(Inventory.InventoryStatus.ACTIVE, first.getStatus());
        assertEquals(Inventory.InventoryStatus.ACTIVE, second.getStatus());
    }
}
