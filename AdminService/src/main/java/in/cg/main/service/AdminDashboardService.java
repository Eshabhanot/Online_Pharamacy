package in.cg.main.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.cg.main.dto.AdminOrderTrackingResponse;
import in.cg.main.dto.DashboardResponse;
import in.cg.main.entities.Inventory;
import in.cg.main.enums.InventoryStatus;
import in.cg.main.enums.PrescriptionStatus;
import in.cg.main.repository.InventoryRepository;
import in.cg.main.repository.MedicineRepository;
import in.cg.main.repository.PrescriptionRepository;

@Service
public class AdminDashboardService {
    private static final int LOW_STOCK_THRESHOLD = 10;
    private static final int TRACKING_LIMIT = 10;

    private final MedicineRepository medicineRepository;
    private final InventoryRepository inventoryRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final RestTemplate restTemplate;
    private final String orderServiceBaseUrl;

    public AdminDashboardService(MedicineRepository medicineRepository,
                                  InventoryRepository inventoryRepository,
                                  PrescriptionRepository prescriptionRepository,
                                  RestTemplate restTemplate,
                                  @Value("${order.service.url:http://localhost:8083}") String orderServiceBaseUrl) {
        this.medicineRepository = medicineRepository;
        this.inventoryRepository = inventoryRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.restTemplate = restTemplate;
        this.orderServiceBaseUrl = orderServiceBaseUrl;
    }

    public DashboardResponse getDashboard() {
        DashboardResponse dashboard = new DashboardResponse();

        dashboard.setPendingPrescriptions(
            prescriptionRepository.countByStatus(PrescriptionStatus.PENDING));
        dashboard.setLowStockMedicines(
            inventoryRepository.findByQuantityLessThanAndStatus(LOW_STOCK_THRESHOLD, InventoryStatus.ACTIVE)
                    .stream()
                    .map(Inventory::getMedicine)
                    .filter(Objects::nonNull)
                    .map(medicine -> medicine.getId())
                    .filter(Objects::nonNull)
                    .distinct()
                    .count());
        dashboard.setExpiringBatchesIn30Days(
            inventoryRepository.findByExpiryDateBeforeAndStatusNot(
                LocalDate.now().plusDays(30),
                InventoryStatus.EXPIRED).size());
        dashboard.setTotalActiveMedicines(
            medicineRepository.countActiveMedicines());

        try {
            DashboardOrderStats stats = restTemplate.getForObject(
                orderServiceBaseUrl + "/api/internal/orders/stats",
                DashboardOrderStats.class);

            if (stats != null) {
                dashboard.setTotalOrdersToday(stats.getTotalOrdersToday());
                dashboard.setPendingOrders(stats.getPendingOrders());
                dashboard.setDeliveredOrdersToday(stats.getDeliveredOrdersToday());
                dashboard.setRevenueToday(stats.getRevenueToday());
            }

            dashboard.setRecentOrderTracking(getOrderTracking());
        } catch (Exception e) {
            dashboard.setTotalOrdersToday(0);
            dashboard.setRevenueToday(BigDecimal.ZERO);
            dashboard.setRecentOrderTracking(Collections.emptyList());
        }

        return dashboard;
    }

    public List<AdminOrderTrackingResponse> getOrderTracking() {
        try {
            AdminOrderTrackingResponse[] tracking = restTemplate.getForObject(
                    orderServiceBaseUrl + "/api/internal/orders/tracking?limit=" + TRACKING_LIMIT,
                    AdminOrderTrackingResponse[].class);
            if (tracking == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(tracking);
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public static class DashboardOrderStats {
        private long totalOrdersToday;
        private long pendingOrders;
        private long deliveredOrdersToday;
        private BigDecimal revenueToday;

        public long getTotalOrdersToday() { return totalOrdersToday; }
        public long getPendingOrders() { return pendingOrders; }
        public long getDeliveredOrdersToday() { return deliveredOrdersToday; }
        public BigDecimal getRevenueToday() { return revenueToday; }
        public void setTotalOrdersToday(long v) { this.totalOrdersToday = v; }
        public void setPendingOrders(long v) { this.pendingOrders = v; }
        public void setDeliveredOrdersToday(long v) { this.deliveredOrdersToday = v; }
        public void setRevenueToday(BigDecimal v) { this.revenueToday = v; }
    }
}
