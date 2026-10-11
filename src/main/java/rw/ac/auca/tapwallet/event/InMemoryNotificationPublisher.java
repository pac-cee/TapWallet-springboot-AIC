package rw.ac.auca.tapwallet.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Profile("!rabbitmq")
public class InMemoryNotificationPublisher implements NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(InMemoryNotificationPublisher.class);
    private final List<NotificationEvent> publishedEvents = new CopyOnWriteArrayList<>();

    @Override
    public void publish(NotificationEvent event) {
        publishedEvents.add(event);
        log.info("In-memory notification event: type={}, channel={}, recipient={}",
                event.type(), event.channel(), event.recipient());
    }

    public List<NotificationEvent> publishedEvents() {
        return List.copyOf(publishedEvents);
    }
}
