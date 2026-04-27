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
import in.cg.main.service.AdminDashboardService;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminDashboardService adminDashboardService;

    public AdminOrderController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    public ResponseEntity<List<AdminOrderTrackingResponse>> getOrders() {
        return ResponseEntity.ok(adminDashboardService.getOrderTracking());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<AdminOrderTrackingResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(adminDashboardService.getOrderTrackingById(orderId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<AdminOrderTrackingResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(adminDashboardService.updateOrderDeliveryStatus(orderId, status));
    }
}
