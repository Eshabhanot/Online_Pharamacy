package in.cg.main.client;

import in.cg.main.client.dto.OrderAddressResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(
        name = "orderAddressClient",
        url = "${order.service.url:http://localhost:8083}"
)
public interface OrderAddressClient {

    @GetMapping("/api/orders/addresses")
    List<OrderAddressResponse> getAddresses(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Customer-Id") Long customerId
    );
}
