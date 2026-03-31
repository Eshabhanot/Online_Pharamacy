package in.cg.main.dto;

import java.util.Collections;
import java.util.List;

public class LoginResponse {

    private String token;
    private String email;
    private String role;
    private Long userId;
    private List<AddressResponse> addresses;

    public LoginResponse(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.addresses = Collections.emptyList();
    }

    public LoginResponse(String token, String email, String role, Long userId, List<AddressResponse> addresses) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.addresses = addresses;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Long getUserId() {
        return userId;
    }

    public List<AddressResponse> getAddresses() {
        return addresses;
    }
}
