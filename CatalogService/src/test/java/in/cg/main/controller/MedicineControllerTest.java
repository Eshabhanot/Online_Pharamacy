package in.cg.main.controller;

import in.cg.main.dto.MedicineDTO;
import in.cg.main.service.MedicineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicineControllerTest {

    @Mock
    private MedicineService medicineService;

    @InjectMocks
    private MedicineController medicineController;

    @Test
    void searchMedicines_returnsOkWithBody() {
        MedicineDTO dto = new MedicineDTO();
        dto.setName("Paracetamol");
        Page<MedicineDTO> expected = new PageImpl<>(List.of(dto));
        when(medicineService.searchByNameAndId("Para", 2L, 0, 10)).thenReturn(expected);

        ResponseEntity<Page<MedicineDTO>> response = medicineController.searchMedicines("Para", 2L, 0, 10);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getTotalElements());
        verify(medicineService).searchByNameAndId("Para", 2L, 0, 10);
    }

    @Test
    void getMedicineById_returnsOkWithBody() {
        MedicineDTO dto = new MedicineDTO();
        dto.setId(7L);
        when(medicineService.getById(7L)).thenReturn(dto);

        ResponseEntity<MedicineDTO> response = medicineController.getMedicineById(7L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(7L, response.getBody().getId());
        verify(medicineService).getById(7L);
    }
}
