package in.cg.main.controller;

import in.cg.main.dto.PaymentResponse;
import in.cg.main.entities.Payment;
import in.cg.main.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestParam Long orderId,
            @RequestParam String method) {

        Payment payment = paymentService.initiatePayment(orderId, method);
        return ResponseEntity.ok(toResponse(payment));
    }

    @PostMapping("/callback")
    public ResponseEntity<PaymentResponse> handleCallback(
            @RequestParam String transactionId,
            @RequestParam boolean success) {

        Payment payment = paymentService.processPayment(transactionId, success);
        return ResponseEntity.ok(toResponse(payment));
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        response.setTransactionId(payment.getTransactionId());
        response.setMethod(payment.getMethod());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        response.setInitiatedAt(payment.getInitiatedAt());
        response.setCompletedAt(payment.getCompletedAt());
        return response;
    }
}
