package in.cg.main.service;

import in.cg.main.config.MedicineRabbitMQConfig;
import in.cg.main.event.MedicineChangedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MedicineNotificationService {

    private final RabbitTemplate rabbitTemplate;

    public MedicineNotificationService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(Long medicineId, String medicineName, String action) {
        MedicineChangedEvent event = new MedicineChangedEvent();
        event.setMedicineId(medicineId);
        event.setMedicineName(medicineName);
        event.setAction(action);
        event.setChangedAt(LocalDateTime.now());

        rabbitTemplate.convertAndSend(
                MedicineRabbitMQConfig.EXCHANGE,
                MedicineRabbitMQConfig.ROUTING_KEY,
                event
        );
    }
}
