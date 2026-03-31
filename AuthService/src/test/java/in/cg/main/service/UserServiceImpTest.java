package in.cg.main.service;

import in.cg.main.dto.LoginDTO;
import in.cg.main.dto.LoginResponse;
import in.cg.main.dto.RegisterDTO;
import in.cg.main.entities.User;
import in.cg.main.entities.UserAddress;
import in.cg.main.exception.ResourceNotFoundException;
import in.cg.main.exception.UserAlreadyExistsException;
import in.cg.main.repository.UserAddressRepository;
import in.cg.main.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        registerDTO.setEmail("test@test.com");
        registerDTO.setMobile("1234567890");
        registerDTO.setPassword("password");
        registerDTO.setRole("USER");
        registerDTO.setStatus("ACTIVE");

        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@test.com");
        user.setPassword("encodedPassword");
        user.setRole("USER");

        loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("password");
    }

    @Test
    void testRegisterUser_Success() throws ResourceNotFoundException {
        when(userRepo.findByEmail(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(user);
        when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> invocation.getArgument(0));
        registerDTO.setAddressLine1("Line 1");
        registerDTO.setCity("City");
        registerDTO.setState("State");
        registerDTO.setPincode("123456");
        
        userService.registerUser(registerDTO);
        
        verify(userRepo, times(1)).save(any(User.class));
        verify(userAddressRepository, times(1)).save(any(UserAddress.class));
        verify(emailService).sendRegistrationEmail("test@test.com", "Test User");
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        when(userRepo.findByEmail(anyString())).thenReturn(user);

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(registerDTO));
        verify(userRepo, never()).save(any(User.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void testLoginUser_Success() throws ResourceNotFoundException {
        when(userRepo.findByEmail(anyString())).thenReturn(user);
        
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        doReturn(Collections.singleton(authority)).when(userDetails).getAuthorities();
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(jwtService.generateToken("test@test.com", "ROLE_USER", 1L)).thenReturn("dummy.jwt.token");
        when(userAddressRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        LoginResponse response = userService.loginUser(loginDTO);

        assertNotNull(response);
        assertEquals("dummy.jwt.token", response.getToken());
        assertEquals("ROLE_USER", response.getRole());
        assertEquals("test@test.com", response.getEmail());
        assertEquals(1L, response.getUserId());
        assertNotNull(response.getAddresses());
    }

    @Test
    void testLoginUser_UserNotFound() {
        when(userRepo.findByEmail(anyString())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> userService.loginUser(loginDTO));
    }
}
