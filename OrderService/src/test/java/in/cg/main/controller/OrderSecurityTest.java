package in.cg.main.controller;

import in.cg.main.client.CatalogClient;
import in.cg.main.security.JwtFilter;
import in.cg.main.security.JwtService;
import in.cg.main.security.RequestCustomerResolver;
import in.cg.main.security.SecurityConfig;
import in.cg.main.service.CartService;
import in.cg.main.service.CheckoutService;
import in.cg.main.service.OrderService;
import in.cg.main.service.PaymentService;
import in.cg.main.service.PrescriptionUploadBridgeService;
import in.cg.main.dto.CartResponse;
import in.cg.main.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CartController.class, OrderController.class, CheckoutController.class, PaymentController.class})
@Import({SecurityConfig.class, JwtFilter.class})
@AutoConfigureMockMvc
class OrderSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CartService cartService;
    @MockBean private OrderService orderService;
    @MockBean private CheckoutService checkoutService;
    @MockBean private PrescriptionUploadBridgeService prescriptionUploadBridgeService;
    @MockBean private PaymentService paymentService;
    @MockBean private CatalogClient catalogClient;
    @MockBean private JwtService jwtService;
    @MockBean private RequestCustomerResolver requestCustomerResolver;

    @BeforeEach
    void setUp() {
        when(requestCustomerResolver.resolve(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // --- Unauthenticated access ---

    @Test
    void cart_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/orders/cart")
                       .header("X-Customer-Id", "1"))
               .andExpect(status().isForbidden());
    }

    @Test
    void orders_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/orders")
                       .header("X-Customer-Id", "1"))
               .andExpect(status().isForbidden());
    }

    // --- USER role access ---

    @Test
    @WithMockUser(roles = "USER")
    void cart_withUserRole_returns200() throws Exception {
        CartResponse cartResponse = new CartResponse.Builder()
                .cartId(1L)
                .items(Collections.emptyList())
                .subtotal(BigDecimal.ZERO)
                .totalItems(0)
                .build();
        when(cartService.getCart(1L)).thenReturn(cartResponse);

        mockMvc.perform(get("/api/orders/cart")
                       .header("X-Customer-Id", "1"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void orders_withUserRole_returns200() throws Exception {
        when(orderService.getCustomerOrders(eq(1L), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/orders")
                       .header("X-Customer-Id", "1"))
               .andExpect(status().isOk());
    }

    // --- ADMIN role: should NOT access cart (USER-only) ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void cart_withAdminRole_isDenied() throws Exception {
        mockMvc.perform(get("/api/orders/cart")
                       .header("X-Customer-Id", "1"))
               .andExpect(status().isForbidden());
    }

    // --- ADMIN role: should access orders ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void orders_withAdminRole_returns200() throws Exception {
        when(orderService.getCustomerOrders(eq(1L), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/orders")
                       .header("X-Customer-Id", "1"))
               .andExpect(status().isOk());
    }
}
