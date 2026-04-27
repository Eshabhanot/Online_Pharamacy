package in.cg.main.service;

import in.cg.main.entities.Category;
import in.cg.main.entities.Inventory;
import in.cg.main.entities.Medicine;
import in.cg.main.enums.InventoryStatus;
import in.cg.main.enums.PrescriptionStatus;
import in.cg.main.repository.InventoryRepository;
import in.cg.main.repository.MedicineRepository;
import in.cg.main.repository.PrescriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock private MedicineRepository medicineRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private PrescriptionRepository prescriptionRepository;
    @Mock private RestTemplate restTemplate;
    @Mock private DiscoveryClient discoveryClient;

    private AdminReportService adminReportService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        adminReportService = new AdminReportService(
                medicineRepository,
                inventoryRepository,
                prescriptionRepository,
                restTemplate,
                discoveryClient,
                "http://order-service:8083");
        lenient().when(discoveryClient.getInstances(anyString())).thenReturn(Collections.emptyList());
    }

    private Medicine createMedicine() {
        Medicine m = new Medicine();
        m.setId(1L);
        m.setName("Paracetamol");
        m.setBrand("Cipla");
        m.setPrice(BigDecimal.valueOf(25.00));
        m.setStock(100);
        m.setActive(true);
        m.setRequiresPrescription(false);
        Category cat = new Category();
        cat.setName("OTC");
        m.setCategory(cat);
        return m;
    }

    @Test
    void exportMedicinesCsv_success() throws Exception {
        when(medicineRepository.findAll()).thenReturn(List.of(createMedicine()));

        byte[] csv = adminReportService.exportMedicinesCsv();

        assertNotNull(csv);
        assertTrue(csv.length > 0);
        String content = new String(csv);
        assertTrue(content.contains("Paracetamol"));
    }

    @Test
    void exportMedicinesCsv_empty() throws Exception {
        when(medicineRepository.findAll()).thenReturn(Collections.emptyList());

        byte[] csv = adminReportService.exportMedicinesCsv();

        assertNotNull(csv);
        // Should still have CSV header
        assertTrue(csv.length > 0);
    }

    @Test
    void exportMedicinesCsv_nullCategoryAndPrescriptionRequired() throws Exception {
        Medicine m = createMedicine();
        m.setCategory(null);
        m.setRequiresPrescription(true);
        m.setActive(false);
        when(medicineRepository.findAll()).thenReturn(List.of(m));

        byte[] csv = adminReportService.exportMedicinesCsv();

        assertNotNull(csv);
        String content = new String(csv);
        assertTrue(content.contains("Yes"));
        assertTrue(content.contains("No"));
        assertTrue(content.contains("Paracetamol"));
    }

    @Test
    void exportLowStockCsv_success() throws Exception {
        Medicine m = createMedicine();
        m.setStock(5);
        when(medicineRepository.findByStockLessThan(10)).thenReturn(List.of(m));

        byte[] csv = adminReportService.exportLowStockCsv();

        assertNotNull(csv);
        String content = new String(csv);
        assertTrue(content.contains("Paracetamol"));
    }

    @Test
    void exportLowStockCsv_empty() throws Exception {
        when(medicineRepository.findByStockLessThan(10)).thenReturn(Collections.emptyList());

        byte[] csv = adminReportService.exportLowStockCsv();

        assertNotNull(csv);
        assertTrue(csv.length > 0);
    }

    @Test
    void getInventoryReport_returnsCounts() {
        when(medicineRepository.countActiveMedicines()).thenReturn(4L);
        when(inventoryRepository.findByQuantityLessThanAndStatus(10, InventoryStatus.ACTIVE))
                .thenReturn(List.of(new Inventory()));
        when(inventoryRepository.findByExpiryDateBeforeAndStatusNot(any(), eq(InventoryStatus.EXPIRED)))
                .thenReturn(List.of(new Inventory(), new Inventory()));

        Map<String, Object> report = adminReportService.getInventoryReport();

        assertEquals(4L, report.get("activeMedicines"));
        assertEquals(1, report.get("lowStockBatches"));
        assertEquals(2, report.get("expiringBatchesIn30Days"));
    }

    @Test
    void getSalesReport_returnsOrderServiceStats() {
        AdminDashboardService.DashboardOrderStats stats = new AdminDashboardService.DashboardOrderStats();
        stats.setTotalOrdersToday(6);
        stats.setPendingOrders(2);
        stats.setDeliveredOrdersToday(3);
        stats.setRevenueToday(BigDecimal.valueOf(900));

        when(restTemplate.getForObject(contains("/api/internal/orders/stats"),
                eq(AdminDashboardService.DashboardOrderStats.class)))
                .thenReturn(stats);

        Map<String, Object> report = adminReportService.getSalesReport();

        assertEquals(6L, report.get("totalOrdersToday"));
        assertEquals(2L, report.get("pendingOrders"));
        assertEquals(3L, report.get("deliveredOrdersToday"));
        assertEquals(BigDecimal.valueOf(900), report.get("revenueToday"));
    }

    @Test
    void getPrescriptionVolumeReport_returnsCountsByStatus() {
        when(prescriptionRepository.countByStatus(PrescriptionStatus.PENDING)).thenReturn(3L);
        when(prescriptionRepository.countByStatus(PrescriptionStatus.APPROVED)).thenReturn(2L);
        when(prescriptionRepository.countByStatus(PrescriptionStatus.REJECTED)).thenReturn(1L);

        Map<String, Object> report = adminReportService.getPrescriptionVolumeReport();

        assertEquals(3L, report.get("pending"));
        assertEquals(2L, report.get("approved"));
        assertEquals(1L, report.get("rejected"));
    }

    @Test
    void getExpiryReport_returnsSevenAndThirtyDayCounts() {
        when(inventoryRepository.findByExpiryDateBeforeAndStatusNot(any(), eq(InventoryStatus.EXPIRED)))
                .thenReturn(List.of(new Inventory()))
                .thenReturn(List.of(new Inventory(), new Inventory()));

        Map<String, Object> report = adminReportService.getExpiryReport();

        assertEquals(1, report.get("expiringIn7Days"));
        assertEquals(2, report.get("expiringIn30Days"));
    }
}
