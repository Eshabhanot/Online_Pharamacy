package in.cg.main.service;

import in.cg.main.client.AuthClient;
import in.cg.main.client.CatalogClient;
import in.cg.main.client.dto.AuthAddressResponse;
import in.cg.main.client.dto.AuthUserResponse;
import in.cg.main.dto.CheckoutRequest;
import in.cg.main.dto.MedicineDTO;
import in.cg.main.dto.OrderResponse;
import in.cg.main.dto.PrescriptionDTO;
import in.cg.main.entities.Address;
import in.cg.main.entities.Cart;
import in.cg.main.entities.CartItem;
import in.cg.main.entities.Order;
import in.cg.main.entities.OrderItem;
import in.cg.main.entities.Payment;
import in.cg.main.enums.OrderStatus;
import in.cg.main.exception.DownstreamServiceException;
import in.cg.main.exception.ResourceNotFoundException;
import in.cg.main.repository.AddressRepository;
import in.cg.main.repository.CartRepository;
import in.cg.main.repository.OrderRepository;
import in.cg.main.util.OrderStatusFormatter;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class CheckoutServiceImpl implements CheckoutService {
    private static final Logger log = LoggerFactory.getLogger(CheckoutServiceImpl.class);

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final AuthClient authClient;
    private final CatalogClient catalogClient;
    private final NotificationService notificationService;
    private final EmailService emailService;

    private static final BigDecimal DELIVERY_CHARGE = BigDecimal.valueOf(49);
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = BigDecimal.valueOf(500);

    public CheckoutServiceImpl(CartRepository cartRepository,
                               OrderRepository orderRepository,
                               AddressRepository addressRepository,
                               AuthClient authClient,
                               CatalogClient catalogClient,
                               NotificationService notificationService,
                               EmailService emailService) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.addressRepository = addressRepository;
        this.authClient = authClient;
        this.catalogClient = catalogClient;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Override
    public OrderResponse startCheckout(Long customerId, String fallbackCustomerEmail, CheckoutRequest req) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Address address = resolveAddress(customerId, req.getAddressId());

        boolean needsRx = cart.getItems().stream().anyMatch(CartItem::isRequiresPrescription);
        PrescriptionDTO prescription = null;
        if (needsRx) {
            if (req.getPrescriptionId() == null) {
                throw new RuntimeException("Prescription is required for this order");
            }
            prescription = fetchPrescriptionOrThrow(req.getPrescriptionId());
            validatePrescriptionForCustomer(customerId, prescription);
        }

        for (CartItem item : cart.getItems()) {
            MedicineDTO medicine = fetchMedicineOrThrow(item.getMedicineId());

            if (item.getQuantity() > medicine.getStock()) {
                throw new RuntimeException("Stock changed for " + medicine.getName());
            }

            if (medicine.isRequiresPrescription() && prescription == null) {
                throw new RuntimeException("Prescription required for " + medicine.getName());
            }
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerId(customerId);
        order.setAddress(address);
        order.setDeliverySlot(req.getDeliverySlot());

        if (needsRx) {
            order.setPrescriptionId(req.getPrescriptionId());
        }
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setMedicineId(ci.getMedicineId());
            oi.setMedicineName(ci.getMedicineName());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());

            BigDecimal lineTotal = ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            oi.setTotalPrice(lineTotal);

            order.getItems().add(oi);
            subtotal = subtotal.add(lineTotal);
        }

        BigDecimal deliveryCharge = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0 ? BigDecimal.ZERO : DELIVERY_CHARGE;
        order.setSubtotal(subtotal);
        order.setDeliveryCharge(deliveryCharge);
        order.setTotalAmount(subtotal.add(deliveryCharge));

        Order saved = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        String customerEmail = resolveCustomerEmail(saved.getCustomerId(), fallbackCustomerEmail);
        notificationService.sendOrderNotification(
                saved.getId(), customerEmail, OrderStatusFormatter.toDisplayName(saved.getStatus()),
                "Order " + saved.getOrderNumber() + " placed successfully");
        emailService.sendOrderEmail(
                customerEmail,
                "Order placed successfully",
                "Your order " + saved.getOrderNumber() + " has been placed with status "
                        + OrderStatusFormatter.toDisplayName(saved.getStatus()) + "."
        );

        return buildOrderResponse(saved);
    }

    @Override
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        validateTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }

        Order saved = orderRepository.save(order);
        String customerEmail = resolveCustomerEmail(saved.getCustomerId(), null);
        notificationService.sendOrderNotification(
                saved.getId(), customerEmail, OrderStatusFormatter.toDisplayName(newStatus),
                "Order #" + saved.getOrderNumber() + " status updated to " + OrderStatusFormatter.toDisplayName(newStatus));
        emailService.sendOrderEmail(
                customerEmail,
                "Order status updated",
                "Your order " + saved.getOrderNumber() + " is now " + OrderStatusFormatter.toDisplayName(newStatus) + "."
        );

        return buildOrderResponse(saved);
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        if (next == OrderStatus.CHECKOUT_STARTED
                || next == OrderStatus.PRESCRIPTION_PENDING
                || next == OrderStatus.PAYMENT_PENDING) {
            throw new IllegalArgumentException("Order cannot be moved to a pending status");
        }

        Map<OrderStatus, List<OrderStatus>> allowed = Map.of(
                OrderStatus.PRESCRIPTION_PENDING, List.of(OrderStatus.PRESCRIPTION_APPROVED, OrderStatus.PRESCRIPTION_REJECTED),
                OrderStatus.PRESCRIPTION_APPROVED, List.of(OrderStatus.PAYMENT_PENDING),
                OrderStatus.PAYMENT_PENDING, List.of(OrderStatus.PAID, OrderStatus.PAYMENT_FAILED, OrderStatus.CUSTOMER_CANCELLED),
                OrderStatus.PAID, List.of(OrderStatus.PACKED, OrderStatus.ADMIN_CANCELLED),
                OrderStatus.PACKED, List.of(OrderStatus.OUT_FOR_DELIVERY),
                OrderStatus.OUT_FOR_DELIVERY, List.of(OrderStatus.DELIVERED)
        );

        List<OrderStatus> validNext = allowed.getOrDefault(current, List.of());
        if (!validNext.contains(next)) {
            throw new RuntimeException("Invalid transition: " + current + " -> " + next);
        }
    }

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seq = String.format("%05d", (int) (Math.random() * 99999));
        return "ORD-" + date + "-" + seq;
    }

    private Address resolveAddress(Long customerId, Long requestedAddressId) {
        if (requestedAddressId != null) {
            Address byId = addressRepository.findByIdAndCustomerId(requestedAddressId, customerId).orElse(null);
            if (byId != null) {
                return byId;
            }

            Address byAuthId = addressRepository.findByAuthAddressIdAndCustomerId(requestedAddressId, customerId).orElse(null);
            if (byAuthId != null) {
                return byAuthId;
            }

            List<AuthAddressResponse> authAddresses = fetchAuthAddressesOrThrow(customerId);
            AuthAddressResponse matched = authAddresses.stream()
                    .filter(a -> requestedAddressId.equals(a.getId()))
                    .findFirst()
                    .orElse(null);
            if (matched != null) {
                return upsertFromAuthAddress(customerId, matched);
            }
        }

        Address defaultLocal = addressRepository.findByCustomerIdAndIsDefaultTrue(customerId).orElse(null);
        if (defaultLocal != null) {
            return defaultLocal;
        }

        List<AuthAddressResponse> authAddresses = fetchAuthAddressesOrThrow(customerId);
        AuthAddressResponse authDefault = authAddresses.stream()
                .filter(AuthAddressResponse::isDefault)
                .findFirst()
                .orElse(null);
        if (authDefault != null) {
            return upsertFromAuthAddress(customerId, authDefault);
        }

        throw new ResourceNotFoundException("Address not found for this customer");
    }

    private MedicineDTO fetchMedicineOrThrow(Long medicineId) {
        try {
            return catalogClient.getMedicineById(medicineId);
        } catch (FeignException ex) {
            throw new DownstreamServiceException("Catalog service unavailable while validating medicine");
        }
    }

    private PrescriptionDTO fetchPrescriptionOrThrow(Long prescriptionId) {
        try {
            return catalogClient.getPrescriptionById(prescriptionId);
        } catch (FeignException ex) {
            throw new DownstreamServiceException("Catalog prescription service unavailable. Please retry in a moment.");
        }
    }

    private void validatePrescriptionForCustomer(Long customerId, PrescriptionDTO prescription) {
        if (prescription == null || prescription.getCustomerId() == null
                || !customerId.equals(prescription.getCustomerId())) {
            throw new RuntimeException("Prescription does not belong to this customer");
        }

        String status = prescription.getStatus();
        if (status == null) {
            throw new RuntimeException("Prescription status is missing");
        }

        if ("PENDING".equalsIgnoreCase(status)) {
            throw new RuntimeException("Prescription is still pending review");
        }

        if ("REJECTED".equalsIgnoreCase(status)) {
            throw new RuntimeException("Prescription was rejected");
        }

        if (!"APPROVED".equalsIgnoreCase(status)) {
            throw new RuntimeException("Unsupported prescription status: " + status);
        }
    }

    private List<AuthAddressResponse> fetchAuthAddressesOrThrow(Long customerId) {
        try {
            return authClient.getUserAddresses(customerId);
        } catch (FeignException ex) {
            throw new DownstreamServiceException("Auth address service unavailable. Please retry in a moment.");
        }
    }

    private String fetchCustomerEmailQuietly(Long customerId) {
        try {
            AuthUserResponse user = authClient.getUserById(customerId);
            return user != null ? user.getEmail() : null;
        } catch (Exception ex) {
            log.warn("Unable to fetch customer email from Auth service for userId={}: {}", customerId, ex.getMessage());
            return null;
        }
    }

    String resolveCustomerEmail(Long customerId, String fallbackCustomerEmail) {
        String fromAuth = fetchCustomerEmailQuietly(customerId);
        if (fromAuth != null && !fromAuth.isBlank()) {
            return fromAuth;
        }
        if (fallbackCustomerEmail != null && !fallbackCustomerEmail.isBlank()) {
            return fallbackCustomerEmail;
        }
        return "unknown@customer.local";
    }

    private Address upsertFromAuthAddress(Long customerId, AuthAddressResponse authAddress) {
        Address address = addressRepository.findByAuthAddressIdAndCustomerId(authAddress.getId(), customerId)
                .orElseGet(Address::new);
        address.setAuthAddressId(authAddress.getId());
        address.setCustomerId(customerId);
        address.setFullName(authAddress.getFullName());
        address.setPhone(authAddress.getPhone());
        address.setAddressLine1(authAddress.getAddressLine1());
        address.setAddressLine2(authAddress.getAddressLine2());
        address.setCity(authAddress.getCity());
        address.setState(authAddress.getState());
        address.setPincode(authAddress.getPincode());
        address.setDefault(authAddress.isDefault());
        return addressRepository.save(address);
    }

    private OrderResponse buildOrderResponse(Order order) {
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
}
