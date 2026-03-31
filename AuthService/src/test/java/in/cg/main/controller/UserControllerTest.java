package in.cg.main.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.cg.main.dto.LoginDTO;
import in.cg.main.dto.LoginResponse;
import in.cg.main.dto.RegisterDTO;
import in.cg.main.service.UserServiceImp;
import in.cg.main.service.JwtService;
import in.cg.main.security.MyUserDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceImp userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private MyUserDetailsService myUserDetailsService;

    @Test
    public void testRegisterUser() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setName("Test");
        dto.setEmail("test@test.com");
        dto.setPassword("password123");
        dto.setMobile("9876543210");
        dto.setRole("USER");
        dto.setStatus("ACTIVE");

        Mockito.doNothing().when(userService).registerUser(any(RegisterDTO.class));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("User Registered Successfully"));
    }

    @Test
    public void testLoginUser() throws Exception {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("password123");

        LoginResponse response = new LoginResponse("mock-token", "test@test.com", "ROLE_USER", 1L, java.util.Collections.emptyList());

        Mockito.when(userService.loginUser(any(LoginDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().json("{'token':'mock-token','email':'test@test.com','role':'ROLE_USER','userId':1,'addresses':[]}"));
    }
}
