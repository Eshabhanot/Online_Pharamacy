package in.cg.main.controller;

import in.cg.main.dto.OrderResponse;
import in.cg.main.dto.OrderTrackingResponse;
import in.cg.main.enums.OrderStatus;
import in.cg.main.security.JwtService;
import in.cg.main.security.RequestCustomerResolver;
import in.cg.main.service.CheckoutService;
import in.cg.main.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock private CheckoutService checkoutService;
    @Mock private OrderService orderService;
    @Mock private RequestCustomerResolver requestCustomerResolver;
    @Mock private JwtService jwtService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void getMyOrders_returnsPage() {
        Page<OrderResponse> page = new PageImpl<>(Collections.singletonList(new OrderResponse.Builder().id(1L).build()));
        when(requestCustomerResolver.resolve(1L, "Bearer abc")).thenReturn(1L);
        when(orderService.getCustomerOrders(1L, 0, 10)).thenReturn(page);

        ResponseEntity<Page<OrderResponse>> response = orderController.getMyOrders(1L, "Bearer abc", 0, 10);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getTotalElements());
    }

    @Test
    void getOrder_returnsOrder() {
        OrderResponse order = new OrderResponse.Builder().id(10L).build();
        when(requestCustomerResolver.resolve(1L, "Bearer abc")).thenReturn(1L);
        when(orderService.getOrderById(10L, 1L)).thenReturn(order);

        ResponseEntity<OrderResponse> response = orderController.getOrder(10L, 1L, "Bearer abc");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(10L, response.getBody().getId());
    }

    @Test
    void cancelOrder_mapsToCustomerCancelled() {
        OrderResponse order = new OrderResponse.Builder().id(10L).status("CUSTOMER_CANCELLED").build();
        when(requestCustomerResolver.resolve(1L, "Bearer abc")).thenReturn(1L);
        when(checkoutService.updateStatus(10L, OrderStatus.CUSTOMER_CANCELLED)).thenReturn(order);

        ResponseEntity<OrderResponse> response = orderController.cancelOrder(10L, 1L, "Bearer abc");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("CUSTOMER_CANCELLED", response.getBody().getStatus());
    }

    @Test
    void getOrderTracking_returnsTrackingResponse() {
        OrderTrackingResponse tracking = new OrderTrackingResponse();
        tracking.setOrderId(10L);
        tracking.setStatus("Out For Delivery");
        when(requestCustomerResolver.resolve(1L, "Bearer abc")).thenReturn(1L);
        when(orderService.getOrderTracking(10L, 1L)).thenReturn(tracking);

        ResponseEntity<OrderTrackingResponse> response = orderController.getOrderTracking(10L, 1L, "Bearer abc");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Out For Delivery", response.getBody().getStatus());
    }

    @Test
    void getOrderTracking_adminToken_usesAdminLookup() {
        OrderTrackingResponse tracking = new OrderTrackingResponse();
        tracking.setOrderId(10L);
        tracking.setStatus("Packed");
        when(jwtService.extractRole("admin-token")).thenReturn("ROLE_ADMIN");
        when(orderService.getOrderTrackingForAdmin(10L)).thenReturn(tracking);

        ResponseEntity<OrderTrackingResponse> response = orderController.getOrderTracking(10L, null, "Bearer admin-token");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Packed", response.getBody().getStatus());
    }
}
