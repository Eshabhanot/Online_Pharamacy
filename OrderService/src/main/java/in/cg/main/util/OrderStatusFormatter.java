package in.cg.main.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import in.cg.main.enums.OrderStatus;
import in.cg.main.enums.PaymentStatus;

public final class OrderStatusFormatter {

    private OrderStatusFormatter() {
    }

    public static String toDisplayName(OrderStatus status) {
        return status == null ? null : humanize(status.name());
    }

    public static String toDisplayName(PaymentStatus status) {
        return status == null ? null : humanize(status.name());
    }

    public static OrderStatus parseOrderStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            throw new IllegalArgumentException("Order status is required");
        }

        String normalized = rawStatus.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);

        try {
            return OrderStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported order status: " + rawStatus);
        }
    }

    private static String humanize(String rawValue) {
        return Arrays.stream(rawValue.split("_"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1) + part.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }
}
