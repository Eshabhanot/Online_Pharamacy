package in.cg.main.controller;

import in.cg.main.security.JwtFilter;
import in.cg.main.security.JwtService;
import in.cg.main.security.SecurityConfig;
import in.cg.main.service.CategoryService;
import in.cg.main.service.MedicineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CategoryController.class, MedicineController.class})
@Import({SecurityConfig.class, JwtFilter.class, CatalogSecurityTest.TestSecurityBeans.class})
@AutoConfigureMockMvc
class CatalogSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CategoryService categoryService;
    @MockBean private MedicineService medicineService;

    @TestConfiguration
    static class TestSecurityBeans {
        @Bean
        JwtService jwtService() {
            return new JwtService() {
                @Override
                public boolean validateToken(String token) {
                    return true;
                }

                @Override
                public String extractUsername(String token) {
                    return "test-user";
                }

                @Override
                public String extractRole(String token) {
                    return "USER";
                }
            };
        }
    }

    @Test
    void categories_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/categories"))
               .andExpect(status().isForbidden());
    }

    @Test
    void medicines_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/medicines"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void categories_withUserRole_returns200() throws Exception {
        when(categoryService.getAllCategories(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/api/categories"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void medicines_withUserRole_returns200() throws Exception {
        when(medicineService.searchByNameAndId(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/api/medicines"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void medicineById_withUserRole_returns200() throws Exception {
        when(medicineService.getById(1L))
                .thenReturn(new in.cg.main.dto.MedicineDTO());
        mockMvc.perform(get("/api/medicines/1"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void categories_withAdminRole_returns200() throws Exception {
        when(categoryService.getAllCategories(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/api/categories"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void medicines_withAdminRole_returns200() throws Exception {
        when(medicineService.searchByNameAndId(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        mockMvc.perform(get("/api/medicines"))
               .andExpect(status().isOk());
    }
}
