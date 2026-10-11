package rw.ac.auca.tapwallet.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationEvent(
        String id,
        String type,
        NotificationChannel channel,
        String recipient,
        String message,
        Instant occurredAt,
        Map<String, String> metadata) {

    public static NotificationEvent email(String type, String recipient, String message,
                                          Map<String, String> metadata) {
        return of(type, NotificationChannel.EMAIL, recipient, message, metadata);
    }

    public static NotificationEvent sms(String type, String recipient, String message,
                                        Map<String, String> metadata) {
        return of(type, NotificationChannel.SMS, recipient, message, metadata);
    }

    public static NotificationEvent of(String type, NotificationChannel channel, String recipient,
                                       String message, Map<String, String> metadata) {
        return new NotificationEvent(UUID.randomUUID().toString(), type, channel, recipient, message,
                Instant.now(), metadata == null ? Map.of() : Map.copyOf(metadata));
    }
}
