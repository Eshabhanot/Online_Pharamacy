package in.cg.main.service;

import in.cg.main.dto.CartItemRequest;
import in.cg.main.dto.CartResponse;
import in.cg.main.entities.Cart;
import in.cg.main.entities.CartItem;
import in.cg.main.exception.ResourceNotFoundException;
import in.cg.main.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(1L);
        cart.setCustomerId(1L);
        cart.setItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setId(10L);
        cartItem.setCart(cart);
        cartItem.setMedicineId(100L);
        cartItem.setMedicineName("Aspirin");
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(new BigDecimal("15.00"));
        cartItem.setRequiresPrescription(false);

        cart.getItems().add(cartItem);
    }

    @Test
    void testGetOrCreateCart_Exists() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getOrCreateCart(1L);

        assertNotNull(result);
        assertEquals(1L, result.getCustomerId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testGetOrCreateCart_New() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.getOrCreateCart(1L);

        assertNotNull(result);
        assertEquals(1L, result.getCustomerId());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testGetCart() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCart(1L);

        assertNotNull(response);
        assertEquals(1L, response.getCartId());
        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getTotalItems());
        assertEquals(new BigDecimal("30.00"), response.getSubtotal());
    }

    @Test
    void testAddItem_NewItem() {
        cart.getItems().clear(); // Empty cart
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartItemRequest req = new CartItemRequest();
        req.setMedicineId(200L);
        req.setQuantity(3);

        CartResponse response = cartService.addItem(1L, req, "Paracetamol", new BigDecimal("10.00"), false, 100);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(3, response.getTotalItems());
        assertEquals(new BigDecimal("30.00"), response.getSubtotal());
    }

    @Test
    void testAddItem_ExistingItem_QuantityUpdated() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartItemRequest req = new CartItemRequest();
        req.setMedicineId(100L);
        req.setQuantity(2);

        CartResponse response = cartService.addItem(1L, req, "Aspirin", new BigDecimal("15.00"), false, 10);

        assertEquals(4, response.getTotalItems());
        assertEquals(new BigDecimal("60.00"), response.getSubtotal());
    }

    @Test
    void testAddItem_InsufficientStock_Throws() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        CartItemRequest req = new CartItemRequest();
        req.setMedicineId(999L);
        req.setQuantity(101);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addItem(1L, req, "X", BigDecimal.ONE, false, 100));
        assertEquals("Insufficient stock available", ex.getMessage());
    }

    @Test
    void testAddItem_ExceedsAvailableStock_Throws() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        CartItemRequest req = new CartItemRequest();
        req.setMedicineId(100L);
        req.setQuantity(10);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addItem(1L, req, "Aspirin", new BigDecimal("15.00"), false, 11));
        assertEquals("Exceeds available stock", ex.getMessage());
    }

    @Test
    void testUpdateItem_Success() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.updateItem(1L, 10L, 5);

        assertNotNull(response);
        assertEquals(5, response.getTotalItems());
        assertEquals(new BigDecimal("75.00"), response.getSubtotal());
    }

    @Test
    void testUpdateItem_NotFound() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        assertThrows(ResourceNotFoundException.class, () -> cartService.updateItem(1L, 99L, 5));
    }

    @Test
    void testUpdateItem_QuantityZero_RemovesItem() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.updateItem(1L, 10L, 0);

        assertEquals(0, response.getItems().size());
        assertEquals(BigDecimal.ZERO, response.getSubtotal());
    }

    @Test
    void testRemoveItem_Success() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.removeItem(1L, 10L);

        assertNotNull(response);
        assertEquals(0, response.getItems().size());
        assertEquals(0, response.getTotalItems());
        assertEquals(BigDecimal.ZERO, response.getSubtotal());
    }

    @Test
    void testRemoveItem_NotFound() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItem(1L, 999L));
    }

    @Test
    void testClearCart() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.clearCart(1L);

        assertEquals(0, cart.getItems().size());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void testClearCart_NoCart_NoSave() {
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.empty());

        cartService.clearCart(1L);

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testBuildCartResponse_PrescriptionUploadedTrue() {
        cartItem.setPrescriptionId(555L);
        when(cartRepository.findByCustomerId(1L)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCart(1L);

        assertTrue(response.getItems().get(0).isPrescriptionUploaded());
    }
}
