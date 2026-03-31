package in.cg.main.controller;

import in.cg.main.client.CatalogClient;
import in.cg.main.dto.CartItemRequest;
import in.cg.main.dto.CartResponse;
import in.cg.main.dto.MedicineDTO;
import in.cg.main.security.RequestCustomerResolver;
import in.cg.main.service.CartService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock private CartService cartService;
    @Mock private CatalogClient catalogClient;
    @Mock private RequestCustomerResolver requestCustomerResolver;

    @InjectMocks
    private CartController cartController;

    @Test
    void getCart_returnsResponse() {
        CartResponse cartResponse = new CartResponse.Builder().cartId(1L).items(Collections.emptyList()).subtotal(BigDecimal.ZERO).totalItems(0).build();
        when(requestCustomerResolver.resolve(1L, "Bearer abc")).thenReturn(1L);
        when(cartService.getCart(1L)).thenReturn(cartResponse);

        ResponseEntity<CartResponse> response = cartController.getCart(1L, "Bearer abc");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, response.getBody().getCartId());
    }

    @Test
    void addItem_usesCatalogData() {
        CartItemRequest req = new CartItemRequest();
        req.setMedicineId(100L);
        req.setQuantity(2);

        MedicineDTO medicine = new MedicineDTO();
        medicine.setName("Paracetamol");
        medicine.setPrice(50.0);
        medicine.setRequiresPrescription(false);
        medicine.setStock(20);

        CartResponse cartResponse = new CartResponse.Builder().cartId(1L).items(Collections.emptyList()).subtotal(BigDecimal.ZERO).totalItems(0).build();

        when(requestCustomerResolver.resolve(1L, "Bearer abc")).thenReturn(1L);
        when(catalogClient.getMedicineById(100L)).thenReturn(medicine);
        when(cartService.addItem(eq(1L), eq(req), eq("Paracetamol"), eq(BigDecimal.valueOf(50.0)), eq(false), eq(20)))
                .thenReturn(cartResponse);

        ResponseEntity<CartResponse> response = cartController.addItem(1L, "Bearer abc", req);

        assertEquals(200, response.getStatusCode().value());
        verify(cartService).addItem(eq(1L), eq(req), eq("Paracetamol"), eq(BigDecimal.valueOf(50.0)), eq(false), eq(20));
    }

    @Test
    void updateItem_returnsResponse() {
        CartResponse cartResponse = new CartResponse.Builder().cartId(1L).items(Collections.emptyList()).subtotal(BigDecimal.ZERO).totalItems(0).build();
        when(requestCustomerResolver.resolve(1L, "Bearer abc")).thenReturn(1L);
        when(cartService.updateItem(1L, 10L, 3)).thenReturn(cartResponse);

        ResponseEntity<CartResponse> response = cartController.updateItem(1L, "Bearer abc", 10L, 3);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void removeItem_returnsResponse() {
        CartResponse cartResponse = new CartResponse.Builder().cartId(1L).items(Collections.emptyList()).subtotal(BigDecimal.ZERO).totalItems(0).build();
        when(requestCustomerResolver.resolve(1L, "Bearer abc")).thenReturn(1L);
        when(cartService.removeItem(1L, 10L)).thenReturn(cartResponse);

        ResponseEntity<CartResponse> response = cartController.removeItem(1L, "Bearer abc", 10L);

        assertEquals(200, response.getStatusCode().value());
    }
}
