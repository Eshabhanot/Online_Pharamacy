package in.cg.main.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MedicineRabbitMQConfig {
    public static final String EXCHANGE = "medicine.exchange";
    public static final String ROUTING_KEY = "medicine.changed";
    public static final String ADMIN_AUDIT_QUEUE = "admin.medicine.audit.queue";

    @Bean
    public TopicExchange medicineExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue adminAuditQueue() {
        return new Queue(ADMIN_AUDIT_QUEUE, true);
    }

    @Bean
    public Binding adminAuditBinding(Queue adminAuditQueue, TopicExchange medicineExchange) {
        return BindingBuilder.bind(adminAuditQueue).to(medicineExchange).with(ROUTING_KEY);
    }
}
