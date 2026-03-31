package in.cg.main.controller;

import in.cg.main.dto.PrescriptionUploadResponse;
import in.cg.main.security.RequestCustomerResolver;
import in.cg.main.service.PrescriptionUploadBridgeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/orders/prescriptions")
public class PrescriptionController {

    private final PrescriptionUploadBridgeService prescriptionUploadBridgeService;
    private final RequestCustomerResolver requestCustomerResolver;

    public PrescriptionController(PrescriptionUploadBridgeService prescriptionUploadBridgeService,
                                  RequestCustomerResolver requestCustomerResolver) {
        this.prescriptionUploadBridgeService = prescriptionUploadBridgeService;
        this.requestCustomerResolver = requestCustomerResolver;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PrescriptionUploadResponse> upload(
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestPart("file") MultipartFile file) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        return ResponseEntity.ok(
                prescriptionUploadBridgeService.uploadToCatalog(customerId, file, authorizationHeader)
        );
    }
}
