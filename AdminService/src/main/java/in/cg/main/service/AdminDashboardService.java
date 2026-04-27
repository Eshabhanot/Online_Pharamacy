package in.cg.main.service;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import in.cg.main.dto.AdminOrderTrackingResponse;
import in.cg.main.dto.DashboardResponse;
import in.cg.main.dto.DashboardResponse.TodayOrderSummary;
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
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    private final MedicineRepository medicineRepository;
    private final InventoryRepository inventoryRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    private final String catalogServiceBaseUrl;
    private final String orderServiceBaseUrl;

    public AdminDashboardService(MedicineRepository medicineRepository,
                                  InventoryRepository inventoryRepository,
                                  PrescriptionRepository prescriptionRepository,
                                  RestTemplate restTemplate,
                                  DiscoveryClient discoveryClient,
                                  @Value("${catalog.service.url:http://localhost:8082}") String catalogServiceBaseUrl,
                                  @Value("${order.service.url:http://localhost:8083}") String orderServiceBaseUrl) {
        this.medicineRepository = medicineRepository;
        this.inventoryRepository = inventoryRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
        this.catalogServiceBaseUrl = catalogServiceBaseUrl;
        this.orderServiceBaseUrl = orderServiceBaseUrl;
    }

    public DashboardResponse getDashboard() {
        DashboardResponse dashboard = new DashboardResponse();

        dashboard.setPendingPrescriptions(fetchPendingPrescriptionCount());
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
            DashboardOrderStats stats = fetchOrderStats();

            if (stats != null) {
                dashboard.setTotalOrdersToday(stats.getTotalOrdersToday());
                dashboard.setPendingOrders(stats.getPendingOrders());
                dashboard.setDeliveredOrdersToday(stats.getDeliveredOrdersToday());
                dashboard.setRevenueToday(stats.getRevenueToday());
            }

            dashboard.setRecentOrderTracking(getOrderTracking());
            dashboard.setTodaysOrders(getTodaysOrders());
        } catch (Exception e) {
            dashboard.setTotalOrdersToday(0);
            dashboard.setRevenueToday(BigDecimal.ZERO);
            dashboard.setRecentOrderTracking(Collections.emptyList());
            dashboard.setTodaysOrders(Collections.emptyList());
        }

        return dashboard;
    }

    public List<AdminOrderTrackingResponse> getOrderTracking() {
        for (String baseUrl : candidateBaseUrls(orderServiceBaseUrl, "ORDER-SERVICE")) {
            try {
                AdminOrderTrackingResponse[] tracking = restTemplate.getForObject(
                        baseUrl + "/api/internal/orders/tracking?limit=" + TRACKING_LIMIT,
                        AdminOrderTrackingResponse[].class);
                if (tracking == null) {
                    return Collections.emptyList();
                }
                return Arrays.asList(tracking);
            } catch (Exception ex) {
            }
        }
        return Collections.emptyList();
    }

    public List<TodayOrderSummary> getTodaysOrders() {
        for (String baseUrl : candidateBaseUrls(orderServiceBaseUrl, "ORDER-SERVICE")) {
            try {
                TodayOrderSummary[] orders = restTemplate.getForObject(
                        baseUrl + "/api/internal/orders/today",
                        TodayOrderSummary[].class);
                if (orders == null) {
                    return Collections.emptyList();
                }
                return Arrays.asList(orders);
            } catch (Exception ex) {
            }
        }
        return Collections.emptyList();
    }

    public AdminOrderTrackingResponse updateOrderDeliveryStatus(Long orderId, String status) {
        for (String baseUrl : candidateBaseUrls(orderServiceBaseUrl, "ORDER-SERVICE")) {
            try {
                String url = UriComponentsBuilder
                        .fromHttpUrl(baseUrl + "/api/internal/orders/{orderId}/status")
                        .queryParam("status", status)
                        .buildAndExpand(orderId)
                        .toUriString();

                RequestEntity<Void> request = new RequestEntity<>(HttpMethod.PUT, URI.create(url));
                ResponseEntity<AdminOrderTrackingResponse> response =
                        restTemplate.exchange(request, AdminOrderTrackingResponse.class);
                return response.getBody();
            } catch (Exception ex) {
            }
        }
        throw new RuntimeException("Unable to update order status");
    }

    public AdminOrderTrackingResponse getOrderTrackingById(Long orderId) {
        for (String baseUrl : candidateBaseUrls(orderServiceBaseUrl, "ORDER-SERVICE")) {
            try {
                return restTemplate.getForObject(
                        baseUrl + "/api/internal/orders/" + orderId + "/tracking",
                        AdminOrderTrackingResponse.class);
            } catch (Exception ex) {
            }
        }
        throw new RuntimeException("Unable to fetch order tracking");
    }

    private long fetchPendingPrescriptionCount() {
        for (String baseUrl : candidateBaseUrls(catalogServiceBaseUrl, "CATALOG-SERVICE")) {
            try {
                PrescriptionSummary[] response = restTemplate.getForObject(
                        baseUrl + "/api/prescriptions/internal/pending",
                        PrescriptionSummary[].class);
                return response == null ? 0 : response.length;
            } catch (Exception ex) {
            }
        }
        return prescriptionRepository.countByStatus(PrescriptionStatus.PENDING);
    }

    private DashboardOrderStats fetchOrderStats() {
        for (String baseUrl : candidateBaseUrls(orderServiceBaseUrl, "ORDER-SERVICE")) {
            try {
                return restTemplate.getForObject(
                        baseUrl + "/api/internal/orders/stats",
                        DashboardOrderStats.class);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    private List<String> candidateBaseUrls(String configuredUrl, String serviceName) {
        Set<String> urls = new LinkedHashSet<>();
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            urls.add(configuredUrl.replaceAll("/+$", ""));
        }
        if (discoveryClient != null) {
            for (ServiceInstance instance : discoveryClient.getInstances(serviceName)) {
                urls.add(instance.getUri().toString().replaceAll("/+$", ""));
            }
        }
        return new ArrayList<>(urls);
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

    public static class PrescriptionSummary {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
