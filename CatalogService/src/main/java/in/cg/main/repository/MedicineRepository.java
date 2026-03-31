package in.cg.main.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import in.cg.main.entities.Medicine;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine,Long> {

	Page<Medicine> findByNameContainingIgnoreCaseAndCategoryId(String name,Long id,Pageable pageable);
	Page<Medicine> findByNameContainingIgnoreCase(String name,Pageable pageable);
Page<Medicine> findByCategoryId(Long id,Pageable pageable);
Page<Medicine> findByRequiresPrescriptionTrue(Pageable pageable);
Page<Medicine> findByStockLessThan(int threshold,Pageable pageable);
Page<Medicine> findByExpiryDateBefore(LocalDate date, Pageable pageable);
}
