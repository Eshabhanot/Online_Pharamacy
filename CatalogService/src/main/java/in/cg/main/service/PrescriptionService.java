package in.cg.main.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import in.cg.main.dto.PrescriptionInternalResponse;
import in.cg.main.dto.PrescriptionUploadResponse;
import in.cg.main.entities.Prescription;

public interface PrescriptionService {

	PrescriptionUploadResponse uploadPrescription(Long userId,MultipartFile file);
	PrescriptionInternalResponse getById(Long id);
	List<Prescription> getByUserId(Long userId);
	List<Prescription> getPendingPrescription();
	Prescription reviewPrescription(Long id,boolean approved,String rejectionReason);
	
}


