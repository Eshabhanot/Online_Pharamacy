package in.cg.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import in.cg.main.entities.Address;

import java.util.Optional;



public interface AddressRepository extends JpaRepository<Address, Long> {

    Page<Address> findByCustomerId(Long customerId, Pageable pageable);

    Optional<Address> findByCustomerIdAndIsDefaultTrue(Long customerId);

    Optional<Address> findByIdAndCustomerId(Long id, Long customerId);

    Optional<Address> findByAuthAddressIdAndCustomerId(Long authAddressId, Long customerId);
}
