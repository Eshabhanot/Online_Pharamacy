package in.cg.main.fallback;



import org.springframework.stereotype.Component;

import in.cg.main.client.CatalogClient;
import in.cg.main.dto.MedicineDTO;
import in.cg.main.dto.PrescriptionDTO;
import in.cg.main.dto.PrescriptionUploadResponse;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CatalogFallback implements CatalogClient {

    public MedicineDTO getMedicineById(Long id) {

       
        MedicineDTO dto = new MedicineDTO();
        dto.setId(id);
        dto.setName("Unavailable");
        dto.setPrice(0);
        dto.setStock(0);
        dto.setRequiresPrescription(false);

        return dto;
    }

    @Override
    public PrescriptionDTO getPrescriptionById(Long id) {
        PrescriptionDTO dto = new PrescriptionDTO();
        dto.setId(id);
        dto.setCustomerId(-1L);
        dto.setStatus("UNAVAILABLE");
        return dto;
    }

    @Override
    public PrescriptionUploadResponse uploadPrescription(String authorization, Long customerId, Long userId, MultipartFile file) {
        throw new RuntimeException("Prescription service unavailable");
    }
}
