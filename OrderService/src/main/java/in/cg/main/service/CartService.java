package in.cg.main.service;

import java.math.BigDecimal;

import in.cg.main.dto.CartItemRequest;
import in.cg.main.dto.CartResponse;
import in.cg.main.entities.Cart;

public interface CartService {

    Cart getOrCreateCart(Long customerId);

    // ✅ Get Cart
    CartResponse getCart(Long customerId);

    // ✅ Add Item (FIXED)
    CartResponse addItem(Long customerId,
                         CartItemRequest req,
                         String medicineName,
                         BigDecimal unitPrice,
                         boolean requiresPrescription,
                         int availableStock); // ✅ fixed spelling

    // ✅ Update Item
    CartResponse updateItem(Long customerId,
                            Long cartItemId,
                            int quantity);

    // ✅ Remove Item (MISSING - ADDED)
    CartResponse removeItem(Long customerId,
                            Long cartItemId);

    // ✅ Clear Cart
    void clearCart(Long customerId);
}