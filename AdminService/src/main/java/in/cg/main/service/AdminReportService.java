package in.cg.main.service;

import org.springframework.stereotype.Service;

import in.cg.main.entities.Medicine;
import in.cg.main.repository.MedicineRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

@Service
public class AdminReportService {

    private final MedicineRepository medicineRepository;

    public AdminReportService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    // ✅ Export full medicine catalog as CSV
    public byte[] exportMedicinesCsv() throws IOException {
        List<Medicine> medicines = medicineRepository.findAll();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "Name", "Brand", "Category", "Price",
                             "Stock", "Requires Prescription", "Expiry Date", "Active"))) {

            for (Medicine m : medicines) {
                printer.printRecord(
                        m.getId(),
                        m.getName(),
                        m.getBrand(),
                        m.getCategory() != null ? m.getCategory().getName() : "",
                        m.getPrice(),
                        m.getStock(),
                        m.isRequiresPrescription() ? "Yes" : "No",
                        m.getExpiryDate(),
                        m.isActive() ? "Yes" : "No"
                );
            }

            printer.flush();
            return out.toByteArray();
        }
    }

    // ✅ Export low-stock medicines as CSV
    public byte[] exportLowStockCsv() throws IOException {
        List<Medicine> lowStock = medicineRepository.findByStockLessThan(10);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "Name", "Brand", "Current Stock", "Price"))) {

            for (Medicine m : lowStock) {
                printer.printRecord(
                        m.getId(),
                        m.getName(),
                        m.getBrand(),
                        m.getStock(),
                        m.getPrice()
                );
            }

            printer.flush();
            return out.toByteArray();
        }
    }
}