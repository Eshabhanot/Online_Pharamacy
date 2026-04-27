package in.cg.main.service;

import in.cg.main.dto.PrescriptionReviewRequest;
import in.cg.main.entities.Prescription;
import in.cg.main.enums.PrescriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AdminPrescriptionServiceTest {

    private static final String CATALOG_URL = "http://catalog-service:8082";
    private static final String ORDER_URL = "http://order-service:8083";

    @Mock private RestTemplate restTemplate;
    @Mock private DiscoveryClient discoveryClient;

    private AdminPrescriptionService adminPrescriptionService;

    @BeforeEach
    void setUp() {
        adminPrescriptionService = new AdminPrescriptionService(
                restTemplate,
                discoveryClient,
                CATALOG_URL,
                ORDER_URL
        );
        lenient().when(discoveryClient.getInstances(any())).thenReturn(List.of());
    }

    @Test
    void getPendingPrescriptions_returnsList() {
        Prescription pending = new Prescription();
        pending.setId(1L);
        pending.setStatus(PrescriptionStatus.PENDING);
        when(restTemplate.getForObject(contains("/internal/pending"), eq(Prescription[].class)))
                .thenReturn(new Prescription[] { pending });

        List<Prescription> result = adminPrescriptionService.getPendingPrescriptions();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getPrescriptionById_found() {
        Prescription p = new Prescription();
        p.setId(1L);
        when(restTemplate.getForObject(CATALOG_URL + "/api/prescriptions/internal/1", Prescription.class))
                .thenReturn(p);

        Prescription result = adminPrescriptionService.getPrescriptionById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void reviewPrescription_approve_success() {
        Prescription p = new Prescription();
        p.setId(1L);
        p.setStatus(PrescriptionStatus.APPROVED);

        PrescriptionReviewRequest req = new PrescriptionReviewRequest();
        req.setApproved(true);

        when(restTemplate.exchange(
                any(RequestEntity.class),
                eq(Prescription.class)))
                .thenReturn(new ResponseEntity<>(p, HttpStatus.OK));
        when(restTemplate.exchange(
                any(RequestEntity.class),
                eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        Prescription result = adminPrescriptionService.reviewPrescription(1L, req, 1L);

        assertEquals(PrescriptionStatus.APPROVED, result.getStatus());
        verify(restTemplate).exchange(any(RequestEntity.class), eq(Void.class));
    }

    @Test
    void reviewPrescription_reject_success() {
        Prescription p = new Prescription();
        p.setId(1L);
        p.setStatus(PrescriptionStatus.REJECTED);
        p.setRejectionReason("Invalid prescription");

        PrescriptionReviewRequest req = new PrescriptionReviewRequest();
        req.setApproved(false);
        req.setRejectionReason("Invalid prescription");

        when(restTemplate.exchange(any(RequestEntity.class), eq(Prescription.class)))
                .thenReturn(new ResponseEntity<>(p, HttpStatus.OK));
        when(restTemplate.exchange(any(RequestEntity.class), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        Prescription result = adminPrescriptionService.reviewPrescription(1L, req, 1L);

        assertEquals(PrescriptionStatus.REJECTED, result.getStatus());
        assertEquals("Invalid prescription", result.getRejectionReason());
    }

    @Test
    void reviewPrescription_rejectWithoutReason_throws() {
        PrescriptionReviewRequest req = new PrescriptionReviewRequest();
        req.setApproved(false);

        assertThrows(RuntimeException.class,
                () -> adminPrescriptionService.reviewPrescription(1L, req, 1L));
    }
}
