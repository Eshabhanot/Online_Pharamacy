package in.cg.main.service;

import in.cg.main.entities.Order;
import in.cg.main.entities.Payment;
import in.cg.main.enums.OrderStatus;
import in.cg.main.enums.PaymentStatus;
import in.cg.main.repository.OrderRepository;
import in.cg.main.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private CheckoutServiceImpl checkoutService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order order;

    @BeforeEach
    void setup() {
        order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setOrderNumber("ORD-1");
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setTotalAmount(new BigDecimal("249.00"));
    }

    @Test
    void initiatePayment_orderNotFound_throws() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.initiatePayment(1L, "UPI"));
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void initiatePayment_invalidOrderState_throws() {
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.initiatePayment(1L, "UPI"));
        assertEquals("Order is not in PAYMENT_PENDING state", ex.getMessage());
    }

    @Test
    void initiatePayment_success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.initiatePayment(1L, "UPI");

        assertEquals("UPI", payment.getMethod());
        assertEquals(new BigDecimal("249.00"), payment.getAmount());
        assertEquals(PaymentStatus.INITIATED, payment.getStatus());
        assertTrue(payment.getTransactionId().startsWith("TXN-"));
    }

    @Test
    void processPayment_transactionNotFound_throws() {
        when(paymentRepository.findByTransactionId("TXN-1")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.processPayment("TXN-1", true));
        assertEquals("Transaction not found", ex.getMessage());
    }

    @Test
    void processPayment_success_updatesPaymentAndOrder() {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setTransactionId("TXN-1");
        payment.setStatus(PaymentStatus.INITIATED);

        when(paymentRepository.findByTransactionId("TXN-1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(checkoutService.resolveCustomerEmail(1L, null)).thenReturn("test@test.com");

        Payment result = paymentService.processPayment("TXN-1", true);

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertNotNull(result.getCompletedAt());
        verify(emailService).sendOrderEmail(eq("test@test.com"), contains("Payment update"), contains("Success"));
    }

    @Test
    void processPayment_failure_updatesPaymentAndOrder() {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setTransactionId("TXN-2");
        payment.setStatus(PaymentStatus.INITIATED);

        when(paymentRepository.findByTransactionId("TXN-2")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(checkoutService.resolveCustomerEmail(1L, null)).thenReturn("test@test.com");

        Payment result = paymentService.processPayment("TXN-2", false);

        assertEquals(PaymentStatus.FAILED, result.getStatus());
        assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
        verify(emailService).sendOrderEmail(eq("test@test.com"), contains("Payment update"), contains("Failed"));
    }
}
