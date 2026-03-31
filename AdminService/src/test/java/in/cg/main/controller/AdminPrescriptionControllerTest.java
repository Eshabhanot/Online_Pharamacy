package in.cg.main.controller;

import in.cg.main.dto.PrescriptionReviewRequest;
import in.cg.main.entities.Prescription;
import in.cg.main.service.AdminPrescriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPrescriptionControllerTest {

    @Mock
    private AdminPrescriptionService adminPrescriptionService;

    @InjectMocks
    private AdminPrescriptionController adminPrescriptionController;

    @Test
    void getPendingQueue_returnsOk() {
        List<Prescription> prescriptions = List.of(new Prescription());
        when(adminPrescriptionService.getPendingPrescriptions()).thenReturn(prescriptions);

        ResponseEntity<List<Prescription>> response = adminPrescriptionController.getPendingQueue();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(prescriptions, response.getBody());
        verify(adminPrescriptionService).getPendingPrescriptions();
    }

    @Test
    void getById_returnsOk() {
        Prescription prescription = new Prescription();
        prescription.setId(11L);
        when(adminPrescriptionService.getPrescriptionById(11L)).thenReturn(prescription);

        ResponseEntity<Prescription> response = adminPrescriptionController.getById(11L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(prescription, response.getBody());
        verify(adminPrescriptionService).getPrescriptionById(11L);
    }

    @Test
    void review_returnsOkAndUsesPlaceholderAdminId() {
        PrescriptionReviewRequest request = new PrescriptionReviewRequest();
        request.setApproved(true);
        Prescription reviewed = new Prescription();
        when(adminPrescriptionService.reviewPrescription(eq(5L), eq(request), eq(1L)))
            .thenReturn(reviewed);

        ResponseEntity<Prescription> response = adminPrescriptionController.review(
            5L, request, (Authentication) null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(reviewed, response.getBody());
        verify(adminPrescriptionService).reviewPrescription(5L, request, 1L);
    }
}
