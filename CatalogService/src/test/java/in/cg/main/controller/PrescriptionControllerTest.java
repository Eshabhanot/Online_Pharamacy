package in.cg.main.controller;

import in.cg.main.dto.PrescriptionUploadResponse;
import in.cg.main.entities.Prescription;
import in.cg.main.service.PrescriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionControllerTest {

    @Mock
    private PrescriptionService prescriptionService;

    private PrescriptionController prescriptionController;

    @BeforeEach
    void setUp() {
        prescriptionController = new PrescriptionController();
        ReflectionTestUtils.setField(prescriptionController, "prescriptionService", prescriptionService);
    }

    @Test
    void uploadPrescription_returnsResponse() {
        MockMultipartFile file = new MockMultipartFile("file", "p.pdf", "application/pdf", "x".getBytes());
        PrescriptionUploadResponse expected = new PrescriptionUploadResponse(1L, "PENDING", "ok", "p.pdf");
        when(prescriptionService.uploadPrescription(1L, file)).thenReturn(expected);

        PrescriptionUploadResponse result = prescriptionController.uploadPrescription(1L, null, file);

        assertEquals(1L, result.getPrescriptionId());
        verify(prescriptionService).uploadPrescription(1L, file);
    }

    @Test
    void getByUserId_returnsList() {
        Prescription p = new Prescription();
        p.setId(2L);
        when(prescriptionService.getByUserId(9L)).thenReturn(List.of(p));

        List<Prescription> result = prescriptionController.getByUserId(9L);

        assertEquals(1, result.size());
        verify(prescriptionService).getByUserId(9L);
    }

    @Test
    void getPendingPrescriptions_returnsList() {
        when(prescriptionService.getPendingPrescription()).thenReturn(List.of(new Prescription()));

        List<Prescription> result = prescriptionController.getPendingPrescriptions();

        assertEquals(1, result.size());
        verify(prescriptionService).getPendingPrescription();
    }

//    @Test
//    void getInternalPendingPrescriptions_returnsList() {
//        when(prescriptionService.getPendingPrescription()).thenReturn(List.of(new Prescription()));
//
//        List<Prescription> result = prescriptionController.getPendingPrescriptions();
//
//        assertEquals(1, result.size());
//        verify(prescriptionService).getPendingPrescription();
//    }

    @Test
    void reviewPrescription_passesArgs() {
        Prescription p = new Prescription();
        p.setId(5L);
        when(prescriptionService.reviewPrescription(5L, false, "bad image")).thenReturn(p);

        Prescription result = prescriptionController.reviewPrescription(5L, false, "bad image");

        assertEquals(5L, result.getId());
        verify(prescriptionService).reviewPrescription(5L, false, "bad image");
    }

    @Test
    void reviewPrescriptionInternal_passesArgs() {
        Prescription p = new Prescription();
        p.setId(6L);
        when(prescriptionService.reviewPrescription(6L, true, null)).thenReturn(p);

        Prescription result = prescriptionController.reviewPrescriptionInternal(6L, true, null);

        assertEquals(6L, result.getId());
        verify(prescriptionService).reviewPrescription(6L, true, null);
    }
}
