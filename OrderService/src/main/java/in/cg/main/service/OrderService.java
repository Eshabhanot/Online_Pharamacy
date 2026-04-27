package in.cg.main.service;

import java.util.List;

import org.springframework.data.domain.Page;

import in.cg.main.dto.OrderResponse;
import in.cg.main.dto.OrderStatsResponse;
import in.cg.main.dto.OrderTrackingResponse;

public interface OrderService {

    Page<OrderResponse> getCustomerOrders(Long customerId, int page, int size);

    OrderResponse getOrderById(Long orderId, Long customerId);

    OrderResponse getOrderByIdForAdmin(Long orderId);

    OrderTrackingResponse getOrderTracking(Long orderId, Long customerId);

    OrderTrackingResponse getOrderTrackingForAdmin(Long orderId);

    OrderStatsResponse getOrderStats();

    List<OrderTrackingResponse> getRecentOrderTracking(int limit);
}
