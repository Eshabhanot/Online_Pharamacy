package in.cg.main.client;

import in.cg.main.client.dto.CatalogCategoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "catalogServiceClient",
        url = "${catalog.service.url:http://localhost:8082}"
)
public interface CatalogServiceClient {

    @GetMapping("/api/categories/{id}")
    CatalogCategoryResponse getCategoryById(@PathVariable("id") Long id);
}
