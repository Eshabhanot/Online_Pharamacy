package in.cg.main.config;

import in.cg.main.entities.Address;
import in.cg.main.entities.Cart;
import in.cg.main.entities.CartItem;
import in.cg.main.entities.Order;
import in.cg.main.entities.OrderItem;
import in.cg.main.entities.Payment;
import in.cg.main.enums.OrderStatus;
import in.cg.main.enums.PaymentStatus;
import in.cg.main.repository.AddressRepository;
import in.cg.main.repository.CartRepository;
import in.cg.main.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class SampleDataConfig {

    @Bean
    CommandLineRunner seedOrderSampleData(AddressRepository addressRepository,
                                          CartRepository cartRepository,
                                          OrderRepository orderRepository) {
        return args -> {
            if (addressRepository.count() == 0) {
                addressRepository.save(buildAddress());
            }
            if (orderRepository.count() == 0) {
                Address address = addressRepository.findByCustomerIdAndIsDefaultTrue(1L)
                        .orElseGet(() -> addressRepository.save(buildAddress()));
                orderRepository.save(buildOrder("ORD-SAMPLE-0001", address, OrderStatus.PACKED, null));
                orderRepository.save(buildOrder("ORD-SAMPLE-0002", address, OrderStatus.OUT_FOR_DELIVERY, null));
                orderRepository.save(buildOrder("ORD-SAMPLE-0003", address, OrderStatus.DELIVERED,
                        LocalDateTime.now().minusHours(2)));
            }
            if (cartRepository.findByCustomerId(1L).isEmpty()) {
                cartRepository.save(buildCart());
            }
        };
    }

    private Address buildAddress() {
        Address address = new Address();
        address.setCustomerId(1L);
        address.setAuthAddressId(1L);
        address.setFullName("Demo User");
        address.setPhone("9876543210");
        address.setAddressLine1("221 Sample Street");
        address.setAddressLine2("Near City Center");
        address.setCity("Bengaluru");
        address.setState("Karnataka");
        address.setPincode("560001");
        address.setDefault(true);
        return address;
    }

    private Order buildOrder(String orderNumber, Address address, OrderStatus status, LocalDateTime deliveredAt) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setCustomerId(1L);
        order.setAddress(address);
        order.setDeliverySlot("10:00 AM - 12:00 PM");
        order.setSubtotal(new BigDecimal("180.00"));
        order.setDeliveryCharge(new BigDecimal("49.00"));
        order.setTotalAmount(new BigDecimal("229.00"));
        order.setStatus(status);
        order.setDeliveredAt(deliveredAt);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setMedicineId(1L);
        item.setMedicineName("Paracetamol 650");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("90.00"));
        item.setTotalPrice(new BigDecimal("180.00"));
        order.getItems().add(item);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod("COD");
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.SUCCESS);
        order.setPayment(payment);

        return order;
    }

    private Cart buildCart() {
        Cart cart = new Cart();
        cart.setCustomerId(1L);

        CartItem nonPrescription = new CartItem();
        nonPrescription.setCart(cart);
        nonPrescription.setMedicineId(1L);
        nonPrescription.setMedicineName("Paracetamol 650");
        nonPrescription.setQuantity(2);
        nonPrescription.setUnitPrice(new BigDecimal("90.00"));
        nonPrescription.setRequiresPrescription(false);

        CartItem prescription = new CartItem();
        prescription.setCart(cart);
        prescription.setMedicineId(2L);
        prescription.setMedicineName("Amoxicillin 500");
        prescription.setQuantity(1);
        prescription.setUnitPrice(new BigDecimal("145.00"));
        prescription.setRequiresPrescription(true);

        cart.getItems().add(nonPrescription);
        cart.getItems().add(prescription);
        return cart;
    }
}
