package in.cg.main.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import in.cg.main.entities.Medicine;
import in.cg.main.enums.InventoryStatus;
import in.cg.main.enums.PrescriptionStatus;
import in.cg.main.repository.InventoryRepository;
import in.cg.main.repository.MedicineRepository;
import in.cg.main.repository.PrescriptionRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

@Service
public class AdminReportService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final MedicineRepository medicineRepository;
    private final InventoryRepository inventoryRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    private final String orderServiceBaseUrl;

    public AdminReportService(MedicineRepository medicineRepository,
                              InventoryRepository inventoryRepository,
                              PrescriptionRepository prescriptionRepository,
                              RestTemplate restTemplate,
                              DiscoveryClient discoveryClient,
                              @Value("${order.service.url:http://localhost:8083}") String orderServiceBaseUrl) {
        this.medicineRepository = medicineRepository;
        this.inventoryRepository = inventoryRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
        this.orderServiceBaseUrl = orderServiceBaseUrl;
    }

    public Map<String, Object> getSalesReport() {
        AdminDashboardService.DashboardOrderStats stats = fetchOrderStats();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalOrdersToday", stats != null ? stats.getTotalOrdersToday() : 0);
        report.put("pendingOrders", stats != null ? stats.getPendingOrders() : 0);
        report.put("deliveredOrdersToday", stats != null ? stats.getDeliveredOrdersToday() : 0);
        report.put("revenueToday", stats != null ? stats.getRevenueToday() : null);
        return report;
    }

    public Map<String, Object> getInventoryReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activeMedicines", medicineRepository.countActiveMedicines());
        report.put("lowStockBatches",
                inventoryRepository.findByQuantityLessThanAndStatus(LOW_STOCK_THRESHOLD, InventoryStatus.ACTIVE).size());
        report.put("expiringBatchesIn30Days",
                inventoryRepository.findByExpiryDateBeforeAndStatusNot(
                        LocalDate.now().plusDays(30), InventoryStatus.EXPIRED).size());
        return report;
    }

    public Map<String, Object> getPrescriptionVolumeReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("pending", prescriptionRepository.countByStatus(PrescriptionStatus.PENDING));
        report.put("approved", prescriptionRepository.countByStatus(PrescriptionStatus.APPROVED));
        report.put("rejected", prescriptionRepository.countByStatus(PrescriptionStatus.REJECTED));
        return report;
    }

    public Map<String, Object> getExpiryReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("expiringIn7Days",
                inventoryRepository.findByExpiryDateBeforeAndStatusNot(
                        LocalDate.now().plusDays(7), InventoryStatus.EXPIRED).size());
        report.put("expiringIn30Days",
                inventoryRepository.findByExpiryDateBeforeAndStatusNot(
                        LocalDate.now().plusDays(30), InventoryStatus.EXPIRED).size());
        return report;
    }

    // ✅ Export full medicine catalog as CSV
    public byte[] exportMedicinesCsv() throws IOException {
        List<Medicine> medicines = medicineRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "Name", "Brand", "Category", "Price",
                             "Stock", "Requires Prescription", "Expiry Date", "Active"))) {

            for (Medicine m : medicines) {
                printer.printRecord(
                        m.getId(),
                        m.getName(),
                        m.getBrand(),
                        m.getCategory() != null ? m.getCategory().getName() : "",
                        m.getPrice(),
                        m.getStock(),
                        m.isRequiresPrescription() ? "Yes" : "No",
                        m.getExpiryDate(),
                        m.isActive() ? "Yes" : "No"
                );
            }

            printer.flush();
            return out.toByteArray();
        }
    }

    // ✅ Export low-stock medicines as CSV
    public byte[] exportLowStockCsv() throws IOException {
        List<Medicine> lowStock = medicineRepository.findByStockLessThan(10);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "Name", "Brand", "Current Stock", "Price"))) {

            for (Medicine m : lowStock) {
                printer.printRecord(
                        m.getId(),
                        m.getName(),
                        m.getBrand(),
                        m.getStock(),
                        m.getPrice()
                );
            }

            printer.flush();
            return out.toByteArray();
        }
    }

    private AdminDashboardService.DashboardOrderStats fetchOrderStats() {
        for (String baseUrl : candidateBaseUrls(orderServiceBaseUrl, "ORDER-SERVICE")) {
            try {
                return restTemplate.getForObject(
                        baseUrl + "/api/internal/orders/stats",
                        AdminDashboardService.DashboardOrderStats.class);
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
}
