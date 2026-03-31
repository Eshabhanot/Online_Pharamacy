package in.cg.main.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import in.cg.main.dto.MedicineDTO;

public interface MedicineService {

	Page<MedicineDTO> searchByNameAndId(String name,Long id,int page ,int size);
	MedicineDTO getById(Long id);
}
