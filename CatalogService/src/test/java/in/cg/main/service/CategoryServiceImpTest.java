package in.cg.main.service;

import in.cg.main.dto.CategoryDTO;
import in.cg.main.entities.Category;
import in.cg.main.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceImpTest {

    @Mock
    private CategoryRepository repo;

    @InjectMocks
    private CategoryServiceImp service;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Antibiotics");
        category.setDescription("Medicines to treat infections");
    }

    @Test
    void testGetAllCategories() {
        Page<Category> page = new PageImpl<>(Collections.singletonList(category));
        when(repo.findAll(any(Pageable.class))).thenReturn(page);

        Page<CategoryDTO> result = service.getAllCategories(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Antibiotics", result.getContent().get(0).getName());
        assertEquals("Medicines to treat infections", result.getContent().get(0).getDescription());
    }
}
