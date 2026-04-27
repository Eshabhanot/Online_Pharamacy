package in.cg.main.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.cg.main.entities.Payment;

import java.util.Optional;



@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    
    Optional<Payment> findByOrder_Id(Long orderId);
    Optional<Payment> findByTransactionId(String transactionId);
}