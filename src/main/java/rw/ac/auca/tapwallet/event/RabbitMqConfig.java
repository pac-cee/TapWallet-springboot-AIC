package rw.ac.auca.tapwallet.event;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("rabbitmq")
public class RabbitMqConfig {

    public static final String EXCHANGE = "tapwallet.events";
    public static final String NOTIFICATION_QUEUE = "tapwallet.notifications";

    @Bean
    TopicExchange tapWalletExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    Binding notificationBinding(Queue notificationQueue, TopicExchange tapWalletExchange) {
        return BindingBuilder.bind(notificationQueue).to(tapWalletExchange).with("notification.#");
    }

    @Bean
    Jackson2JsonMessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
