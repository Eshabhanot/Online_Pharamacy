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
}
