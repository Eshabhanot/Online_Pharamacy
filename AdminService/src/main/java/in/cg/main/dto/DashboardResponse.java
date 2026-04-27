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
    private List<TodayOrderSummary> todaysOrders;

    public long getTotalOrdersToday() { return totalOrdersToday; }
    public long getPendingOrders() { return pendingOrders; }
    public long getDeliveredOrdersToday() { return deliveredOrdersToday; }
    public BigDecimal getRevenueToday() { return revenueToday; }
    public long getPendingPrescriptions() { return pendingPrescriptions; }
    public long getLowStockMedicines() { return lowStockMedicines; }
    public long getExpiringBatchesIn30Days() { return expiringBatchesIn30Days; }
    public long getTotalActiveMedicines() { return totalActiveMedicines; }
    public List<AdminOrderTrackingResponse> getRecentOrderTracking() { return recentOrderTracking; }
    public List<TodayOrderSummary> getTodaysOrders() { return todaysOrders; }

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
    public void setTodaysOrders(List<TodayOrderSummary> todaysOrders) {
        this.todaysOrders = todaysOrders;
    }

    public static class TodayOrderSummary {
        private Long orderId;
        private String orderNumber;
        private String customerEmail;
        private String status;
        private BigDecimal totalAmount;
        private int itemCount;
        private String placedAt;

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public int getItemCount() { return itemCount; }
        public void setItemCount(int itemCount) { this.itemCount = itemCount; }
        public String getPlacedAt() { return placedAt; }
        public void setPlacedAt(String placedAt) { this.placedAt = placedAt; }
    }
}
