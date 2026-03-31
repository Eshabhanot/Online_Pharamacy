package in.cg.main.controller;

import in.cg.main.dto.CheckoutRequest;
import in.cg.main.dto.OrderResponse;
import in.cg.main.dto.PrescriptionUploadResponse;
import in.cg.main.security.JwtService;
import in.cg.main.security.RequestCustomerResolver;
import in.cg.main.service.CheckoutService;
import in.cg.main.service.PrescriptionUploadBridgeService;
import in.cg.main.util.OrderStatusFormatter;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/orders/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final PrescriptionUploadBridgeService prescriptionUploadBridgeService;
    private final RequestCustomerResolver requestCustomerResolver;
    private final JwtService jwtService;

    public CheckoutController(CheckoutService checkoutService,
                              PrescriptionUploadBridgeService prescriptionUploadBridgeService,
                              RequestCustomerResolver requestCustomerResolver,
                              JwtService jwtService) {
        this.checkoutService = checkoutService;
        this.prescriptionUploadBridgeService = prescriptionUploadBridgeService;
        this.requestCustomerResolver = requestCustomerResolver;
        this.jwtService = jwtService;
    }

    @PostMapping("/start")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> startCheckout(
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody @Valid CheckoutRequest req) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        String fallbackCustomerEmail = extractEmailFromToken(authorizationHeader);
        return ResponseEntity.ok(checkoutService.startCheckout(customerId, fallbackCustomerEmail, req));
    }

    @PostMapping(value = "/start-with-prescription", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> startCheckoutWithPrescription(
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam Long addressId,
            @RequestParam(required = false) String deliverySlot,
            @RequestPart("file") MultipartFile file) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        PrescriptionUploadResponse upload = prescriptionUploadBridgeService.uploadToCatalog(customerId, file, authorizationHeader);

        CheckoutRequest req = new CheckoutRequest();
        req.setAddressId(addressId);
        req.setDeliverySlot(deliverySlot);
        req.setPrescriptionId(upload.getPrescriptionId());

        String fallbackCustomerEmail = extractEmailFromToken(authorizationHeader);
        return ResponseEntity.ok(checkoutService.startCheckout(customerId, fallbackCustomerEmail, req));
    }

    @PutMapping("/status/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {

        return ResponseEntity.ok(
                checkoutService.updateStatus(orderId, OrderStatusFormatter.parseOrderStatus(status))
        );
    }

    private String extractEmailFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            return jwtService.extractUsername(authorizationHeader.substring(7));
        } catch (Exception ex) {
            return null;
        }
    }
}
