package in.cg.main.service;

import in.cg.main.client.AdminMedicineClient;
import in.cg.main.client.dto.AdminMedicineResponse;
import in.cg.main.dto.MedicineDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MedicineServiceImpTest {

    @Mock
    private AdminMedicineClient adminMedicineClient;

    @InjectMocks
    private MedicineServiceImp service;

    private AdminMedicineResponse medicine;

    @BeforeEach
    void setUp() {
        medicine = new AdminMedicineResponse();
        medicine.setId(1L);
        medicine.setName("Paracetamol");
        medicine.setBrand("Cipla");
        medicine.setDescription("Fever medicine");
        medicine.setDosage("500mg");
        medicine.setPrice(BigDecimal.valueOf(10.0));
        medicine.setStock(100);
        medicine.setRequiresPrescription(false);
        medicine.setActive(true);
        medicine.setCategoryId(1L);
        medicine.setCategoryName("Analgesics");
        medicine.setExpiryDate(LocalDate.parse("2027-01-01"));
    }

    @Test
    void testGetById_Success() {
        when(adminMedicineClient.getMedicineById(1L)).thenReturn(medicine);

        MedicineDTO dto = service.getById(1L);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Paracetamol", dto.getName());
        assertEquals("Analgesics", dto.getCategoryName());
    }

    @Test
    void testGetById_Success_WithNullCategory() {
        medicine.setCategoryName(null);
        when(adminMedicineClient.getMedicineById(1L)).thenReturn(medicine);

        MedicineDTO dto = service.getById(1L);

        assertNotNull(dto);
        assertNull(dto.getCategoryName());
    }

    @Test
    void testGetById_NotFound() {
        when(adminMedicineClient.getMedicineById(1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> service.getById(1L));
    }

    @Test
    void testSearchByNameAndId_FindAll() {
        when(adminMedicineClient.getMedicines(null, null)).thenReturn(Collections.singletonList(medicine));

        Page<MedicineDTO> result = service.searchByNameAndId(null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Paracetamol", result.getContent().get(0).getName());
    }

    @Test
    void testSearchByNameAndId_FindByName() {
        when(adminMedicineClient.getMedicines("Para", null)).thenReturn(Collections.singletonList(medicine));

        Page<MedicineDTO> result = service.searchByNameAndId("Para", null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Paracetamol", result.getContent().get(0).getName());
    }

    @Test
    void testSearchByNameAndId_FindByNameAndCategory() {
        when(adminMedicineClient.getMedicines("Para", 1L)).thenReturn(Collections.singletonList(medicine));

        Page<MedicineDTO> result = service.searchByNameAndId("Para", 1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Paracetamol", result.getContent().get(0).getName());
    }

    @Test
    void testSearchByNameAndId_FindByCategoryOnly() {
        when(adminMedicineClient.getMedicines(null, 1L)).thenReturn(Collections.singletonList(medicine));

        Page<MedicineDTO> result = service.searchByNameAndId(null, 1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Paracetamol", result.getContent().get(0).getName());
    }

    @Test
    void testSearchByNameAndId_InactiveOrZeroStockMappedToNull() {
        AdminMedicineResponse inactive = new AdminMedicineResponse();
        inactive.setId(2L);
        inactive.setName("Inactive");
        inactive.setActive(false);
        inactive.setStock(10);

        AdminMedicineResponse zeroStock = new AdminMedicineResponse();
        zeroStock.setId(3L);
        zeroStock.setName("Zero");
        zeroStock.setActive(true);
        zeroStock.setStock(0);

        when(adminMedicineClient.getMedicines(null, null)).thenReturn(List.of(inactive, zeroStock));

        Page<MedicineDTO> result = service.searchByNameAndId(null, null, 0, 10);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testSearchByNameAndId_EmptyNameFallsBackToFindAll() {
        when(adminMedicineClient.getMedicines("", null)).thenReturn(Collections.singletonList(medicine));

        Page<MedicineDTO> result = service.searchByNameAndId("", null, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Paracetamol", result.getContent().get(0).getName());
    }
}
