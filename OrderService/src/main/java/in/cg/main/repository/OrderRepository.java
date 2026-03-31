package in.cg.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import in.cg.main.entities.Order;
import in.cg.main.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByOrderNumberContaining(String keyword, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByPlacedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Order> findByPlacedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(OrderStatus status);

    long countByStatusIn(List<OrderStatus> statuses);

    long countByCustomerId(Long customerId);

    long countByStatusAndDeliveredAtBetween(OrderStatus status, LocalDateTime start, LocalDateTime end);

    Page<Order> findAllByOrderByPlacedAtDesc(Pageable pageable);
}
