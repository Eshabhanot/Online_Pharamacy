package in.cg.main.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "order.exchange";
    public static final String QUEUE = "order.notification.queue";
    public static final String ROUTING_KEY = "order.notification";
    public static final String MEDICINE_EXCHANGE = "medicine.exchange";
    public static final String MEDICINE_ROUTING_KEY = "medicine.changed";
    public static final String MEDICINE_QUEUE = "order.medicine.notification.queue";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding binding(Queue notificationQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(notificationQueue).to(orderExchange).with(ROUTING_KEY);
    }

    @Bean
    public TopicExchange medicineExchange() {
        return new TopicExchange(MEDICINE_EXCHANGE);
    }

    @Bean
    public Queue medicineNotificationQueue() {
        return QueueBuilder.durable(MEDICINE_QUEUE).build();
    }

    @Bean
    public Binding medicineBinding(Queue medicineNotificationQueue, TopicExchange medicineExchange) {
        return BindingBuilder.bind(medicineNotificationQueue).to(medicineExchange).with(MEDICINE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
