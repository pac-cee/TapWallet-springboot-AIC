package rw.ac.auca.tapwallet.event;

public interface NotificationPublisher {
    void publish(NotificationEvent event);
}
