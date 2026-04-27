package in.cg.main.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import in.cg.main.dto.CategoryDTO;
import in.cg.main.service.CategoryService;

@RestController
@RequestMapping({"/api/categories", "/api/catalog/categories"})
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

   
    @GetMapping

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<CategoryDTO> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return categoryService.getAllCategories(page, size);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryDTO createCategory(@RequestBody CategoryDTO categoryDTO) {
        return categoryService.createCategory(categoryDTO);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public CategoryDTO getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }
}
