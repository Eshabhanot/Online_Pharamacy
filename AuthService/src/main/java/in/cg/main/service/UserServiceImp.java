package in.cg.main.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.cg.main.dto.AddressResponse;
import in.cg.main.dto.ForgotPasswordOtpRequest;
import in.cg.main.dto.ForgotPasswordOtpResponse;
import in.cg.main.dto.ForgotPasswordRequest;
import in.cg.main.dto.LoginDTO;
import in.cg.main.dto.LoginResponse;
import in.cg.main.dto.RegisterDTO;
import in.cg.main.entities.OtpToken;
import in.cg.main.entities.User;
import in.cg.main.entities.UserAddress;
import in.cg.main.exception.InvalidRoleAssignmentException;
import in.cg.main.exception.ResourceNotFoundException;
import in.cg.main.exception.UserAlreadyExistsException;
import in.cg.main.repository.OtpTokenRepository;
import in.cg.main.repository.UserAddressRepository;
import in.cg.main.repository.UserRepository;

@Service
public class UserServiceImp implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImp.class);
    private static final String GMAIL_DOMAIN = "@gmail.com";
    private static final String RESERVED_ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String PASSWORD_RESET_PURPOSE = "PASSWORD_RESET";
    private static final int OTP_VALIDITY_MINUTES = 10;
    private static final int OTP_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserAddressRepository userAddressRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    public UserServiceImp(UserRepository userRepo,
                          BCryptPasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserAddressRepository userAddressRepository,
                          OtpTokenRepository otpTokenRepository,
                          EmailService emailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userAddressRepository = userAddressRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
    }

    @Override
    public void registerUser(RegisterDTO userDto) throws ResourceNotFoundException {
        String normalizedEmail = normalizeEmail(userDto.getEmail());
        validatePublicRegistration(normalizedEmail, userDto.getRole());

        if (userRepo.findByEmailIgnoreCase(normalizedEmail) != null) {
            throw new UserAlreadyExistsException("User already exists");
        }

        User newUser = new User();
        newUser.setName(userDto.getName());
        newUser.setEmail(normalizedEmail);
        newUser.setMobile(userDto.getMobile());
        newUser.setRole(resolveRole(userDto.getRole()));
        newUser.setStatus(resolveStatus(userDto.getStatus()));
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

        boolean emailSent = emailService.sendRegistrationSuccessEmail(savedUser.getEmail(), savedUser.getName());
        logger.info("Registration email for {} sent: {}", savedUser.getEmail(), emailSent);
    }

    @Override
    public LoginResponse loginUser(LoginDTO login) throws ResourceNotFoundException {
        String normalizedEmail = normalizeEmail(login.getEmail());
        User user = userRepo.findByEmailIgnoreCase(normalizedEmail);

        if (user == null) {
            throw new ResourceNotFoundException("User Not Found");
        }

        validateAdminLoginAccess(user, normalizedEmail);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, login.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        String token = jwtService.generateToken(userDetails.getUsername(), role, user.getId());

        List<AddressResponse> addresses = userAddressRepository.findByUserId(user.getId())
                .stream()
                .map(AddressResponse::from)
                .toList();

        return new LoginResponse(token, userDetails.getUsername(), role, user.getId(), addresses);
    }

    @Override
    @Transactional
    public ForgotPasswordOtpResponse requestForgotPasswordOtp(ForgotPasswordOtpRequest request)
            throws ResourceNotFoundException {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (RESERVED_ADMIN_EMAIL.equals(normalizedEmail)) {
            throw new InvalidRoleAssignmentException("Admin password cannot be changed through forgot password");
        }

        User user = userRepo.findByEmailIgnoreCase(normalizedEmail);
        if (user == null) {
            throw new ResourceNotFoundException("User Not Found");
        }

        invalidateActiveOtps(normalizedEmail);

        LocalDateTime now = LocalDateTime.now();
        String otp = generateOtp();

        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(normalizedEmail);
        otpToken.setPurpose(PASSWORD_RESET_PURPOSE);
        otpToken.setOtpHash(passwordEncoder.encode(otp));
        otpToken.setCreatedAt(now);
        otpToken.setExpiresAt(now.plusMinutes(OTP_VALIDITY_MINUTES));
        otpToken.setUsed(Boolean.FALSE);
        otpTokenRepository.save(otpToken);

        boolean emailSent = emailService.sendPasswordResetOtp(normalizedEmail, otp, OTP_VALIDITY_MINUTES);
        if (!emailSent) {
            logger.info("Password reset OTP for {} is {}. Valid for {} minutes.", normalizedEmail, otp,
                    OTP_VALIDITY_MINUTES);
        }
        logger.info("Password reset OTP for {} generated. Email sent: {}", normalizedEmail, emailSent);

        return new ForgotPasswordOtpResponse(emailSent
                ? "OTP sent to your email address."
                : "OTP generated successfully. Mail delivery is disabled, so check server logs in local development.");
    }

    @Override
    @Transactional
    public void resetForgottenPassword(ForgotPasswordRequest request) throws ResourceNotFoundException {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (RESERVED_ADMIN_EMAIL.equals(normalizedEmail)) {
            throw new InvalidRoleAssignmentException("Admin password cannot be changed through forgot password");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password must match");
        }

        User user = userRepo.findByEmailIgnoreCase(normalizedEmail);
        if (user == null) {
            throw new ResourceNotFoundException("User Not Found");
        }

        OtpToken otpToken = getLatestOtp(normalizedEmail);
        if (otpToken.getConsumedAt() != null) {
            throw new IllegalArgumentException("OTP has already been used");
        }

        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }

        if (!passwordEncoder.matches(request.getOtp(), otpToken.getOtpHash())) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepo.save(user);
        otpToken.setConsumedAt(LocalDateTime.now());
        otpToken.setUsed(Boolean.TRUE);
        otpTokenRepository.save(otpToken);
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (!normalizedEmail.endsWith(GMAIL_DOMAIN)) {
            throw new IllegalArgumentException("Only Gmail addresses are allowed");
        }
        return normalizedEmail;
    }

    private String resolveRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        String normalizedRole = role.trim().toUpperCase();
        if (ADMIN_ROLE.equals(normalizedRole)) {
            throw new InvalidRoleAssignmentException("Admin role cannot be selected during registration");
        }
        return normalizedRole;
    }

    private String resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        return status.trim().toUpperCase();
    }

    private void validatePublicRegistration(String normalizedEmail, String role) {
        if (RESERVED_ADMIN_EMAIL.equals(normalizedEmail)) {
            throw new InvalidRoleAssignmentException("admin@gmail.com is reserved for the system admin");
        }

        if (role != null && ADMIN_ROLE.equals(role.trim().toUpperCase())) {
            throw new InvalidRoleAssignmentException("Admin role cannot be selected during registration");
        }
    }

    private void validateAdminLoginAccess(User user, String normalizedEmail) {
        boolean reservedAdminEmail = RESERVED_ADMIN_EMAIL.equals(normalizedEmail);
        boolean adminRole = ADMIN_ROLE.equalsIgnoreCase(user.getRole());

        if (reservedAdminEmail && !adminRole) {
            throw new BadCredentialsException("Invalid admin credentials");
        }

        if (adminRole && !RESERVED_ADMIN_EMAIL.equalsIgnoreCase(user.getEmail())) {
            throw new BadCredentialsException("Invalid admin credentials");
        }
    }

    private void invalidateActiveOtps(String email) {
        List<OtpToken> activeTokens = otpTokenRepository.findByEmailAndPurposeAndConsumedAtIsNull(email,
                PASSWORD_RESET_PURPOSE);
        if (activeTokens.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (OtpToken token : activeTokens) {
            token.setConsumedAt(now);
        }
        otpTokenRepository.saveAll(activeTokens);
    }

    private OtpToken getLatestOtp(String email) {
        Optional<OtpToken> otpToken = otpTokenRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email,
                PASSWORD_RESET_PURPOSE);
        return otpToken.orElseThrow(() -> new IllegalArgumentException("Please request a new OTP first"));
    }

    private String generateOtp() {
        int bound = (int) Math.pow(10, OTP_LENGTH);
        int otpValue = SECURE_RANDOM.nextInt(bound);
        return String.format("%0" + OTP_LENGTH + "d", otpValue);
    }
}
