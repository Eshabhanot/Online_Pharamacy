package in.cg.main.controller;

import in.cg.main.dto.AddressRequest;
import in.cg.main.dto.AddressResponse;
import in.cg.main.security.RequestCustomerResolver;
import in.cg.main.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders/addresses")
public class AddressController {

    private final AddressService addressService;
    private final RequestCustomerResolver requestCustomerResolver;

    public AddressController(AddressService addressService,
                             RequestCustomerResolver requestCustomerResolver) {
        this.addressService = addressService;
        this.requestCustomerResolver = requestCustomerResolver;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> addAddress(
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody @Valid AddressRequest request) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        return ResponseEntity.ok(addressService.addAddress(customerId, request));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AddressResponse>> getAddresses(
            @RequestHeader(value = "X-Customer-Id", required = false) Long customerIdHeader,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        Long customerId = requestCustomerResolver.resolve(customerIdHeader, authorizationHeader);
        return ResponseEntity.ok(addressService.getAddresses(customerId));
    }
}
