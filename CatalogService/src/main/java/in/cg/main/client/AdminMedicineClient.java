package in.cg.main.client;

import in.cg.main.client.dto.AdminMedicineResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "adminMedicineClient",
        url = "${admin.service.url:http://localhost:8084}"
)
public interface AdminMedicineClient {

    @GetMapping("/api/internal/medicines")
    List<AdminMedicineResponse> getMedicines(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "categoryId", required = false) Long categoryId
    );

    @GetMapping("/api/internal/medicines/{id}")
    AdminMedicineResponse getMedicineById(@PathVariable("id") Long id);
}
