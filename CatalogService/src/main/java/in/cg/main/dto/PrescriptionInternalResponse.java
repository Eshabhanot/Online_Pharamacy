package in.cg.main.dto;

import in.cg.main.entities.Prescription;

public class PrescriptionInternalResponse {
    private Long id;
    private Long customerId;
    private String status;

    public static PrescriptionInternalResponse from(Prescription prescription) {
        PrescriptionInternalResponse response = new PrescriptionInternalResponse();
        response.setId(prescription.getId());
        response.setCustomerId(prescription.getCustomerId());
        response.setStatus(prescription.getStatus().name());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
