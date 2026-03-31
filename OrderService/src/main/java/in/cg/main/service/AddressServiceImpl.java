package in.cg.main.service;

import in.cg.main.dto.AddressRequest;
import in.cg.main.dto.AddressResponse;
import in.cg.main.entities.Address;
import in.cg.main.repository.AddressRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public AddressResponse addAddress(Long customerId, AddressRequest request) {
        boolean makeDefault = Boolean.TRUE.equals(request.getIsDefault());

        if (makeDefault) {
            addressRepository.findByCustomerIdAndIsDefaultTrue(customerId)
                    .ifPresent(existingDefault -> {
                        existingDefault.setDefault(false);
                        addressRepository.save(existingDefault);
                    });
        } else {
            boolean hasDefault = addressRepository.findByCustomerIdAndIsDefaultTrue(customerId).isPresent();
            if (!hasDefault) {
                makeDefault = true;
            }
        }

        Address address = new Address();
        address.setCustomerId(customerId);
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPincode(request.getPincode());
        address.setDefault(makeDefault);

        return AddressResponse.from(addressRepository.save(address));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(Long customerId) {
        return addressRepository.findByCustomerId(customerId, PageRequest.of(0, 100))
                .getContent()
                .stream()
                .map(AddressResponse::from)
                .toList();
    }
}
