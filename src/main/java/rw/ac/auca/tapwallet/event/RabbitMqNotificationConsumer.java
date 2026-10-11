package rw.ac.auca.tapwallet.event;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rabbitmq")
public class RabbitMqNotificationConsumer {

    private final NotificationDeliveryService deliveryService;

    public RabbitMqNotificationConsumer(NotificationDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE)
    public void consume(NotificationEvent event) {
        deliveryService.deliver(event);
    }
}
