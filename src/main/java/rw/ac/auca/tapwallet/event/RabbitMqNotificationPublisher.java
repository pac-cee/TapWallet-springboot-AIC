package rw.ac.auca.tapwallet.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rabbitmq")
public class RabbitMqNotificationPublisher implements NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqNotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(NotificationEvent event) {
        String routingKey = "notification." + event.channel().name().toLowerCase() + "." + event.type();
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, routingKey, event);
    }
}
