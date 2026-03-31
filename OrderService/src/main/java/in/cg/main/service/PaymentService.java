package in.cg.main.service;

import in.cg.main.entities.Payment;

public interface PaymentService {

    Payment initiatePayment(Long orderId, String method);

    Payment processPayment(String transactionId, boolean success);
}