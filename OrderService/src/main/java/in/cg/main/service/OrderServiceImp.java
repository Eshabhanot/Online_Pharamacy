package in.cg.main.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import in.cg.main.client.AuthClient;
import in.cg.main.client.dto.AuthUserResponse;
import in.cg.main.dto.OrderResponse;
import in.cg.main.dto.OrderStatsResponse;
import in.cg.main.dto.OrderTrackingResponse;
import in.cg.main.entities.Order;
import in.cg.main.entities.Payment;
import in.cg.main.enums.OrderStatus;
import in.cg.main.exception.ResourceNotFoundException;
import in.cg.main.repository.OrderRepository;
import in.cg.main.util.OrderStatusFormatter;

@Service
public class OrderServiceImp implements OrderService {

    private static final Set<OrderStatus> REVENUE_STATUSES = Set.of(
            OrderStatus.PAID,
            OrderStatus.PACKED,
            OrderStatus.OUT_FOR_DELIVERY,
            OrderStatus.DELIVERED
    );

    private final OrderRepository orderRepository;
    private final AuthClient authClient;

    public OrderServiceImp(OrderRepository orderRepository, AuthClient authClient) {
        this.orderRepository = orderRepository;
        this.authClient = authClient;
    }

    @Override
    public Page<OrderResponse> getCustomerOrders(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("placedAt").descending());
        return orderRepository.findByCustomerId(customerId, pageable).map(this::mapToDto);
    }

    @Override
    public OrderResponse getOrderById(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!Objects.equals(order.getCustomerId(), customerId)) {
            throw new RuntimeException("Unauthorized access");
        }

        return mapToDto(order);
    }

    @Override
    public OrderTrackingResponse getOrderTracking(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!Objects.equals(order.getCustomerId(), customerId)) {
            throw new RuntimeException("Unauthorized access");
        }

        return mapTrackingDto(order);
    }

    @Override
    public OrderStatsResponse getOrderStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Order> todaysOrders = orderRepository.findByPlacedAtBetween(startOfDay, endOfDay);

        OrderStatsResponse response = new OrderStatsResponse();
        response.setTotalOrdersToday(todaysOrders.size());
        response.setPendingOrders(orderRepository.countByStatusIn(List.of(
                OrderStatus.CHECKOUT_STARTED,
                OrderStatus.PRESCRIPTION_PENDING,
                OrderStatus.PAYMENT_PENDING
        )));
        response.setDeliveredOrdersToday(orderRepository.countByStatusAndDeliveredAtBetween(
                OrderStatus.DELIVERED, startOfDay, endOfDay));
        response.setRevenueToday(todaysOrders.stream()
                .filter(order -> REVENUE_STATUSES.contains(order.getStatus()))
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return response;
    }

    @Override
    public List<OrderTrackingResponse> getRecentOrderTracking(int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(limit, 1), Sort.by("placedAt").descending());
        return orderRepository.findAllByOrderByPlacedAtDesc(pageable)
                .stream()
                .map(this::mapTrackingDto)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToDto(Order order) {
        OrderResponse.AddressDto addressDto = null;
        if (order.getAddress() != null) {
            addressDto = new OrderResponse.AddressDto();
            addressDto.setFullName(order.getAddress().getFullName());
            addressDto.setAddressLine1(order.getAddress().getAddressLine1());
            addressDto.setCity(order.getAddress().getCity());
            addressDto.setPincode(order.getAddress().getPincode());
        }

        List<OrderResponse.OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> {
                    OrderResponse.OrderItemDto dto = new OrderResponse.OrderItemDto();
                    dto.setMedicineName(item.getMedicineName());
                    dto.setQuantity(item.getQuantity());
                    dto.setUnitPrice(item.getUnitPrice());
                    dto.setTotalPrice(item.getTotalPrice());
                    return dto;
                })
                .collect(Collectors.toList());

        OrderResponse.PaymentDto paymentDto = null;
        Payment payment = order.getPayment();
        if (payment != null) {
            paymentDto = new OrderResponse.PaymentDto();
            paymentDto.setMethod(payment.getMethod());
            paymentDto.setStatus(OrderStatusFormatter.toDisplayName(payment.getStatus()));
            paymentDto.setTransactionId(payment.getTransactionId());
        }

        return new OrderResponse.Builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(OrderStatusFormatter.toDisplayName(order.getStatus()))
                .subtotal(order.getSubtotal())
                .deliveryCharge(order.getDeliveryCharge())
                .totalAmount(order.getTotalAmount())
                .deliverySlot(order.getDeliverySlot())
                .placedAt(order.getPlacedAt())
                .deliveredAt(order.getDeliveredAt())
                .items(itemDtos)
                .deliveryAddress(addressDto)
                .payment(paymentDto)
                .build();
    }

    private OrderTrackingResponse mapTrackingDto(Order order) {
        OrderTrackingResponse tracking = new OrderTrackingResponse();
        tracking.setOrderId(order.getId());
        tracking.setOrderNumber(order.getOrderNumber());
        tracking.setCustomerId(order.getCustomerId());
        tracking.setCustomerEmail(fetchCustomerEmailQuietly(order.getCustomerId()));
        tracking.setStatus(OrderStatusFormatter.toDisplayName(order.getStatus()));
        tracking.setPaymentStatus(order.getPayment() != null && order.getPayment().getStatus() != null
                ? OrderStatusFormatter.toDisplayName(order.getPayment().getStatus())
                : null);
        tracking.setDeliverySlot(order.getDeliverySlot());
        tracking.setPlacedAt(order.getPlacedAt());
        tracking.setDeliveredAt(order.getDeliveredAt());
        return tracking;
    }

    private String fetchCustomerEmailQuietly(Long customerId) {
        try {
            AuthUserResponse user = authClient.getUserById(customerId);
            return user != null ? user.getEmail() : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
