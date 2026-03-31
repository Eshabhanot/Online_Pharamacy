package in.cg.main.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MedicineRabbitMQConfig {
    public static final String EXCHANGE = "medicine.exchange";
    public static final String ROUTING_KEY = "medicine.changed";
    public static final String CATALOG_QUEUE = "catalog.medicine.notification.queue";

    @Bean
    public TopicExchange medicineExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue catalogMedicineQueue() {
        return new Queue(CATALOG_QUEUE, true);
    }

    @Bean
    public Binding catalogMedicineBinding(Queue catalogMedicineQueue, TopicExchange medicineExchange) {
        return BindingBuilder.bind(catalogMedicineQueue).to(medicineExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
