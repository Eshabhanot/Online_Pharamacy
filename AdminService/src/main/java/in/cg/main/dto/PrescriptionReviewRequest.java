package in.cg.main.dto;



public class PrescriptionReviewRequest {

    private boolean approved;
    private String rejectionReason;   // required when approved = false

    public boolean isApproved() { return approved; }
    public String getRejectionReason() { return rejectionReason; }

    public void setApproved(boolean approved) { this.approved = approved; }
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}