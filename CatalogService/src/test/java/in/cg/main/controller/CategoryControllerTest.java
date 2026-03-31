package in.cg.main.controller;

import in.cg.main.dto.CategoryDTO;
import in.cg.main.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void getAllCategories_returnsPageFromService() {
        CategoryDTO dto = new CategoryDTO();
        dto.setName("Pain Relief");
        dto.setDescription("desc");
        Page<CategoryDTO> expected = new PageImpl<>(List.of(dto));
        when(categoryService.getAllCategories(1, 5)).thenReturn(expected);

        Page<CategoryDTO> result = categoryController.getAllCategories(1, 5);

        assertEquals(1, result.getTotalElements());
        assertEquals("Pain Relief", result.getContent().get(0).getName());
        verify(categoryService).getAllCategories(1, 5);
    }
}
