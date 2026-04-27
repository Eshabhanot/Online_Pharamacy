package in.cg.main.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import in.cg.main.dto.MedicineRequest;
import in.cg.main.dto.MedicineResponse;
import in.cg.main.service.AdminMedicineService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/medicines")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMedicineController {

    private final AdminMedicineService adminMedicineService;

    public AdminMedicineController(AdminMedicineService adminMedicineService) {
        this.adminMedicineService = adminMedicineService;
    }

    
    @GetMapping
    public ResponseEntity<List<MedicineResponse>> getAllMedicines() {
        return ResponseEntity.ok(adminMedicineService.getAllMedicines());
    }

  
    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponse> getMedicine(@PathVariable Long id) {
        return ResponseEntity.ok(adminMedicineService.getMedicineById(id));
    }


    @GetMapping("/search")
    public ResponseEntity<List<MedicineResponse>> search(
            @RequestParam String query) {
        return ResponseEntity.ok(adminMedicineService.searchMedicines(query));
    }

    // POST /api/admin/medicines
    @PostMapping
    public ResponseEntity<MedicineResponse> addMedicine(
            @RequestBody @Valid MedicineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(adminMedicineService.addMedicine(request));
    }

    // PUT /api/admin/medicines/{id}
    @PutMapping("/{id}")
    public ResponseEntity<MedicineResponse> updateMedicine(
            @PathVariable Long id,
            @RequestBody @Valid MedicineRequest request) {
        return ResponseEntity.ok(adminMedicineService.updateMedicine(id, request));
    }

    // DELETE /api/admin/medicines/{id}  — soft delete
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deactivateMedicine(@PathVariable Long id) {
//        adminMedicineService.deactivateMedicine(id);
//        return ResponseEntity.ok("Medicine deactivated successfully");
//    }
    
    @DeleteMapping("/{id}")
    public String deleteMedicine(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean permanent) {

        if (permanent) {
            adminMedicineService.deleteMedicinePermanently(id);
            return "Medicine permanently deleted";
        } else {
            adminMedicineService.deactivateMedicine(id);
            return "Medicine deactivated (soft delete)";
        }
    }
    
    @PutMapping("/{id}/restore")
    public String restoreMedicine(@PathVariable Long id) {
        adminMedicineService.restoreMedicine(id);
        return "Medicine restored successfully";
    }
}
