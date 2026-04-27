package in.cg.main.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.cg.main.dto.AdminOrderTrackingResponse;
import in.cg.main.dto.DashboardResponse;
import in.cg.main.service.AdminDashboardService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminDashboardService.getDashboard());
    }

    @GetMapping("/dashboard/order-tracking")
    public ResponseEntity<List<AdminOrderTrackingResponse>> getOrderTracking() {
        return ResponseEntity.ok(adminDashboardService.getOrderTracking());
    }

    @PutMapping("/dashboard/order-tracking/{orderId}/status")
    public ResponseEntity<AdminOrderTrackingResponse> updateOrderDeliveryStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(adminDashboardService.updateOrderDeliveryStatus(orderId, status));
    }
}
