package in.cg.main.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.cg.main.client.CatalogClient;
import in.cg.main.entities.Order;
import in.cg.main.entities.OrderItem;
import in.cg.main.entities.Payment;
import in.cg.main.enums.OrderStatus;
import in.cg.main.enums.PaymentStatus;
import in.cg.main.repository.OrderRepository;
import in.cg.main.repository.PaymentRepository;
import in.cg.main.util.OrderStatusFormatter;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final CheckoutServiceImpl checkoutService;
    private final CatalogClient catalogClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderRepository orderRepository,
                              EmailService emailService,
                              CheckoutServiceImpl checkoutService,
                              CatalogClient catalogClient) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.checkoutService = checkoutService;
        this.catalogClient = catalogClient;
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
            
            reduceStockForOrder(order);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
        }

        orderRepository.save(order);
        Payment savedPayment = paymentRepository.save(payment);

        String customerEmail = checkoutService.resolveCustomerEmail(order.getCustomerId(), null);
        
        String paymentStatus = OrderStatusFormatter.toDisplayName(savedPayment.getStatus());
        emailService.sendOrderEmail(
                customerEmail,
                "Payment update - Order " + order.getOrderNumber(),
                "Payment for order " + order.getOrderNumber() + " is " + paymentStatus + "."
        );

        return savedPayment;
    }

    private void reduceStockForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            try {
                catalogClient.reduceStock(item.getMedicineId(), item.getQuantity());
                log.info("Stock reduced for medicine ID {} by quantity {}", 
                        item.getMedicineId(), item.getQuantity());
            } catch (Exception ex) {
                log.error("Failed to reduce stock for medicine ID {}: {}", 
                        item.getMedicineId(), ex.getMessage());
                throw new RuntimeException("Failed to reduce stock for " + item.getMedicineName());
            }
        }
    }
}
