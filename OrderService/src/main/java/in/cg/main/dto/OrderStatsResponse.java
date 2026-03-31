package in.cg.main.dto;

import java.math.BigDecimal;

public class OrderStatsResponse {

    private long totalOrdersToday;
    private long pendingOrders;
    private long deliveredOrdersToday;
    private BigDecimal revenueToday;

    public long getTotalOrdersToday() {
        return totalOrdersToday;
    }

    public void setTotalOrdersToday(long totalOrdersToday) {
        this.totalOrdersToday = totalOrdersToday;
    }

    public long getPendingOrders() {
        return pendingOrders;
    }

    public void setPendingOrders(long pendingOrders) {
        this.pendingOrders = pendingOrders;
    }

    public long getDeliveredOrdersToday() {
        return deliveredOrdersToday;
    }

    public void setDeliveredOrdersToday(long deliveredOrdersToday) {
        this.deliveredOrdersToday = deliveredOrdersToday;
    }

    public BigDecimal getRevenueToday() {
        return revenueToday;
    }

    public void setRevenueToday(BigDecimal revenueToday) {
        this.revenueToday = revenueToday;
    }
}
