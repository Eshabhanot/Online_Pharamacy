package in.cg.main.controller;

import in.cg.main.dto.CheckoutRequest;
import in.cg.main.dto.OrderResponse;
import in.cg.main.enums.OrderStatus;
import in.cg.main.security.JwtService;
import in.cg.main.security.RequestCustomerResolver;
import in.cg.main.service.CheckoutService;
import in.cg.main.service.PrescriptionUploadBridgeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private CheckoutService checkoutService;

    @Mock
    private PrescriptionUploadBridgeService prescriptionUploadBridgeService;

    @Mock
    private RequestCustomerResolver requestCustomerResolver;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private CheckoutController checkoutController;

    @Test
    void startCheckout_returnsResponse() {
        CheckoutRequest req = new CheckoutRequest();
        req.setAddressId(1L);

        OrderResponse orderResponse = new OrderResponse.Builder().id(10L).status("Payment Pending").build();
        when(requestCustomerResolver.resolve(1L, "Bearer abc")).thenReturn(1L);
        when(jwtService.extractUsername("abc")).thenReturn("user@example.com");
        when(checkoutService.startCheckout(1L, "user@example.com", req)).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response = checkoutController.startCheckout(1L, "Bearer abc", req);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(10L, response.getBody().getId());
    }

    @Test
    void updateStatus_returnsResponse() {
        OrderResponse orderResponse = new OrderResponse.Builder().id(10L).status("Paid").build();
        when(checkoutService.updateStatus(10L, OrderStatus.PAID)).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response = checkoutController.updateStatus(10L, "PAID");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Paid", response.getBody().getStatus());
    }

    @Test
    void updateStatus_invalidEnum_throws() {
        assertThrows(IllegalArgumentException.class, () -> checkoutController.updateStatus(10L, "UNKNOWN_STATUS"));
    }

    @Test
    void updateStatus_acceptsFriendlyStatusValue() {
        OrderResponse orderResponse = new OrderResponse.Builder().id(10L).status("Out For Delivery").build();
        when(checkoutService.updateStatus(10L, OrderStatus.OUT_FOR_DELIVERY)).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response = checkoutController.updateStatus(10L, "out for delivery");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Out For Delivery", response.getBody().getStatus());
    }
}
