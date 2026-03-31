package in.cg.main.security;

import org.springframework.stereotype.Component;

@Component
public class RequestCustomerResolver {

    private final JwtService jwtService;

    public RequestCustomerResolver(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public Long resolve(Long customerIdHeader, String authorizationHeader) {
        if (customerIdHeader != null) {
            return customerIdHeader;
        }

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            Long tokenUserId = jwtService.extractUserId(authorizationHeader.substring(7));
            if (tokenUserId != null) {
                return tokenUserId;
            }
        }

        throw new IllegalArgumentException("X-Customer-Id header missing and token does not contain userId claim");
    }
}
