package in.cg.main.service;

import in.cg.main.config.MedicineRabbitMQConfig;
import in.cg.main.event.MedicineChangedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class MedicineNotificationListener {

    @RabbitListener(queues = MedicineRabbitMQConfig.CATALOG_QUEUE)
    @CacheEvict(value = "medicines", allEntries = true)
    public void onMedicineChanged(MedicineChangedEvent event) {
        System.out.println("[CatalogService] Medicine changed event received: "
                + event.getAction() + " -> " + event.getMedicineName());
    }
}
