package in.cg.main.fallback;

import in.cg.main.exception.DownstreamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import in.cg.main.client.CatalogClient;
import in.cg.main.dto.MedicineDTO;
import in.cg.main.dto.PrescriptionDTO;
import in.cg.main.dto.PrescriptionUploadResponse;

@Component
public class CatalogFallback implements CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogFallback.class);

    @Override
    public MedicineDTO getMedicineById(Long id) {
        log.error("Catalog fallback invoked for medicine ID {}", id);
        throw new DownstreamServiceException("Catalog service unavailable while validating medicine. Please retry in a moment.");
    }

    @Override
    public PrescriptionDTO getPrescriptionById(Long id) {
        log.error("Catalog fallback invoked for prescription ID {}", id);
        throw new DownstreamServiceException("Catalog prescription service unavailable. Please retry in a moment.");
    }

    @Override
    public PrescriptionUploadResponse uploadPrescription(String authorization, Long customerId, Long userId, MultipartFile file) {
        log.error("Catalog fallback invoked while uploading prescription for customer {}", customerId);
        throw new DownstreamServiceException("Prescription service unavailable. Please retry in a moment.");
    }

    @Override
    public void reduceStock(Long medicineId, int quantity) {
        log.error("Catalog fallback invoked while reducing stock for medicine ID {}", medicineId);
        throw new DownstreamServiceException("Inventory service unavailable. Please retry in a moment.");
    }
}
