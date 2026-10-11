package rw.ac.auca.tapwallet.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Safe academic delivery adapter. A production adapter can replace this class
 * with an email/SMS provider without changing payment or RabbitMQ code.
 */
@Service
public class LoggingNotificationDeliveryService implements NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationDeliveryService.class);

    @Override
    public void deliver(NotificationEvent event) {
        log.info("Delivered simulated {} notification: id={}, type={}, recipient={}",
                event.channel(), event.id(), event.type(), event.recipient());
    }
}
