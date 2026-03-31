package in.cg.main.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import in.cg.main.dto.PrescriptionReviewRequest;
import in.cg.main.entities.Prescription;
import in.cg.main.service.AdminPrescriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/prescriptions")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPrescriptionController {

    private final AdminPrescriptionService adminPrescriptionService;

    public AdminPrescriptionController(
            AdminPrescriptionService adminPrescriptionService) {
        this.adminPrescriptionService = adminPrescriptionService;
    }

    // GET /api/admin/prescriptions/pending
    @GetMapping("/pending")
    public ResponseEntity<List<Prescription>> getPendingQueue() {
        return ResponseEntity.ok(
            adminPrescriptionService.getPendingPrescriptions());
    }

    // GET /api/admin/prescriptions/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Prescription> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
            adminPrescriptionService.getPrescriptionById(id));
    }

    // PUT /api/admin/prescriptions/{id}/review
    @PutMapping("/{id}/review")
    public ResponseEntity<Prescription> review(
            @PathVariable Long id,
            @RequestBody PrescriptionReviewRequest request,
            Authentication authentication) {

        // Extract admin ID from JWT — stored as username (email)
        // In real implementation you'd decode the JWT to get the admin's DB ID
        Long adminId = 1L;  // placeholder; replace with actual JWT claim

        return ResponseEntity.ok(
            adminPrescriptionService.reviewPrescription(id, request, adminId));
    }
}