package in.cg.main.client;

import in.cg.main.client.dto.AuthAddressResponse;
import in.cg.main.client.dto.AuthUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "auth-client",
        url = "${auth.service.url:http://localhost:8081}"
)
public interface AuthClient {

    @GetMapping("/api/internal/users/{userId}/addresses")
    List<AuthAddressResponse> getUserAddresses(@PathVariable("userId") Long userId);

    @GetMapping("/api/internal/users/{userId}")
    AuthUserResponse getUserById(@PathVariable("userId") Long userId);
}
