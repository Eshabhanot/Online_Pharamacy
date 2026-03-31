package in.cg.main.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import in.cg.main.dto.PrescriptionInternalResponse;
import in.cg.main.dto.PrescriptionUploadResponse;
import in.cg.main.entities.Prescription;
import in.cg.main.entities.Prescription.PrescriptionStatus;
import in.cg.main.repository.PrescriptionRepository;

@Service
public class PrescriptionServiceImp implements PrescriptionService {

    @Autowired
    private PrescriptionRepository repo;

    // ── 1. Upload Prescription ───────────────────────────
    @Override
    public PrescriptionUploadResponse uploadPrescription(Long userId, MultipartFile file) {

        try {
            // ✅ Check empty file
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            // ✅ Get content type safely
            String contentType = file.getContentType();
            if (contentType == null) {
                throw new RuntimeException("Invalid file type");
            }

            // ✅ FIXED validation logic
            if (!contentType.equals("image/png") &&
                !contentType.equals("image/jpeg") &&
                !contentType.equals("application/pdf")) {

                throw new RuntimeException("File must be PNG, JPG, or PDF");
            }

            // ✅ File size check (2MB)
            long maxSize = 2 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                throw new RuntimeException("File size must be less than 2MB");
            }

            // ✅ Create directory
            String uploadDir = "C:\\uploads";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();   // fixed
            }

            // ✅ Determine file extension
            String extension;
            if (contentType.equals("image/png")) {
                extension = ".png";
            } else if (contentType.equals("image/jpeg")) {
                extension = ".jpg";
            } else {
                extension = ".pdf";
            }

            // ✅ Generate unique filename
            String filename = System.currentTimeMillis() + extension;

            // ✅ FIXED file path
            String filePath = uploadDir + "\\" + filename;

            // ✅ Save file
            file.transferTo(new File(filePath));

            // ✅ Save to DB
            Prescription p = new Prescription();
            p.setCustomerId(userId);
            p.setOrderId(null);
            p.setFileName(filename);
            p.setFilePath(filePath);
            p.setFileType(contentType);
            p.setStatus(PrescriptionStatus.PENDING);
            p.setUploadedAt(LocalDateTime.now());

            Prescription saved = repo.save(p);

            return new PrescriptionUploadResponse(
                    saved.getId(),
                    saved.getStatus().name(),
                    "Prescription uploaded successfully",
                    saved.getFileName()
            );

        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    @Override
    public PrescriptionInternalResponse getById(Long id) {
        Prescription prescription = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription Not Found"));
        return PrescriptionInternalResponse.from(prescription);
    }

    // ── 2. Get Prescriptions by User ─────────────────────
    @Override
    public List<Prescription> getByUserId(Long userId) {
        return repo.findByCustomerId(userId);
    }

    // ── 3. Get Pending Prescriptions ─────────────────────
    @Override
    public List<Prescription> getPendingPrescription() {
        return repo.findByStatus(PrescriptionStatus.PENDING);
    }

    // ── 4. Review Prescription ───────────────────────────
    @Override
    public Prescription reviewPrescription(Long id, boolean approved, String rejectionReason) {

        Prescription prescription = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription Not Found"));

        // ✅ Update status
        prescription.setStatus(
                approved ? PrescriptionStatus.APPROVED : PrescriptionStatus.REJECTED
        );

        // ✅ Set rejection reason if needed
        if (!approved) {
            prescription.setRejectionReason(rejectionReason);
        }

        prescription.setReviewedAt(LocalDateTime.now());

        return repo.save(prescription);
    }
}
