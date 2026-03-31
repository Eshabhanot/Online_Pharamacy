package in.cg.main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import in.cg.main.dto.PrescriptionInternalResponse;
import in.cg.main.dto.PrescriptionUploadResponse;
import in.cg.main.entities.Prescription;
import in.cg.main.service.PrescriptionService;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    // ── 1. Upload Prescription ───────────────────────────
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public PrescriptionUploadResponse uploadPrescription(
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestPart("file") MultipartFile file) {

        Long effectiveUserId = userId != null ? userId : customerIdHeader;
        if (effectiveUserId == null) {
            throw new RuntimeException("User id is required");
        }

        return prescriptionService.uploadPrescription(effectiveUserId, file);
    }

    @GetMapping("/internal/{id}")
    public PrescriptionInternalResponse getInternalById(@PathVariable Long id) {
        return prescriptionService.getById(id);
    }

    // ── 2. Get Prescriptions by User ─────────────────────
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public List<Prescription> getByUserId(@PathVariable Long userId) {
        return prescriptionService.getByUserId(userId);
    }

    // ── 3. Get Pending Prescriptions (Admin) ─────────────
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Prescription> getPendingPrescriptions() {
        return prescriptionService.getPendingPrescription();
    }

    // ── 4. Review Prescription (Approve / Reject) ────────
    @PutMapping("/review/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Prescription reviewPrescription(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason) {

        return prescriptionService.reviewPrescription(id, approved, rejectionReason);
    }
}
