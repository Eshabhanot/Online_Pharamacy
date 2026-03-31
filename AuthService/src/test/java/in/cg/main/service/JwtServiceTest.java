package in.cg.main.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService();
    }

    @Test
    public void testGenerateAndValidateToken() {
        String username = "testuser";
        String role = "ROLE_USER";

        String token = jwtService.generateToken(username, role, 1L);

        assertNotNull(token);
        
        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn(username);

        boolean isValid = jwtService.validateToken(token, userDetails);
        assertTrue(isValid);

        String extractedUser = jwtService.extractUserName(token);
        assertEquals(username, extractedUser);
    }
}
