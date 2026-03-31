package in.cg.main.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import in.cg.main.dto.CategoryDTO;
import in.cg.main.entities.Category;
import in.cg.main.exception.ResourceNotFoundException;
import in.cg.main.repository.CategoryRepository;

@Service
public class CategoryServiceImp implements CategoryService{
	@Autowired
	private CategoryRepository repo;

	@Override
	@Cacheable(value = "categories", key = "#page + '-' + #size")
	public Page<CategoryDTO> getAllCategories(int page, int size) {
			Page<Category> med = repo.findAll(PageRequest.of(page, size));
		   return med.map(m->{
			   CategoryDTO dto = new CategoryDTO();
			   dto.setId(m.getId());
			   dto.setName(m.getName());
			   dto.setDescription(m.getDescription());
			   return dto; 
		   }) ;
	}
	@Override
	public CategoryDTO createCategory(CategoryDTO dto) {

	    Category category = new Category();
	    category.setName(dto.getName());
	    category.setDescription(dto.getDescription());

	    Category saved = repo.save(category);

	    CategoryDTO response = new CategoryDTO();
	    response.setId(saved.getId());
	    response.setName(saved.getName());
	    response.setDescription(saved.getDescription());

	    return response;
	}

	@Override
	public CategoryDTO getCategoryById(Long id) {
		Category category = repo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

		CategoryDTO dto = new CategoryDTO();
		dto.setId(category.getId());
		dto.setName(category.getName());
		dto.setDescription(category.getDescription());
		return dto;
	}

}
