package in.cg.main.service;

import org.springframework.data.domain.Page;

import in.cg.main.dto.CategoryDTO;



public interface CategoryService {
	Page<CategoryDTO> getAllCategories(int page,int size);

	CategoryDTO createCategory(CategoryDTO categoryDTO);

	CategoryDTO getCategoryById(Long id);

}
