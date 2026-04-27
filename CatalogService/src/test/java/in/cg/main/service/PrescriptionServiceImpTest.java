package in.cg.main.service;

import in.cg.main.dto.PrescriptionUploadResponse;
import in.cg.main.entities.Prescription;
import in.cg.main.repository.PrescriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImpTest {

    @Mock
    private PrescriptionRepository repo;

    @InjectMocks
    private PrescriptionServiceImp service;

    @Test
    void uploadPrescription_success_png() {
        MockMultipartFile file = new MockMultipartFile("file", "p.png", "image/png", "abc".getBytes()) {
            @Override
            public void transferTo(File dest) {
                // no-op to avoid actual filesystem write in test
            }
        };
        Prescription saved = new Prescription();
        saved.setId(10L);
        saved.setStatus(Prescription.PrescriptionStatus.PENDING);
        saved.setFileName("x.png");

        when(repo.save(any(Prescription.class))).thenReturn(saved);

        PrescriptionUploadResponse response = service.uploadPrescription(5L, file);

        assertEquals(10L, response.getPrescriptionId());
        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void uploadPrescription_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", new byte[0]);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.uploadPrescription(1L, file));

        assertEquals("File is empty", ex.getMessage());
    }

    @Test
    void uploadPrescription_nullContentType_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "x.bin", null, "data".getBytes());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.uploadPrescription(1L, file));

        assertEquals("Invalid file type", ex.getMessage());
    }

    @Test
    void uploadPrescription_invalidType_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "x.txt", "text/plain", "data".getBytes());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.uploadPrescription(1L, file));

        assertEquals("File must be PNG, JPG, or PDF", ex.getMessage());
    }

    @Test
    void uploadPrescription_sizeTooLarge_throws() {
        byte[] big = new byte[(2 * 1024 * 1024) + 1];
        MockMultipartFile file = new MockMultipartFile("file", "x.pdf", "application/pdf", big);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.uploadPrescription(1L, file));

        assertEquals("File size must be less than 2MB", ex.getMessage());
    }

    @Test
    void uploadPrescription_ioException_throws() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "x.pdf", "application/pdf", "data".getBytes()) {
            @Override
            public void transferTo(java.io.File dest) throws IOException {
                throw new IOException("disk error");
            }
        };

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.uploadPrescription(1L, file));

        assertTrue(ex.getMessage().contains("File upload failed: disk error"));
    }

    @Test
    void uploadPrescription_createsDirectory_andHandlesJpeg() throws Exception {
        Path uploadPath = Path.of("C:\\uploads");
        if (Files.exists(uploadPath)) {
            Files.walk(uploadPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        MockMultipartFile file = new MockMultipartFile("file", "x.jpg", "image/jpeg", "data".getBytes()) {
            @Override
            public void transferTo(File dest) {
                // no-op to avoid actual filesystem write in test
            }
        };

        Prescription saved = new Prescription();
        saved.setId(12L);
        saved.setStatus(Prescription.PrescriptionStatus.PENDING);
        saved.setFileName("x.jpg");
        when(repo.save(any(Prescription.class))).thenReturn(saved);

        PrescriptionUploadResponse response = service.uploadPrescription(1L, file);

        assertEquals(12L, response.getPrescriptionId());
        assertTrue(Files.exists(uploadPath));
    }

    @Test
    void getByUserId_returnsRepoData() {
        Prescription p = new Prescription();
        p.setId(1L);
        when(repo.findByCustomerId(9L)).thenReturn(List.of(p));

        List<Prescription> result = service.getByUserId(9L);

        assertEquals(1, result.size());
    }

    @Test
    void getPendingPrescription_returnsRepoData() {
        when(repo.findByStatus(Prescription.PrescriptionStatus.PENDING)).thenReturn(List.of(new Prescription()));

        List<Prescription> result = service.getPendingPrescription();

        assertEquals(1, result.size());
    }

    @Test
    void reviewPrescription_approved() {
        Prescription p = new Prescription();
        p.setId(7L);
        p.setStatus(Prescription.PrescriptionStatus.PENDING);
        when(repo.findById(7L)).thenReturn(Optional.of(p));
        when(repo.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prescription result = service.reviewPrescription(7L, true, null);

        assertEquals(Prescription.PrescriptionStatus.APPROVED, result.getStatus());
        assertNotNull(result.getReviewedAt());
    }

    @Test
    void reviewPrescription_rejected_setsReason() {
        Prescription p = new Prescription();
        p.setId(7L);
        when(repo.findById(7L)).thenReturn(Optional.of(p));
        when(repo.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prescription result = service.reviewPrescription(7L, false, "Not clear");

        assertEquals(Prescription.PrescriptionStatus.REJECTED, result.getStatus());
        assertEquals("Not clear", result.getRejectionReason());
    }

    @Test
    void reviewPrescription_notFound_throws() {
        when(repo.findById(100L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.reviewPrescription(100L, true, null));

        assertEquals("Prescription Not Found", ex.getMessage());
    }
}
