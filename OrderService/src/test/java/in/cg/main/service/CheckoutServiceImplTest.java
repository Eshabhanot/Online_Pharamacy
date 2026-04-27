package in.cg.main.service;

import in.cg.main.client.CatalogClient;
import in.cg.main.client.AuthClient;
import in.cg.main.dto.CheckoutRequest;
import in.cg.main.dto.MedicineDTO;
import in.cg.main.dto.OrderResponse;
import in.cg.main.dto.PrescriptionDTO;
import in.cg.main.entities.Address;
import in.cg.main.entities.Cart;
import in.cg.main.entities.CartItem;
import in.cg.main.entities.Order;
import in.cg.main.enums.OrderStatus;
import in.cg.main.exception.ResourceNotFoundException;
import in.cg.main.repository.AddressRepository;
import in.cg.main.repository.CartRepository;
import in.cg.main.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private AuthClient authClient;
    @Mock private CatalogClient catalogClient;
    @Mock private NotificationService notificationService;
    @Mock private EmailService emailService;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private Cart cart;
    private Address address;
    private CheckoutRequest request;
    private CartItem cartItem;

    @BeforeEach
    void setup() {
        cart = new Cart();
        cart.setId(1L);
        cart.setCustomerId(11L);

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setMedicineId(100L);
        cartItem.setMedicineName("Paracetamol");
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("100.00"));
        cartItem.setRequiresPrescription(false);
        cart.getItems().add(cartItem);

        address = new Address();
        address.setId(99L);

        request = new CheckoutRequest();
        request.setAddressId(99L);
        request.setDeliverySlot("10AM-12PM");

    }

    @Test
    void startCheckout_cartNotFound_throws() {
        when(cartRepository.findByCustomerId(11L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> checkoutService.startCheckout(11L, null, request));
    }

    @Test
    void startCheckout_cartEmpty_throws() {
        cart.getItems().clear();
        when(cartRepository.findByCustomerId(11L)).thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> checkoutService.startCheckout(11L, null, request));
        assertEquals("Cart is empty", ex.getMessage());
    }

    @Test
    void startCheckout_addressNotFound_throws() {
        when(cartRepository.findByCustomerId(11L)).thenReturn(Optional.of(cart));
        when(addressRepository.findByIdAndCustomerId(99L, 11L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> checkoutService.startCheckout(11L, null, request));
    }

    @Test
    void startCheckout_stockChanged_throws() {
        MedicineDTO lowStock = new MedicineDTO();
        lowStock.setId(100L);
        lowStock.setName("Paracetamol");
        lowStock.setStock(1);
        lowStock.setRequiresPrescription(false);
        when(catalogClient.getMedicineById(100L)).thenReturn(lowStock);

        when(cartRepository.findByCustomerId(11L)).thenReturn(Optional.of(cart));
        when(addressRepository.findByIdAndCustomerId(99L, 11L)).thenReturn(Optional.of(address));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> checkoutService.startCheckout(11L, null, request));
        assertTrue(ex.getMessage().contains("Stock changed"));
    }

    @Test
    void startCheckout_requiresPrescriptionButMissing_throws() {
        cartItem.setRequiresPrescription(true);
        when(cartRepository.findByCustomerId(11L)).thenReturn(Optional.of(cart));
        when(addressRepository.findByIdAndCustomerId(99L, 11L)).thenReturn(Optional.of(address));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> checkoutService.startCheckout(11L, null, request));
        assertTrue(ex.getMessage().contains("required"));
    }

    @Test
    void startCheckout_success_paymentPending_withDeliveryCharge() {
        MedicineDTO medicine = new MedicineDTO();
        medicine.setId(100L);
        medicine.setName("Paracetamol");
        medicine.setStock(10);
        medicine.setRequiresPrescription(false);
        medicine.setPrice(100.0);
        when(catalogClient.getMedicineById(100L)).thenReturn(medicine);

        when(cartRepository.findByCustomerId(11L)).thenReturn(Optional.of(cart));
        when(addressRepository.findByIdAndCustomerId(99L, 11L)).thenReturn(Optional.of(address));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(123L);
            return o;
        });
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        OrderResponse response = checkoutService.startCheckout(11L, null, request);

        assertEquals("Payment Pending", response.getStatus());
        assertEquals(new BigDecimal("200.00"), response.getSubtotal());
        assertEquals(new BigDecimal("49"), response.getDeliveryCharge());
        assertEquals(new BigDecimal("249.00"), response.getTotalAmount());
        assertEquals(0, cart.getItems().size());
        verify(notificationService).sendOrderNotification(eq(123L), eq("unknown@customer.local"), eq("Payment Pending"), contains("placed successfully"));
        verify(emailService).sendOrderEmail(eq("unknown@customer.local"), contains("Order placed"), contains("Payment Pending"));
    }

    @Test
    void startCheckout_pendingPrescription_throwsAndDoesNotPlaceOrder() {
        cartItem.setRequiresPrescription(true);
        cartItem.setQuantity(6);
        request.setPrescriptionId(500L);

        PrescriptionDTO prescription = new PrescriptionDTO();
        prescription.setId(500L);
        prescription.setCustomerId(11L);
        prescription.setStatus("PENDING");
        when(catalogClient.getPrescriptionById(500L)).thenReturn(prescription);

        when(cartRepository.findByCustomerId(11L)).thenReturn(Optional.of(cart));
        when(addressRepository.findByIdAndCustomerId(99L, 11L)).thenReturn(Optional.of(address));
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> checkoutService.startCheckout(11L, null, request));
        assertTrue(ex.getMessage().contains("pending review"));
        verify(orderRepository, never()).save(any(Order.class));
        verify(notificationService, never()).sendOrderNotification(anyLong(), anyString(), anyString(), anyString());
        verify(emailService, never()).sendOrderEmail(anyString(), anyString(), anyString());
    }

    @Test
    void updateStatus_orderNotFound_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> checkoutService.updateStatus(1L, OrderStatus.PAID));
    }

    @Test
    void updateStatus_invalidTransition_throws() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-1");
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> checkoutService.updateStatus(1L, OrderStatus.DELIVERED));
        assertTrue(ex.getMessage().contains("Invalid transition"));
    }

    @Test
    void updateStatus_pendingTarget_throwsIllegalArgument() {
        Order order = new Order();
        order.setId(3L);
        order.setOrderNumber("ORD-3");
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> checkoutService.updateStatus(3L, OrderStatus.PAYMENT_PENDING));
        assertTrue(ex.getMessage().contains("pending status"));
    }

    @Test
    void updateStatus_delivered_setsDeliveredAt_andSendsNotification() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-1");
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = checkoutService.updateStatus(1L, OrderStatus.DELIVERED);

        assertEquals("Delivered", response.getStatus());
        assertNotNull(order.getDeliveredAt());
        verify(notificationService).sendOrderNotification(eq(1L), eq("unknown@customer.local"), eq("Delivered"), contains("status updated"));
        verify(emailService).sendOrderEmail(eq("unknown@customer.local"), contains("Order status"), contains("Delivered"));
    }

    @Test
    void updateStatus_validNonDelivered_keepsDeliveredAtNull() {
        Order order = new Order();
        order.setId(2L);
        order.setOrderNumber("ORD-2");
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = checkoutService.updateStatus(2L, OrderStatus.PAID);

        assertEquals("Paid", response.getStatus());
        assertNull(order.getDeliveredAt());
        verify(notificationService).sendOrderNotification(eq(2L), eq("unknown@customer.local"), eq("Paid"), contains("status updated"));
        verify(emailService).sendOrderEmail(eq("unknown@customer.local"), contains("Order status"), contains("Paid"));
    }

    @Test
    void syncPrescriptionReview_approved_movesPendingOrdersToPaymentPending() {
        Order order = new Order();
        order.setId(20L);
        order.setOrderNumber("ORD-20");
        order.setCustomerId(11L);
        order.setStatus(OrderStatus.PRESCRIPTION_PENDING);
        when(orderRepository.findByPrescriptionIdAndStatus(500L, OrderStatus.PRESCRIPTION_PENDING))
                .thenReturn(List.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        checkoutService.syncPrescriptionReview(500L, true);

        assertEquals(OrderStatus.PAYMENT_PENDING, order.getStatus());
        verify(notificationService).sendOrderNotification(eq(20L), eq("unknown@customer.local"),
                eq("Payment Pending"), contains("Prescription review completed"));
    }

    @Test
    void syncPrescriptionReview_rejected_movesPendingOrdersToRejected() {
        Order order = new Order();
        order.setId(21L);
        order.setOrderNumber("ORD-21");
        order.setCustomerId(11L);
        order.setStatus(OrderStatus.PRESCRIPTION_PENDING);
        when(orderRepository.findByPrescriptionIdAndStatus(501L, OrderStatus.PRESCRIPTION_PENDING))
                .thenReturn(List.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        checkoutService.syncPrescriptionReview(501L, false);

        assertEquals(OrderStatus.PRESCRIPTION_REJECTED, order.getStatus());
        verify(notificationService).sendOrderNotification(eq(21L), eq("unknown@customer.local"),
                eq("Prescription Rejected"), contains("Prescription review completed"));
    }

    private MedicineDTO medicineForPrescriptionOrder() {
        MedicineDTO medicine = new MedicineDTO();
        medicine.setId(100L);
        medicine.setName("Paracetamol");
        medicine.setStock(10);
        medicine.setRequiresPrescription(true);
        medicine.setPrice(100.0);
        return medicine;
    }
}
