package in.cg.main.service;

import in.cg.main.dto.DashboardResponse;
import in.cg.main.dto.AdminOrderTrackingResponse;
import in.cg.main.entities.Inventory;
import in.cg.main.entities.Medicine;
import in.cg.main.enums.InventoryStatus;
import in.cg.main.enums.PrescriptionStatus;
import in.cg.main.repository.InventoryRepository;
import in.cg.main.repository.MedicineRepository;
import in.cg.main.repository.PrescriptionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {
    private static final String ORDER_SERVICE_BASE_URL = "http://order-service:8083";

    @Mock private MedicineRepository medicineRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private PrescriptionRepository prescriptionRepository;
    @Mock private RestTemplate restTemplate;
    
    private AdminDashboardService adminDashboardService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        adminDashboardService = new AdminDashboardService(
                medicineRepository,
                inventoryRepository,
                prescriptionRepository,
                restTemplate,
                ORDER_SERVICE_BASE_URL
        );
    }

    @Test
    void getDashboard_success() {

        when(prescriptionRepository.countByStatus(PrescriptionStatus.PENDING))
                .thenReturn(5L);

        Medicine medicine = new Medicine();
        medicine.setId(1L);
        Inventory lowStockInventory = new Inventory();
        lowStockInventory.setMedicine(medicine);
        when(inventoryRepository.findByQuantityLessThanAndStatus(10, InventoryStatus.ACTIVE))
                .thenReturn(List.of(lowStockInventory));

        when(inventoryRepository.findByExpiryDateBeforeAndStatusNot(any(), eq(InventoryStatus.EXPIRED)))
                .thenReturn(Collections.emptyList());

        when(medicineRepository.countActiveMedicines())
                .thenReturn(20L);

        // Mock Order Service response
        AdminDashboardService.DashboardOrderStats stats =
                new AdminDashboardService.DashboardOrderStats();
        stats.setTotalOrdersToday(3);
        stats.setPendingOrders(1);
        stats.setDeliveredOrdersToday(2);
        stats.setRevenueToday(BigDecimal.valueOf(1000));

        when(restTemplate.getForObject(anyString(),
                eq(AdminDashboardService.DashboardOrderStats.class)))
                .thenReturn(stats);
        when(restTemplate.getForObject(contains("/tracking"),
                eq(AdminOrderTrackingResponse[].class)))
                .thenReturn(new AdminOrderTrackingResponse[] { new AdminOrderTrackingResponse() });

        DashboardResponse result = adminDashboardService.getDashboard();

        assertNotNull(result);
        assertEquals(5, result.getPendingPrescriptions());
        assertEquals(1, result.getLowStockMedicines());
        assertEquals(0, result.getExpiringBatchesIn30Days());
        assertEquals(20, result.getTotalActiveMedicines());

        assertEquals(3, result.getTotalOrdersToday());
        assertEquals(1, result.getPendingOrders());
        assertEquals(2, result.getDeliveredOrdersToday());
        assertEquals(BigDecimal.valueOf(1000), result.getRevenueToday());
        assertEquals(1, result.getRecentOrderTracking().size());
    }

    @Test
    void getDashboard_orderServiceDown() {

        when(prescriptionRepository.countByStatus(PrescriptionStatus.PENDING))
                .thenReturn(0L);

        when(inventoryRepository.findByQuantityLessThanAndStatus(10, InventoryStatus.ACTIVE))
                .thenReturn(Collections.emptyList());

        when(inventoryRepository.findByExpiryDateBeforeAndStatusNot(any(), eq(InventoryStatus.EXPIRED)))
                .thenReturn(Collections.emptyList());

        when(medicineRepository.countActiveMedicines())
                .thenReturn(10L);

        when(restTemplate.getForObject(anyString(),
                eq(AdminDashboardService.DashboardOrderStats.class)))
                .thenThrow(new RestClientException("Service down"));

        DashboardResponse result = adminDashboardService.getDashboard();

        assertNotNull(result);
        assertEquals(0, result.getTotalOrdersToday());
        assertEquals(BigDecimal.ZERO, result.getRevenueToday());
        assertEquals(List.of(), result.getRecentOrderTracking());
    }

    @Test
    void getDashboard_orderStatsNull_keepsDefaultOrderValues() {
        when(prescriptionRepository.countByStatus(PrescriptionStatus.PENDING))
            .thenReturn(1L);
        when(inventoryRepository.findByQuantityLessThanAndStatus(10, InventoryStatus.ACTIVE))
            .thenReturn(Collections.emptyList());
        when(inventoryRepository.findByExpiryDateBeforeAndStatusNot(any(), eq(InventoryStatus.EXPIRED)))
            .thenReturn(Collections.emptyList());
        when(medicineRepository.countActiveMedicines())
            .thenReturn(8L);
        when(restTemplate.getForObject(anyString(), eq(AdminDashboardService.DashboardOrderStats.class)))
            .thenReturn(null);
        when(restTemplate.getForObject(contains("/tracking"), eq(AdminOrderTrackingResponse[].class)))
            .thenReturn(null);

        DashboardResponse result = adminDashboardService.getDashboard();

        assertNotNull(result);
        assertEquals(0, result.getTotalOrdersToday());
        assertEquals(0, result.getPendingOrders());
        assertEquals(0, result.getDeliveredOrdersToday());
        assertNull(result.getRevenueToday());
        assertEquals(1, result.getPendingPrescriptions());
        assertEquals(8, result.getTotalActiveMedicines());
        assertEquals(Collections.emptyList(), result.getRecentOrderTracking());
    }
}
