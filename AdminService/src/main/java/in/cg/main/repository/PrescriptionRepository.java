package in.cg.main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.cg.main.entities.Prescription;
import in.cg.main.enums.PrescriptionStatus;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByStatus(PrescriptionStatus status);
    List<Prescription> findByCustomerId(Long customerId);
    long countByStatus(PrescriptionStatus status);
}
