package in.cg.main.controller;

import in.cg.main.client.CatalogClient;
import in.cg.main.dto.CartItemRequest;
import in.cg.main.dto.CartResponse;
import in.cg.main.dto.MedicineDTO;
import in.cg.main.security.RequestCustomerResolver;
import in.cg.main.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders/cart")
public class CartController {

    private final CartService cartService;
    private final CatalogClient catalogClient;
    private final RequestCustomerResolver requestCustomerResolver;

    public CartController(CartService cartService,
                          CatalogClient catalogClient,
                          RequestCustomerResolver requestCustomerResolver) {
        this.cartService = cartService;
        this.catalogClient = catalogClient;
        this.requestCustomerResolver = requestCustomerResolver;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> addItem(
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody @Valid CartItemRequest req) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        MedicineDTO medicine = catalogClient.getMedicineById(req.getMedicineId());

        return ResponseEntity.ok(
                cartService.addItem(
                        customerId,
                        req,
                        medicine.getName(),
                        BigDecimal.valueOf(medicine.getPrice()),
                        medicine.isRequiresPrescription(),
                        medicine.getStock()
                )
        );
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> updateItem(
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long itemId,
            @RequestParam int quantity) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        return ResponseEntity.ok(cartService.updateItem(customerId, itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CartResponse> removeItem(
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable Long itemId) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        return ResponseEntity.ok(cartService.removeItem(customerId, itemId));
    }
}
