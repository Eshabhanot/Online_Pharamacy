package in.cg.main.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import in.cg.main.dto.MedicineDTO;
import in.cg.main.dto.PrescriptionDTO;
import in.cg.main.dto.PrescriptionUploadResponse;
import in.cg.main.fallback.CatalogFallback;

@FeignClient(
        name = "catalog-service",
        url = "${catalog.service.url:http://localhost:8082}",
        fallback = CatalogFallback.class
)
public interface CatalogClient {

    @GetMapping("/api/medicines/internal/{id}")
    MedicineDTO getMedicineById(@PathVariable Long id);

    @GetMapping("/api/prescriptions/internal/{id}")
    PrescriptionDTO getPrescriptionById(@PathVariable Long id);

    @PostMapping(value = "/api/prescriptions/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    PrescriptionUploadResponse uploadPrescription(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader("X-Customer-Id") Long customerId,
            @RequestParam("userId") Long userId,
            @RequestPart("file") MultipartFile file
    );

    @PutMapping("/api/inventory/reduce")
    void reduceStock(@RequestParam Long medicineId, @RequestParam int quantity);
}
