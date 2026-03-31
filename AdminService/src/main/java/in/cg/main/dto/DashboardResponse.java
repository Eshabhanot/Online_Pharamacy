package in.cg.main.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {

    private long totalOrdersToday;
    private long pendingOrders;
    private long deliveredOrdersToday;
    private BigDecimal revenueToday;
    private long pendingPrescriptions;
    private long lowStockMedicines;
    private long expiringBatchesIn30Days;
    private long totalActiveMedicines;
    private List<AdminOrderTrackingResponse> recentOrderTracking;

    public long getTotalOrdersToday() { return totalOrdersToday; }
    public long getPendingOrders() { return pendingOrders; }
    public long getDeliveredOrdersToday() { return deliveredOrdersToday; }
    public BigDecimal getRevenueToday() { return revenueToday; }
    public long getPendingPrescriptions() { return pendingPrescriptions; }
    public long getLowStockMedicines() { return lowStockMedicines; }
    public long getExpiringBatchesIn30Days() { return expiringBatchesIn30Days; }
    public long getTotalActiveMedicines() { return totalActiveMedicines; }
    public List<AdminOrderTrackingResponse> getRecentOrderTracking() { return recentOrderTracking; }

    public void setTotalOrdersToday(long v) { this.totalOrdersToday = v; }
    public void setPendingOrders(long v) { this.pendingOrders = v; }
    public void setDeliveredOrdersToday(long v) { this.deliveredOrdersToday = v; }
    public void setRevenueToday(BigDecimal v) { this.revenueToday = v; }
    public void setPendingPrescriptions(long v) { this.pendingPrescriptions = v; }
    public void setLowStockMedicines(long v) { this.lowStockMedicines = v; }
    public void setExpiringBatchesIn30Days(long v) { this.expiringBatchesIn30Days = v; }
    public void setTotalActiveMedicines(long v) { this.totalActiveMedicines = v; }
    public void setRecentOrderTracking(List<AdminOrderTrackingResponse> recentOrderTracking) {
        this.recentOrderTracking = recentOrderTracking;
    }
}
