package in.cg.main.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import in.cg.main.dto.MedicineDTO;
import in.cg.main.service.MedicineService;

@RestController
@RequestMapping({"/api/medicines"})
public class MedicineController {

    private final MedicineService medicineService;

  
    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

   
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Page<MedicineDTO>> searchMedicines(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                medicineService.searchByNameAndId(name, categoryId, page, size)
        );
    }

   
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<MedicineDTO> getMedicineById(@PathVariable Long id) {

        return ResponseEntity.ok(
                medicineService.getById(id)
        );
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<MedicineDTO> getMedicineByIdInternal(@PathVariable Long id) {
        return ResponseEntity.ok(medicineService.getById(id));
    }
}
