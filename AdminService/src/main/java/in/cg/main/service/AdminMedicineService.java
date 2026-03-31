package in.cg.main.service;

import jakarta.transaction.Transactional;
import feign.FeignException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import in.cg.main.client.CatalogServiceClient;
import in.cg.main.client.dto.CatalogCategoryResponse;
import in.cg.main.dto.MedicineRequest;
import in.cg.main.dto.MedicineResponse;
import in.cg.main.entities.Category;
import in.cg.main.entities.Medicine;
import in.cg.main.repository.CategoryRepository;
import in.cg.main.repository.MedicineRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminMedicineService {

    private final MedicineRepository medicineRepository;
    private final CategoryRepository categoryRepository;
    private final CatalogServiceClient catalogServiceClient;
    private final MedicineNotificationService medicineNotificationService;

    public AdminMedicineService(MedicineRepository medicineRepository,
                                 CategoryRepository categoryRepository,
                                 CatalogServiceClient catalogServiceClient,
                                 MedicineNotificationService medicineNotificationService) {
        this.medicineRepository = medicineRepository;
        this.categoryRepository = categoryRepository;
        this.catalogServiceClient = catalogServiceClient;
        this.medicineNotificationService = medicineNotificationService;
    }

    // CREATE
    @Caching(evict = {
        @CacheEvict(value = "adminMedicines", allEntries = true),
        @CacheEvict(value = "dashboard", allEntries = true)
    })
    public MedicineResponse addMedicine(MedicineRequest request) {
        if (medicineRepository.existsByNameAndBrand(
                request.getName(), request.getBrand())) {
            throw new RuntimeException(
                "Medicine already exists: " + request.getName());
        }

        Category category = resolveAndSyncCategory(request.getCategoryId());

        Medicine medicine = new Medicine();
        medicine.setName(request.getName());
        medicine.setBrand(request.getBrand());
        medicine.setDescription(request.getDescription());
        medicine.setDosage(request.getDosage());
        medicine.setPrice(request.getPrice());
        medicine.setStock(request.getStock());
        medicine.setRequiresPrescription(request.isRequiresPrescription());
        medicine.setExpiryDate(request.getExpiryDate());
        medicine.setCategory(category);
        medicine.setActive(true);

        Medicine saved = medicineRepository.save(medicine);
        medicineNotificationService.publish(saved.getId(), saved.getName(), "CREATED");
        return MedicineResponse.from(saved);
    }

    // READ ALL
    @Cacheable("adminMedicines")
    public List<MedicineResponse> getAllMedicines() {
        return medicineRepository.findAll()
            .stream()
            .map(MedicineResponse::from)
            .collect(Collectors.toList());
    }

    // READ ONE
    public MedicineResponse getMedicineById(Long id) {
        Medicine medicine = medicineRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Medicine not found: " + id));
        return MedicineResponse.from(medicine);
    }

    // UPDATE
    @Caching(evict = {
        @CacheEvict(value = "adminMedicines", allEntries = true),
        @CacheEvict(value = "dashboard", allEntries = true)
    })
    public MedicineResponse updateMedicine(Long id, MedicineRequest request) {
        Medicine medicine = medicineRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Medicine not found: " + id));

        Category category = resolveAndSyncCategory(request.getCategoryId());

        medicine.setName(request.getName());
        medicine.setBrand(request.getBrand());
        medicine.setDescription(request.getDescription());
        medicine.setDosage(request.getDosage());
        medicine.setPrice(request.getPrice());
        medicine.setStock(request.getStock());
        medicine.setRequiresPrescription(request.isRequiresPrescription());
        medicine.setExpiryDate(request.getExpiryDate());
        medicine.setCategory(category);
        Medicine saved = medicineRepository.save(medicine);
        medicineNotificationService.publish(saved.getId(), saved.getName(), "UPDATED");
        return MedicineResponse.from(saved);
    }

    // SOFT DELETE — never hard delete medicines
    @Caching(evict = {
        @CacheEvict(value = "adminMedicines", allEntries = true),
        @CacheEvict(value = "dashboard", allEntries = true)
    })
    public void deactivateMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Medicine not found: " + id));
        medicine.setActive(false);
        Medicine saved = medicineRepository.save(medicine);
        medicineNotificationService.publish(saved.getId(), saved.getName(), "DEACTIVATED");
    }
    @Caching(evict = {
    	    @CacheEvict(value = "adminMedicines", allEntries = true),
    	    @CacheEvict(value = "dashboard", allEntries = true)
    	})
    	public void deleteMedicinePermanently(Long id) {
    	    Medicine medicine = medicineRepository.findById(id)
    	        .orElseThrow(() -> new RuntimeException("Medicine not found: " + id));

    	    medicineRepository.delete(medicine);
    	    medicineNotificationService.publish(medicine.getId(), medicine.getName(), "DELETED");
    	}

    // SEARCH
    public List<MedicineResponse> searchMedicines(String query) {
        return medicineRepository.findByNameContainingIgnoreCase(query)
            .stream()
            .map(MedicineResponse::from)
            .collect(Collectors.toList());
    }

    public List<MedicineResponse> getMedicinesForCatalog(String name, Long categoryId) {
        List<Medicine> medicines;
        if (name != null && !name.isBlank()) {
            medicines = medicineRepository.findByNameContainingIgnoreCase(name);
        } else if (categoryId != null) {
            medicines = medicineRepository.findByCategoryId(categoryId);
        } else {
            medicines = medicineRepository.findAll();
        }

        return medicines.stream()
                .filter(Medicine::isActive)
                .map(MedicineResponse::from)
                .collect(Collectors.toList());
    }

    private Category resolveAndSyncCategory(Long categoryId) {
        CatalogCategoryResponse catalogCategory;
        try {
            catalogCategory = catalogServiceClient.getCategoryById(categoryId);
        } catch (FeignException.NotFound ex) {
            throw new RuntimeException("Category not found in Catalog Service: " + categoryId);
        } catch (FeignException ex) {
            throw new RuntimeException("Unable to fetch category from Catalog Service");
        }

        Category category = categoryRepository.findById(categoryId).orElseGet(Category::new);
        category.setId(catalogCategory.getId());
        category.setName(catalogCategory.getName());
        category.setDescription(catalogCategory.getDescription());
        return categoryRepository.save(category);
    }
    @Caching(evict = {
    	    @CacheEvict(value = "adminMedicines", allEntries = true),
    	    @CacheEvict(value = "dashboard", allEntries = true)
    	})
    	public void restoreMedicine(Long id) {
    	    Medicine medicine = medicineRepository.findById(id)
    	        .orElseThrow(() -> new RuntimeException("Medicine not found: " + id));

    	    medicine.setActive(true);
    	    Medicine saved = medicineRepository.save(medicine);
    	    medicineNotificationService.publish(saved.getId(), saved.getName(), "RESTORED");
    	}
}
