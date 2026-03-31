package in.cg.main.service;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import in.cg.main.dto.AddressResponse;
import in.cg.main.dto.LoginDTO;
import in.cg.main.dto.LoginResponse;
import in.cg.main.dto.RegisterDTO;
import in.cg.main.entities.User;
import in.cg.main.entities.UserAddress;
import in.cg.main.exception.ResourceNotFoundException;
import in.cg.main.exception.UserAlreadyExistsException;
import in.cg.main.repository.UserAddressRepository;
import in.cg.main.repository.UserRepository;

@Service
public class UserServiceImp implements UserService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserAddressRepository userAddressRepository;
    private final EmailService emailService;

    public UserServiceImp(UserRepository userRepo,
                          BCryptPasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserAddressRepository userAddressRepository,
                          EmailService emailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userAddressRepository = userAddressRepository;
        this.emailService = emailService;
    }

    @Override
    public void registerUser(RegisterDTO userDto) throws ResourceNotFoundException {
        if (userRepo.findByEmail(userDto.getEmail()) != null) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User newUser = new User();
        newUser.setName(userDto.getName());
        newUser.setEmail(userDto.getEmail());
        newUser.setMobile(userDto.getMobile());
        newUser.setRole(userDto.getRole().toUpperCase());
        newUser.setStatus(userDto.getStatus());
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));

        User savedUser = userRepo.save(newUser);

        if (userDto.getAddressLine1() != null && !userDto.getAddressLine1().isBlank()) {
            UserAddress address = new UserAddress();
            address.setUserId(savedUser.getId());
            address.setFullName(savedUser.getName());
            address.setPhone(savedUser.getMobile());
            address.setAddressLine1(userDto.getAddressLine1());
            address.setAddressLine2(userDto.getAddressLine2());
            address.setCity(userDto.getCity());
            address.setState(userDto.getState());
            address.setPincode(userDto.getPincode());
            address.setDefault(Boolean.TRUE.equals(userDto.getIsDefault()));
            userAddressRepository.save(address);
        }

        emailService.sendRegistrationEmail(savedUser.getEmail(), savedUser.getName());
    }

    @Override
    public LoginResponse loginUser(LoginDTO login) throws ResourceNotFoundException {
        User user = userRepo.findByEmail(login.getEmail());

        if (user == null) {
            throw new ResourceNotFoundException("User Not Found");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        String token = jwtService.generateToken(userDetails.getUsername(), role, user.getId());

        List<AddressResponse> addresses = userAddressRepository.findByUserId(user.getId())
                .stream()
                .map(AddressResponse::from)
                .toList();

        return new LoginResponse(token, userDetails.getUsername(), role, user.getId(), addresses);
    }
}
