package in.cg.main.service;

import in.cg.main.entities.Category;
import in.cg.main.entities.Medicine;
import in.cg.main.repository.MedicineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportServiceTest {

    @Mock private MedicineRepository medicineRepository;

    @InjectMocks
    private AdminReportService adminReportService;

    private Medicine createMedicine() {
        Medicine m = new Medicine();
        m.setId(1L);
        m.setName("Paracetamol");
        m.setBrand("Cipla");
        m.setPrice(BigDecimal.valueOf(25.00));
        m.setStock(100);
        m.setActive(true);
        m.setRequiresPrescription(false);
        Category cat = new Category();
        cat.setName("OTC");
        m.setCategory(cat);
        return m;
    }

    @Test
    void exportMedicinesCsv_success() throws Exception {
        when(medicineRepository.findAll()).thenReturn(List.of(createMedicine()));

        byte[] csv = adminReportService.exportMedicinesCsv();

        assertNotNull(csv);
        assertTrue(csv.length > 0);
        String content = new String(csv);
        assertTrue(content.contains("Paracetamol"));
    }

    @Test
    void exportMedicinesCsv_empty() throws Exception {
        when(medicineRepository.findAll()).thenReturn(Collections.emptyList());

        byte[] csv = adminReportService.exportMedicinesCsv();

        assertNotNull(csv);
        // Should still have CSV header
        assertTrue(csv.length > 0);
    }

    @Test
    void exportMedicinesCsv_nullCategoryAndPrescriptionRequired() throws Exception {
        Medicine m = createMedicine();
        m.setCategory(null);
        m.setRequiresPrescription(true);
        m.setActive(false);
        when(medicineRepository.findAll()).thenReturn(List.of(m));

        byte[] csv = adminReportService.exportMedicinesCsv();

        assertNotNull(csv);
        String content = new String(csv);
        assertTrue(content.contains("Yes"));
        assertTrue(content.contains("No"));
        assertTrue(content.contains("Paracetamol"));
    }

    @Test
    void exportLowStockCsv_success() throws Exception {
        Medicine m = createMedicine();
        m.setStock(5);
        when(medicineRepository.findByStockLessThan(10)).thenReturn(List.of(m));

        byte[] csv = adminReportService.exportLowStockCsv();

        assertNotNull(csv);
        String content = new String(csv);
        assertTrue(content.contains("Paracetamol"));
    }

    @Test
    void exportLowStockCsv_empty() throws Exception {
        when(medicineRepository.findByStockLessThan(10)).thenReturn(Collections.emptyList());

        byte[] csv = adminReportService.exportLowStockCsv();

        assertNotNull(csv);
        assertTrue(csv.length > 0);
    }
}
