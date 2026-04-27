package in.cg.main.controller;

import in.cg.main.dto.AdminOrderTrackingResponse;
import in.cg.main.service.AdminDashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderControllerTest {

    @Mock
    private AdminDashboardService adminDashboardService;

    @InjectMocks
    private AdminOrderController adminOrderController;

    @Test
    void getOrders_returnsTrackedOrders() {
        List<AdminOrderTrackingResponse> orders = List.of(new AdminOrderTrackingResponse());
        when(adminDashboardService.getOrderTracking()).thenReturn(orders);

        ResponseEntity<List<AdminOrderTrackingResponse>> response = adminOrderController.getOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(orders, response.getBody());
        verify(adminDashboardService).getOrderTracking();
    }

    @Test
    void getOrder_returnsTrackedOrder() {
        AdminOrderTrackingResponse order = new AdminOrderTrackingResponse();
        when(adminDashboardService.getOrderTrackingById(9L)).thenReturn(order);

        ResponseEntity<AdminOrderTrackingResponse> response = adminOrderController.getOrder(9L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(order, response.getBody());
        verify(adminDashboardService).getOrderTrackingById(9L);
    }

    @Test
    void updateOrderStatus_returnsUpdatedOrder() {
        AdminOrderTrackingResponse order = new AdminOrderTrackingResponse();
        order.setStatus("Packed");
        when(adminDashboardService.updateOrderDeliveryStatus(9L, "PACKED")).thenReturn(order);

        ResponseEntity<AdminOrderTrackingResponse> response = adminOrderController.updateOrderStatus(9L, "PACKED");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(order, response.getBody());
        verify(adminDashboardService).updateOrderDeliveryStatus(9L, "PACKED");
    }
}
