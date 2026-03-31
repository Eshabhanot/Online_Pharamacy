package in.cg.main.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.cg.main.service.AdminReportService;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final AdminReportService adminReportService;

    public AdminReportController(AdminReportService adminReportService) {
        this.adminReportService = adminReportService;
    }

    // GET /api/admin/reports/medicines/export
    @GetMapping("/medicines/export")
    public ResponseEntity<byte[]> exportMedicines() throws Exception {
        byte[] csv = adminReportService.exportMedicinesCsv();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=medicines.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }

    // GET /api/admin/reports/low-stock/export
    @GetMapping("/low-stock/export")
    public ResponseEntity<byte[]> exportLowStock() throws Exception {
        byte[] csv = adminReportService.exportLowStockCsv();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=low-stock.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }
}