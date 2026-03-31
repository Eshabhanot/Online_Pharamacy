package in.cg.main.service;

import in.cg.main.dto.CheckoutRequest;
import in.cg.main.dto.OrderResponse;
import in.cg.main.enums.OrderStatus;


public interface CheckoutService {

    OrderResponse startCheckout(Long customerId, String fallbackCustomerEmail, CheckoutRequest req);

    OrderResponse updateStatus(Long orderId, OrderStatus newStatus);
}
