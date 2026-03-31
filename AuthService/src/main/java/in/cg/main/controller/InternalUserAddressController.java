package in.cg.main.controller;

import in.cg.main.dto.AddressResponse;
import in.cg.main.dto.InternalUserResponse;
import in.cg.main.entities.User;
import in.cg.main.exception.ResourceNotFoundException;
import in.cg.main.repository.UserRepository;
import in.cg.main.repository.UserAddressRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal/users")
public class InternalUserAddressController {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    public InternalUserAddressController(UserAddressRepository userAddressRepository,
                                         UserRepository userRepository) {
        this.userAddressRepository = userAddressRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}/addresses")
    public List<AddressResponse> getAddresses(@PathVariable Long userId) {
        return userAddressRepository.findByUserId(userId)
                .stream()
                .map(AddressResponse::from)
                .toList();
    }

    @GetMapping("/{userId}")
    public InternalUserResponse getUserById(@PathVariable Long userId) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return InternalUserResponse.from(user);
    }
}
