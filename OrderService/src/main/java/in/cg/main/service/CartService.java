package in.cg.main.service;

import java.math.BigDecimal;

import in.cg.main.dto.CartItemRequest;
import in.cg.main.dto.CartResponse;
import in.cg.main.entities.Cart;

public interface CartService {

    Cart getOrCreateCart(Long customerId);
    CartResponse getCart(Long customerId);
    CartResponse addItem(Long customerId,
                         CartItemRequest req,
                         String medicineName,
                         BigDecimal unitPrice,
                         boolean requiresPrescription,
                         int availableStock); 

    CartResponse updateItem(Long customerId,
                            Long cartItemId,
                            int quantity);

    CartResponse removeItem(Long customerId,
                            Long cartItemId);


    void clearCart(Long customerId);
}