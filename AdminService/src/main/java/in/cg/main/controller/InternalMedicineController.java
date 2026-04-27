package in.cg.main.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.cg.main.dto.MedicineResponse;
import in.cg.main.service.AdminMedicineService;

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
