package in.cg.main.service;

import in.cg.main.dto.ForgotPasswordOtpRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImpTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImp userService;

    private RegisterDTO registerDTO;
    private LoginDTO loginDTO;
    private User user;

    @BeforeEach
    void setUp() {
        registerDTO = new RegisterDTO();
        registerDTO.setName("Test User");
        registerDTO.setEmail("testuser@gmail.com");
        registerDTO.setMobile("1234567890");
        registerDTO.setPassword("password");
        registerDTO.setRole("USER");
        registerDTO.setStatus("ACTIVE");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("testuser@gmail.com");
        user.setPassword("encodedPassword");
        user.setRole("USER");

        loginDTO = new LoginDTO();
        loginDTO.setEmail("testuser@gmail.com");
        loginDTO.setPassword("password");
    }

    @Test
    void testRegisterUser_Success() throws ResourceNotFoundException {
        when(userRepo.findByEmailIgnoreCase(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(user);
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailService.sendRegistrationSuccessEmail("testuser@gmail.com", "Test User")).thenReturn(true);
        registerDTO.setAddressLine1("Line 1");
        registerDTO.setCity("City");
        registerDTO.setState("State");
        registerDTO.setPincode("123456");
        
        userService.registerUser(registerDTO);
        
        verify(userRepo, times(1)).save(any(User.class));
        verify(userAddressRepository, times(1)).save(any(UserAddress.class));
        verify(emailService).sendRegistrationSuccessEmail("testuser@gmail.com", "Test User");
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        when(userRepo.findByEmailIgnoreCase(anyString())).thenReturn(user);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(registerDTO));
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void testLoginUser_Success() throws ResourceNotFoundException {
        when(userRepo.findByEmailIgnoreCase(anyString())).thenReturn(user);

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        "testuser@gmail.com",
                        "encodedPassword",
                        Collections.singleton(authority));
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken("testuser@gmail.com", "ROLE_USER", 1L)).thenReturn("dummy.jwt.token");
        when(userAddressRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        LoginResponse response = userService.loginUser(loginDTO);

        assertNotNull(response);
        assertEquals("dummy.jwt.token", response.getToken());
        assertEquals("ROLE_USER", response.getRole());
        assertEquals("testuser@gmail.com", response.getEmail());
        assertEquals(1L, response.getUserId());
        assertNotNull(response.getAddresses());
    }

    @Test
    void testLoginUser_UserNotFound() {
        when(userRepo.findByEmailIgnoreCase(anyString())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> userService.loginUser(loginDTO));
    }

    @Test
    void testRegisterUser_NonGmailRejected() {
        registerDTO.setEmail("testuser@yahoo.com");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(registerDTO));

        assertEquals("Only Gmail addresses are allowed", ex.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_BlankRoleAndStatus_Defaulted() throws ResourceNotFoundException {
        registerDTO.setRole(" ");
        registerDTO.setStatus(null);
        when(userRepo.findByEmailIgnoreCase(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailService.sendRegistrationSuccessEmail("testuser@gmail.com", "Test User")).thenReturn(true);

        userService.registerUser(registerDTO);

        verify(userRepo).save(argThat(saved ->
                "USER".equals(saved.getRole()) && "ACTIVE".equals(saved.getStatus())));
    }

    @Test
    void testRegisterUser_AdminRoleRejected() {
        registerDTO.setRole("ADMIN");

        InvalidRoleAssignmentException ex = assertThrows(InvalidRoleAssignmentException.class,
                () -> userService.registerUser(registerDTO));

        assertEquals("Admin role cannot be selected during registration", ex.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_AdminEmailRejected() {
        registerDTO.setEmail("admin@gmail.com");

        InvalidRoleAssignmentException ex = assertThrows(InvalidRoleAssignmentException.class,
                () -> userService.registerUser(registerDTO));

        assertEquals("admin@gmail.com is reserved for the system admin", ex.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void testLoginUser_AdminRoleWithDifferentEmailRejected() {
        user.setEmail("otheradmin@gmail.com");
        user.setRole("ADMIN");
        loginDTO.setEmail("otheradmin@gmail.com");
        when(userRepo.findByEmailIgnoreCase(anyString())).thenReturn(user);

        assertThrows(BadCredentialsException.class, () -> userService.loginUser(loginDTO));
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testResetForgottenPassword_Success() throws ResourceNotFoundException {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("testuser@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");
        OtpToken otpToken = new OtpToken();
        otpToken.setOtpHash("encodedOtp");
        otpToken.setCreatedAt(java.time.LocalDateTime.now());
        otpToken.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));

        when(userRepo.findByEmailIgnoreCase("testuser@gmail.com")).thenReturn(user);
        when(otpTokenRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc("testuser@gmail.com", "PASSWORD_RESET"))
                .thenReturn(Optional.of(otpToken));
        when(passwordEncoder.matches("123456", "encodedOtp")).thenReturn(true);
        when(passwordEncoder.encode("newpassword123")).thenReturn("encodedNewPassword");

        userService.resetForgottenPassword(request);

        verify(userRepo).save(argThat(saved -> "encodedNewPassword".equals(saved.getPassword())));
        verify(otpTokenRepository).save(argThat(saved -> saved.getConsumedAt() != null && Boolean.TRUE.equals(saved.getUsed())));
    }

    @Test
    void testResetForgottenPassword_AdminRejected() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("admin@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");

        InvalidRoleAssignmentException ex = assertThrows(InvalidRoleAssignmentException.class,
                () -> userService.resetForgottenPassword(request));

        assertEquals("Admin password cannot be changed through forgot password", ex.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void testRequestForgotPasswordOtp_Success() throws ResourceNotFoundException {
        ForgotPasswordOtpRequest request = new ForgotPasswordOtpRequest();
        request.setEmail("testuser@gmail.com");

        when(userRepo.findByEmailIgnoreCase("testuser@gmail.com")).thenReturn(user);
        when(otpTokenRepository.findByEmailAndPurposeAndConsumedAtIsNull("testuser@gmail.com", "PASSWORD_RESET"))
                .thenReturn(Collections.emptyList());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedOtp");
        when(emailService.sendPasswordResetOtp(eq("testuser@gmail.com"), anyString(), eq(10))).thenReturn(true);

        userService.requestForgotPasswordOtp(request);

        verify(otpTokenRepository).save(argThat(saved ->
                "testuser@gmail.com".equals(saved.getEmail())
                        && "PASSWORD_RESET".equals(saved.getPurpose())
                        && saved.getConsumedAt() == null
                        && Boolean.FALSE.equals(saved.getUsed())));
        verify(emailService).sendPasswordResetOtp(eq("testuser@gmail.com"), anyString(), eq(10));
    }

    @Test
    void testResetForgottenPassword_InvalidOtpRejected() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("testuser@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("newpassword123");
        request.setConfirmPassword("newpassword123");
        OtpToken otpToken = new OtpToken();
        otpToken.setOtpHash("encodedOtp");
        otpToken.setCreatedAt(java.time.LocalDateTime.now());
        otpToken.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));

        when(userRepo.findByEmailIgnoreCase("testuser@gmail.com")).thenReturn(user);
        when(otpTokenRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc("testuser@gmail.com", "PASSWORD_RESET"))
                .thenReturn(Optional.of(otpToken));
        when(passwordEncoder.matches("123456", "encodedOtp")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.resetForgottenPassword(request));

        assertEquals("Invalid OTP", ex.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }
}
