package in.cg.main.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.cg.main.entities.Order;
import in.cg.main.entities.Payment;
import in.cg.main.enums.OrderStatus;
import in.cg.main.enums.PaymentStatus;
import in.cg.main.repository.OrderRepository;
import in.cg.main.repository.PaymentRepository;
import in.cg.main.util.OrderStatusFormatter;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final CheckoutServiceImpl checkoutService;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderRepository orderRepository,
                              EmailService emailService,
                              CheckoutServiceImpl checkoutService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.checkoutService = checkoutService;
    }

    @Override
    public Payment initiatePayment(Long orderId, String method) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new RuntimeException("Order is not in PAYMENT_PENDING state");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(method);
        payment.setAmount(order.getTotalAmount());
        payment.setTransactionId(
                "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
        );
        payment.setStatus(PaymentStatus.INITIATED);

        return paymentRepository.save(payment);
    }

    @Override
    public Payment processPayment(String transactionId, boolean success) {

        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        Order order = payment.getOrder();

        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.now());
            order.setStatus(OrderStatus.PAID);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        orderRepository.save(order);
        Payment savedPayment = paymentRepository.save(payment);

        String customerEmail = checkoutService.resolveCustomerEmail(order.getCustomerId(), null);
        emailService.sendOrderEmail(
                customerEmail,
                "Payment update for order " + order.getOrderNumber(),
                "Payment for order " + order.getOrderNumber() + " is " + OrderStatusFormatter.toDisplayName(savedPayment.getStatus())
                        + " and order status is " + OrderStatusFormatter.toDisplayName(order.getStatus()) + "."
        );

        return savedPayment;
    }
}
