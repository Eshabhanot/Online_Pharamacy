package in.cg.main.controller;

import in.cg.main.service.AdminReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReportControllerTest {

    @Mock
    private AdminReportService adminReportService;

    @InjectMocks
    private AdminReportController adminReportController;

    @Test
    void exportMedicines_returnsCsvAttachment() throws Exception {
        byte[] csv = "id,name".getBytes();
        when(adminReportService.exportMedicinesCsv()).thenReturn(csv);

        ResponseEntity<byte[]> response = adminReportController.exportMedicines();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(csv, response.getBody());
        assertEquals("attachment; filename=medicines.csv",
            response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.parseMediaType("text/csv"), response.getHeaders().getContentType());
        verify(adminReportService).exportMedicinesCsv();
    }

    @Test
    void exportLowStock_returnsCsvAttachment() throws Exception {
        byte[] csv = "id,name,stock".getBytes();
        when(adminReportService.exportLowStockCsv()).thenReturn(csv);

        ResponseEntity<byte[]> response = adminReportController.exportLowStock();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(csv, response.getBody());
        assertEquals("attachment; filename=low-stock.csv",
            response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(MediaType.parseMediaType("text/csv"), response.getHeaders().getContentType());
        verify(adminReportService).exportLowStockCsv();
    }

    @Test
    void getSalesReport_returnsReport() {
        Map<String, Object> report = Map.of("totalOrdersToday", 2);
        when(adminReportService.getSalesReport()).thenReturn(report);

        ResponseEntity<Map<String, Object>> response = adminReportController.getSalesReport();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(report, response.getBody());
        verify(adminReportService).getSalesReport();
    }

    @Test
    void getInventoryReport_returnsReport() {
        Map<String, Object> report = Map.of("activeMedicines", 12L);
        when(adminReportService.getInventoryReport()).thenReturn(report);

        ResponseEntity<Map<String, Object>> response = adminReportController.getInventoryReport();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(report, response.getBody());
        verify(adminReportService).getInventoryReport();
    }

    @Test
    void getPrescriptionVolumeReport_returnsReport() {
        Map<String, Object> report = Map.of("pending", 3L);
        when(adminReportService.getPrescriptionVolumeReport()).thenReturn(report);

        ResponseEntity<Map<String, Object>> response = adminReportController.getPrescriptionVolumeReport();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(report, response.getBody());
        verify(adminReportService).getPrescriptionVolumeReport();
    }

    @Test
    void getExpiryReport_returnsReport() {
        Map<String, Object> report = Map.of("expiringIn30Days", 4);
        when(adminReportService.getExpiryReport()).thenReturn(report);

        ResponseEntity<Map<String, Object>> response = adminReportController.getExpiryReport();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(report, response.getBody());
        verify(adminReportService).getExpiryReport();
    }
}
