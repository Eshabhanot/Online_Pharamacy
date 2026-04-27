package in.cg.main.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import in.cg.main.dto.OrderStatsResponse;
import in.cg.main.dto.OrderResponse;
import in.cg.main.dto.OrderTrackingResponse;
import in.cg.main.enums.OrderStatus;
import in.cg.main.service.CheckoutService;
import in.cg.main.service.OrderService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalOrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private CheckoutService checkoutService;

    @InjectMocks
    private InternalOrderController internalOrderController;

    @Test
    void getOrderStats_returnsStats() {
        OrderStatsResponse stats = new OrderStatsResponse();
        stats.setPendingOrders(2);
        when(orderService.getOrderStats()).thenReturn(stats);

        ResponseEntity<OrderStatsResponse> response = internalOrderController.getOrderStats();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().getPendingOrders());
    }

    @Test
    void getOrderTracking_returnsTrackingList() {
        OrderTrackingResponse tracking = new OrderTrackingResponse();
        tracking.setOrderId(1L);
        when(orderService.getRecentOrderTracking(5)).thenReturn(List.of(tracking));

        ResponseEntity<List<OrderTrackingResponse>> response = internalOrderController.getOrderTracking(5);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getOrderTrackingByOrderId_returnsTracking() {
        OrderTrackingResponse tracking = new OrderTrackingResponse();
        tracking.setOrderId(7L);
        when(orderService.getOrderTrackingForAdmin(7L)).thenReturn(tracking);

        ResponseEntity<OrderTrackingResponse> response = internalOrderController.getOrderTrackingByOrderId(7L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(7L, response.getBody().getOrderId());
    }

    @Test
    void updateOrderStatus_returnsUpdatedTracking() {
        OrderResponse updatedOrder = new OrderResponse.Builder().id(9L).status("Out For Delivery").build();
        OrderTrackingResponse tracking = new OrderTrackingResponse();
        tracking.setOrderId(9L);
        tracking.setStatus("Out For Delivery");
        when(checkoutService.updateStatus(9L, OrderStatus.OUT_FOR_DELIVERY)).thenReturn(updatedOrder);
        when(orderService.getOrderTrackingForAdmin(9L)).thenReturn(tracking);

        ResponseEntity<OrderTrackingResponse> response =
                internalOrderController.updateOrderStatus(9L, "OUT_FOR_DELIVERY");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Out For Delivery", response.getBody().getStatus());
    }

    @Test
    void syncPrescriptionStatus_returnsOk() {
        ResponseEntity<Void> response = internalOrderController.syncPrescriptionStatus(5L, true);

        assertEquals(200, response.getStatusCode().value());
        verify(checkoutService).syncPrescriptionReview(5L, true);
    }
}
