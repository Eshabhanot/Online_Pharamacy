package in.cg.main.controller;

import in.cg.main.dto.PaymentResponse;
import in.cg.main.entities.Payment;
import in.cg.main.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void initiatePayment_returnsPayment() {
        Payment payment = new Payment();
        payment.setTransactionId("TXN-1");
        when(paymentService.initiatePayment(1L, "UPI")).thenReturn(payment);

        ResponseEntity<PaymentResponse> response = paymentController.initiatePayment(1L, "UPI");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("TXN-1", response.getBody().getTransactionId());
    }

    @Test
    void handleCallback_returnsPayment() {
        Payment payment = new Payment();
        payment.setTransactionId("TXN-2");
        when(paymentService.processPayment("TXN-2", true)).thenReturn(payment);

        ResponseEntity<PaymentResponse> response = paymentController.handleCallback("TXN-2", true);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("TXN-2", response.getBody().getTransactionId());
    }
}
