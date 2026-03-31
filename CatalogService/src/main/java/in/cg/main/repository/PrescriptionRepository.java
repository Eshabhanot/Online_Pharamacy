package in.cg.main.repository;


import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.cg.main.entities.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByCustomerId(Long customerId);
    List<Prescription> findByStatus(Prescription.PrescriptionStatus status);
    Optional<Prescription> findByCustomerIdAndOrderId(Long customerId, Long orderId);
}
