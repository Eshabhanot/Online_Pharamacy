package in.cg.main.service;

import in.cg.main.dto.PrescriptionReviewRequest;
import in.cg.main.entities.Prescription;
import in.cg.main.enums.PrescriptionStatus;
import in.cg.main.repository.PrescriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminPrescriptionServiceTest {

    @Mock private PrescriptionRepository prescriptionRepository;

    @InjectMocks
    private AdminPrescriptionService adminPrescriptionService;

    @Test
    void getPendingPrescriptions_returnsList() {
        when(prescriptionRepository.findByStatus(PrescriptionStatus.PENDING))
                .thenReturn(Collections.emptyList());

        List<Prescription> result = adminPrescriptionService.getPendingPrescriptions();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPrescriptionById_found() {
        Prescription p = new Prescription();
        p.setId(1L);
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(p));

        Prescription result = adminPrescriptionService.getPrescriptionById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getPrescriptionById_notFound_throws() {
        when(prescriptionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> adminPrescriptionService.getPrescriptionById(99L));
    }

    @Test
    void reviewPrescription_approve_success() {
        Prescription p = new Prescription();
        p.setId(1L);
        p.setStatus(PrescriptionStatus.PENDING);

        PrescriptionReviewRequest req = new PrescriptionReviewRequest();
        req.setApproved(true);

        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(p));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(p);

        Prescription result = adminPrescriptionService.reviewPrescription(1L, req, 1L);

        assertEquals(PrescriptionStatus.APPROVED, result.getStatus());
    }

    @Test
    void reviewPrescription_reject_success() {
        Prescription p = new Prescription();
        p.setId(1L);
        p.setStatus(PrescriptionStatus.PENDING);

        PrescriptionReviewRequest req = new PrescriptionReviewRequest();
        req.setApproved(false);
        req.setRejectionReason("Invalid prescription");

        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(p));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(p);

        Prescription result = adminPrescriptionService.reviewPrescription(1L, req, 1L);

        assertEquals(PrescriptionStatus.REJECTED, result.getStatus());
        assertEquals("Invalid prescription", result.getRejectionReason());
    }

    @Test
    void reviewPrescription_alreadyReviewed_throws() {
        Prescription p = new Prescription();
        p.setId(1L);
        p.setStatus(PrescriptionStatus.APPROVED);

        PrescriptionReviewRequest req = new PrescriptionReviewRequest();
        req.setApproved(true);

        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(RuntimeException.class,
                () -> adminPrescriptionService.reviewPrescription(1L, req, 1L));
    }

    @Test
    void reviewPrescription_rejectWithoutReason_throws() {
        Prescription p = new Prescription();
        p.setId(1L);
        p.setStatus(PrescriptionStatus.PENDING);

        PrescriptionReviewRequest req = new PrescriptionReviewRequest();
        req.setApproved(false);
        // No rejection reason

        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(RuntimeException.class,
                () -> adminPrescriptionService.reviewPrescription(1L, req, 1L));
    }

    @Test
    void reviewPrescription_rejectWithBlankReason_throws() {
        Prescription p = new Prescription();
        p.setId(1L);
        p.setStatus(PrescriptionStatus.PENDING);

        PrescriptionReviewRequest req = new PrescriptionReviewRequest();
        req.setApproved(false);
        req.setRejectionReason("   ");
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(RuntimeException.class,
            () -> adminPrescriptionService.reviewPrescription(1L, req, 1L));
    }

    @Test
    void reviewPrescription_notFound_throws() {
        PrescriptionReviewRequest req = new PrescriptionReviewRequest();
        req.setApproved(true);
        when(prescriptionRepository.findById(123L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> adminPrescriptionService.reviewPrescription(123L, req, 1L));
        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }
}
