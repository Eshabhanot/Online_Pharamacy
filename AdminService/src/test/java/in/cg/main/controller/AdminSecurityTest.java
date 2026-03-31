package in.cg.main.controller;

import in.cg.main.dto.DashboardResponse;
import in.cg.main.security.JwtFilter;
import in.cg.main.service.AdminDashboardService;
import in.cg.main.service.AdminMedicineService;
import in.cg.main.service.AdminPrescriptionService;
import in.cg.main.service.AdminReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AdminDashboardController.class,
        AdminMedicineController.class,
        AdminPrescriptionController.class,
        AdminReportController.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "eureka.client.enabled=false"
})
class AdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private AdminDashboardService adminDashboardService;
    @MockBean private AdminMedicineService adminMedicineService;
    @MockBean private AdminPrescriptionService adminPrescriptionService;
    @MockBean private AdminReportService adminReportService;
    @MockBean private JwtFilter jwtFilter;

    @Test
    @WithMockUser(roles = "ADMIN")
    void dashboard_returns200() throws Exception {
        when(adminDashboardService.getDashboard()).thenReturn(new DashboardResponse());
        mockMvc.perform(get("/api/admin/dashboard")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void medicines_returns200() throws Exception {
        when(adminMedicineService.getAllMedicines()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/admin/medicines")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void prescriptions_returns200() throws Exception {
        when(adminPrescriptionService.getPendingPrescriptions()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/admin/prescriptions/pending")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reports_returns200() throws Exception {
        when(adminReportService.exportMedicinesCsv()).thenReturn("data".getBytes());
        mockMvc.perform(get("/api/admin/reports/medicines/export")).andExpect(status().isOk());
    }
}
