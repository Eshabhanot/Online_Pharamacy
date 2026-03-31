package in.cg.main.service;

import in.cg.main.client.CatalogClient;
import in.cg.main.dto.PrescriptionUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PrescriptionUploadBridgeService {

    private final CatalogClient catalogClient;

    public PrescriptionUploadBridgeService(CatalogClient catalogClient) {
        this.catalogClient = catalogClient;
    }

    public PrescriptionUploadResponse uploadToCatalog(Long customerId, MultipartFile file, String authorizationHeader) {
        try {
            return catalogClient.uploadPrescription(authorizationHeader, customerId, customerId, file);
        } catch (Exception ex) {
            throw new RuntimeException("Prescription upload failed: " + ex.getMessage());
        }
    }
}
