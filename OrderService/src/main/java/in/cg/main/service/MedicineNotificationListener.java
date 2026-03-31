package in.cg.main.service;

import in.cg.main.config.RabbitMQConfig;
import in.cg.main.event.MedicineChangedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MedicineNotificationListener {

    @RabbitListener(queues = RabbitMQConfig.MEDICINE_QUEUE)
    public void onMedicineChanged(MedicineChangedEvent event) {
        System.out.println("[OrderService] Medicine update received: "
                + event.getAction() + " -> " + event.getMedicineName());
    }
}
