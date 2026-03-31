package in.cg.main.service;

import in.cg.main.client.AdminMedicineClient;
import in.cg.main.client.dto.AdminMedicineResponse;
import in.cg.main.dto.MedicineDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineServiceImp implements MedicineService {

    private final AdminMedicineClient adminMedicineClient;

    public MedicineServiceImp(AdminMedicineClient adminMedicineClient) {
        this.adminMedicineClient = adminMedicineClient;
    }

    private MedicineDTO map(AdminMedicineResponse src) {
        MedicineDTO dto = new MedicineDTO();
        dto.setId(src.getId());
        dto.setName(src.getName());
        dto.setBrand(src.getBrand());
        dto.setDescription(src.getDescription());
        dto.setDosage(src.getDosage());
        dto.setPrice(src.getPrice() != null ? src.getPrice().doubleValue() : 0.0);
        dto.setStock(src.getStock());
        dto.setRequiresPrescription(src.isRequiresPrescription());
        dto.setCategoryName(src.getCategoryName());
        dto.setExpiryDate(src.getExpiryDate());
        return dto;
    }

    @Override
    public Page<MedicineDTO> searchByNameAndId(String name, Long id, int page, int size) {
        List<AdminMedicineResponse> medicines = adminMedicineClient.getMedicines(name, id);
        List<MedicineDTO> filtered = medicines.stream()
                .filter(AdminMedicineResponse::isActive)
                .filter(m -> m.getStock() > 0)
                .map(this::map)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), filtered.size());
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end), pageable, filtered.size());
    }

    @Override
    @Cacheable(value = "medicines", key = "#id")
    public MedicineDTO getById(Long id) {
        AdminMedicineResponse medicine = adminMedicineClient.getMedicineById(id);
        if (medicine == null || !medicine.isActive()) {
            throw new RuntimeException("Medicine Not Found!!");
        }
        return map(medicine);
    }
}
