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

 
    @Override
    public PrescriptionUploadResponse uploadPrescription(Long userId, MultipartFile file) {

        try {
           
            if (file.isEmpty()) {
                throw new RuntimeException("File is empty");
            }

            
            String contentType = file.getContentType();
            if (contentType == null) {
                throw new RuntimeException("Invalid file type");
            }

          
            if (!contentType.equals("image/png") &&
                !contentType.equals("image/jpeg") &&
                !contentType.equals("application/pdf")) {

                throw new RuntimeException("File must be PNG, JPG, or PDF");
            }

           
            long maxSize = 2 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                throw new RuntimeException("File size must be less than 2MB");
            }

           
            String uploadDir = "C:\\uploads";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();  
            }

        
            String extension;
            if (contentType.equals("image/png")) {
                extension = ".png";
            } else if (contentType.equals("image/jpeg")) {
                extension = ".jpg";
            } else {
                extension = ".pdf";
            }

            
            String filename = System.currentTimeMillis() + extension;

           
            String filePath = uploadDir + "\\" + filename;

            
            file.transferTo(new File(filePath));

          
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

   
    @Override
    public List<Prescription> getByUserId(Long userId) {
        return repo.findByCustomerId(userId);
    }

   
    @Override
    public List<Prescription> getPendingPrescription() {
        return repo.findByStatus(PrescriptionStatus.PENDING);
    }

   
    @Override
    public Prescription reviewPrescription(Long id, boolean approved, String rejectionReason) {

        Prescription prescription = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription Not Found"));

      
        prescription.setStatus(
                approved ? PrescriptionStatus.APPROVED : PrescriptionStatus.REJECTED
        );

       
        if (!approved) {
            prescription.setRejectionReason(rejectionReason);
        }

        prescription.setReviewedAt(LocalDateTime.now());

        return repo.save(prescription);
    }
}
