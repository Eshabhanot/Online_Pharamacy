package in.cg.main.controller;

import in.cg.main.dto.AdminOrderTrackingResponse;
import in.cg.main.dto.DashboardResponse;
import in.cg.main.service.AdminDashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

    @Mock
    private AdminDashboardService adminDashboardService;

    @InjectMocks
    private AdminDashboardController adminDashboardController;

    @Test
    void getDashboard_returnsOkResponse() {
        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setTotalOrdersToday(2);
        dashboard.setRevenueToday(BigDecimal.valueOf(500));

        when(adminDashboardService.getDashboard()).thenReturn(dashboard);

        ResponseEntity<DashboardResponse> response = adminDashboardController.getDashboard();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dashboard, response.getBody());
        verify(adminDashboardService).getDashboard();
    }

    @Test
    void getOrderTracking_returnsOkResponse() {
        List<AdminOrderTrackingResponse> tracking = List.of(new AdminOrderTrackingResponse());
        when(adminDashboardService.getOrderTracking()).thenReturn(tracking);

        ResponseEntity<List<AdminOrderTrackingResponse>> response = adminDashboardController.getOrderTracking();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(tracking, response.getBody());
        verify(adminDashboardService).getOrderTracking();
    }
}
