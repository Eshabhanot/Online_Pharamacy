package in.cg.main.controller;

import in.cg.main.dto.MedicineResponse;
import in.cg.main.service.AdminMedicineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal/medicines")
public class InternalMedicineController {

    private final AdminMedicineService adminMedicineService;

    public InternalMedicineController(AdminMedicineService adminMedicineService) {
        this.adminMedicineService = adminMedicineService;
    }

    @GetMapping
    public ResponseEntity<List<MedicineResponse>> getMedicines(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(adminMedicineService.getMedicinesForCatalog(name, categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicineResponse> getMedicineById(@PathVariable Long id) {
        return ResponseEntity.ok(adminMedicineService.getMedicineById(id));
    }
}
