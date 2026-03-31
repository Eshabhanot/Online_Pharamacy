package in.cg.main.controller;

import in.cg.main.dto.OrderResponse;
import in.cg.main.dto.OrderTrackingResponse;
import in.cg.main.enums.OrderStatus;
import in.cg.main.security.RequestCustomerResolver;
import in.cg.main.service.CheckoutService;
import in.cg.main.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderService orderService;
    private final RequestCustomerResolver requestCustomerResolver;

    public OrderController(CheckoutService checkoutService,
                           OrderService orderService,
                           RequestCustomerResolver requestCustomerResolver) {
        this.checkoutService = checkoutService;
        this.orderService = orderService;
        this.requestCustomerResolver = requestCustomerResolver;
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        return ResponseEntity.ok(orderService.getCustomerOrders(customerId, page, size));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        return ResponseEntity.ok(orderService.getOrderById(id, customerId));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{id}/tracking")
    public ResponseEntity<OrderTrackingResponse> getOrderTracking(
            @PathVariable Long id,
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        return ResponseEntity.ok(orderService.getOrderTracking(id, customerId));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/tracking/{id}")
    public ResponseEntity<OrderTrackingResponse> getOrderTrackingAlias(
            @PathVariable Long id,
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        return ResponseEntity.ok(orderService.getOrderTracking(id, customerId));
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        return ResponseEntity.ok(checkoutService.updateStatus(id, OrderStatus.CUSTOMER_CANCELLED));
    }
}
