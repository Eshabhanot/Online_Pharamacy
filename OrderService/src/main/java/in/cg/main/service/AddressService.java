package in.cg.main.service;

import in.cg.main.dto.AddressRequest;
import in.cg.main.dto.AddressResponse;

import java.util.List;

public interface AddressService {
    AddressResponse addAddress(Long customerId, AddressRequest request);
    List<AddressResponse> getAddresses(Long customerId);
}
