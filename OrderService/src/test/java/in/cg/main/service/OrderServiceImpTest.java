package in.cg.main.service;

import in.cg.main.client.AuthClient;
import in.cg.main.client.dto.AuthUserResponse;
import in.cg.main.dto.OrderResponse;
import in.cg.main.dto.OrderStatsResponse;
import in.cg.main.dto.OrderTrackingResponse;
import in.cg.main.entities.Order;
import in.cg.main.enums.OrderStatus;
import in.cg.main.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImpTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private OrderServiceImp orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(100L);
        order.setOrderNumber("ORD-12345");
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setSubtotal(new BigDecimal("100.00"));
        order.setDeliveryCharge(new BigDecimal("50.00"));
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setDeliverySlot("10:00 AM - 12:00 PM");
        order.setPlacedAt(LocalDateTime.now());
    }

    @Test
    void testGetCustomerOrders() {
        Page<Order> page = new PageImpl<>(Collections.singletonList(order));
        when(orderRepository.findByCustomerId(eq(1L), any(Pageable.class))).thenReturn(page);

        Page<OrderResponse> result = orderService.getCustomerOrders(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("ORD-12345", result.getContent().get(0).getOrderNumber());
    }

    @Test
    void testGetOrderById_Success() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(100L, 1L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("ORD-12345", response.getOrderNumber());
    }

    @Test
    void testGetOrderById_Unauthorized() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(100L, 2L));
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(100L, 1L));
    }

    @Test
    void testGetOrderStats() {
        Order delivered = new Order();
        delivered.setStatus(OrderStatus.DELIVERED);
        delivered.setTotalAmount(new BigDecimal("200.00"));
        delivered.setDeliveredAt(LocalDateTime.now());

        when(orderRepository.findByPlacedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(order, delivered));
        when(orderRepository.countByStatusIn(any()))
                .thenReturn(1L);
        when(orderRepository.countByStatusAndDeliveredAtBetween(eq(OrderStatus.DELIVERED), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1L);

        OrderStatsResponse response = orderService.getOrderStats();

        assertEquals(2, response.getTotalOrdersToday());
        assertEquals(1, response.getPendingOrders());
        assertEquals(1, response.getDeliveredOrdersToday());
        assertEquals(new BigDecimal("200.00"), response.getRevenueToday());
    }

    @Test
    void testGetRecentOrderTracking() {
        Page<Order> page = new PageImpl<>(Collections.singletonList(order));
        AuthUserResponse user = new AuthUserResponse();
        user.setEmail("track@test.com");
        when(orderRepository.findAllByOrderByPlacedAtDesc(any(Pageable.class))).thenReturn(page);
        when(authClient.getUserById(1L)).thenReturn(user);

        List<OrderTrackingResponse> response = orderService.getRecentOrderTracking(5);

        assertEquals(1, response.size());
        assertEquals("track@test.com", response.get(0).getCustomerEmail());
        assertEquals("Payment Pending", response.get(0).getStatus());
        assertNotNull(response.get(0).getStatusTimeline());
        assertFalse(response.get(0).getStatusTimeline().isEmpty());
    }

    @Test
    void testGetOrderTracking_containsOrderDetailsAndStatusTimeline() {
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderTrackingResponse response = orderService.getOrderTracking(100L, 1L);

        assertEquals(100L, response.getOrderId());
        assertEquals(new BigDecimal("100.00"), response.getSubtotal());
        assertEquals(new BigDecimal("150.00"), response.getTotalAmount());
        assertNotNull(response.getStatusTimeline());
    }
}
