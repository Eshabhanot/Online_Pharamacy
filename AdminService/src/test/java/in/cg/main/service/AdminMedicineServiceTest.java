package in.cg.main.service;

import feign.FeignException;
import in.cg.main.client.CatalogServiceClient;
import in.cg.main.client.dto.CatalogCategoryResponse;
import in.cg.main.dto.MedicineRequest;
import in.cg.main.dto.MedicineResponse;
import in.cg.main.entities.Category;
import in.cg.main.entities.Medicine;
import in.cg.main.repository.CategoryRepository;
import in.cg.main.repository.MedicineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminMedicineServiceTest {

    @Mock private MedicineRepository medicineRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CatalogServiceClient catalogServiceClient;
    @Mock private MedicineNotificationService medicineNotificationService;

    @InjectMocks
    private AdminMedicineService adminMedicineService;

    private MedicineRequest createRequest() {
        MedicineRequest req = new MedicineRequest();
        req.setName("Paracetamol");
        req.setBrand("Cipla");
        req.setDescription("Pain relief");
        req.setDosage("500mg");
        req.setPrice(BigDecimal.valueOf(25.00));
        req.setStock(100);
        req.setRequiresPrescription(false);
        req.setCategoryId(1L);
        req.setExpiryDate(LocalDate.now().plusYears(1));
        return req;
    }

    private Medicine createMedicine() {
        Medicine m = new Medicine();
        m.setId(1L);
        m.setName("Paracetamol");
        m.setBrand("Cipla");
        m.setPrice(BigDecimal.valueOf(25.00));
        m.setStock(100);
        m.setActive(true);
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("OTC");
        m.setCategory(cat);
        return m;
    }

    private CatalogCategoryResponse createCatalogCategory() {
        CatalogCategoryResponse response = new CatalogCategoryResponse();
        response.setId(1L);
        response.setName("OTC");
        response.setDescription("Over-the-counter");
        return response;
    }

    @Test
    void addMedicine_success() {
        MedicineRequest req = createRequest();
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("OTC");

        when(medicineRepository.existsByNameAndBrand("Paracetamol", "Cipla")).thenReturn(false);
        when(catalogServiceClient.getCategoryById(1L)).thenReturn(createCatalogCategory());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(categoryRepository.save(any(Category.class))).thenReturn(cat);
        when(medicineRepository.save(any(Medicine.class))).thenAnswer(inv -> {
            Medicine m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        MedicineResponse result = adminMedicineService.addMedicine(req);

        assertNotNull(result);
        assertEquals("Paracetamol", result.getName());
        verify(medicineRepository).save(any(Medicine.class));
    }

    @Test
    void addMedicine_alreadyExists_throws() {
        MedicineRequest req = createRequest();
        when(medicineRepository.existsByNameAndBrand("Paracetamol", "Cipla")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> adminMedicineService.addMedicine(req));
    }

    @Test
    void addMedicine_categoryNotFound_throws() {
        MedicineRequest req = createRequest();
        when(medicineRepository.existsByNameAndBrand("Paracetamol", "Cipla")).thenReturn(false);
        when(catalogServiceClient.getCategoryById(1L)).thenThrow(mock(FeignException.class));

        assertThrows(RuntimeException.class, () -> adminMedicineService.addMedicine(req));
        verify(medicineRepository, never()).save(any(Medicine.class));
    }

    @Test
    void getAllMedicines_returnsList() {
        when(medicineRepository.findAll()).thenReturn(List.of(createMedicine()));

        List<MedicineResponse> result = adminMedicineService.getAllMedicines();

        assertEquals(1, result.size());
    }

    @Test
    void getMedicineById_found() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(createMedicine()));

        MedicineResponse result = adminMedicineService.getMedicineById(1L);

        assertNotNull(result);
        assertEquals("Paracetamol", result.getName());
    }

    @Test
    void getMedicineById_notFound_throws() {
        when(medicineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminMedicineService.getMedicineById(99L));
    }

    @Test
    void updateMedicine_success() {
        Medicine existing = createMedicine();
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("OTC");

        when(medicineRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(catalogServiceClient.getCategoryById(1L)).thenReturn(createCatalogCategory());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        when(categoryRepository.save(any(Category.class))).thenReturn(cat);
        when(medicineRepository.save(any(Medicine.class))).thenReturn(existing);

        MedicineResponse result = adminMedicineService.updateMedicine(1L, createRequest());

        assertNotNull(result);
        verify(medicineRepository).save(any(Medicine.class));
    }

    @Test
    void updateMedicine_medicineNotFound_throws() {
        when(medicineRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> adminMedicineService.updateMedicine(404L, createRequest()));
        verify(medicineRepository, never()).save(any(Medicine.class));
    }

    @Test
    void updateMedicine_categoryNotFound_throws() {
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(createMedicine()));
        when(catalogServiceClient.getCategoryById(1L)).thenThrow(mock(FeignException.class));

        assertThrows(RuntimeException.class,
            () -> adminMedicineService.updateMedicine(1L, createRequest()));
        verify(medicineRepository, never()).save(any(Medicine.class));
    }

    @Test
    void deactivateMedicine_success() {
        Medicine m = createMedicine();
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(m));
        when(medicineRepository.save(any(Medicine.class))).thenReturn(m);

        adminMedicineService.deactivateMedicine(1L);

        assertFalse(m.isActive());
        verify(medicineRepository).save(m);
    }

    @Test
    void deactivateMedicine_notFound_throws() {
        when(medicineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminMedicineService.deactivateMedicine(99L));
        verify(medicineRepository, never()).save(any(Medicine.class));
    }

    @Test
    void searchMedicines_returnsResults() {
        when(medicineRepository.findByNameContainingIgnoreCase("para"))
                .thenReturn(List.of(createMedicine()));

        List<MedicineResponse> result = adminMedicineService.searchMedicines("para");

        assertEquals(1, result.size());
    }
}
