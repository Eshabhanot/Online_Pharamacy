package in.cg.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import in.cg.main.config.RabbitMQConfig;
import in.cg.main.dto.OrderNotification;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final RabbitTemplate rabbitTemplate;

    public NotificationService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOrderNotification(Long orderId, String customerEmail, String status, String message) {
        OrderNotification notification = new OrderNotification(orderId, customerEmail, status, message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, notification);
        log.info("Order notification sent for orderId={} with status={}", orderId, status);
    }
}
