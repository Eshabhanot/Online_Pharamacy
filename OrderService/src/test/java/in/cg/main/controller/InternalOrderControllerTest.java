package in.cg.main.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import in.cg.main.dto.OrderStatsResponse;
import in.cg.main.dto.OrderTrackingResponse;
import in.cg.main.service.OrderService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalOrderControllerTest {

    @Mock
    private OrderService orderService;

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
}
