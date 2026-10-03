package rw.ac.auca.tapwallet.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record NotificationEvent(
        String id,
        String type,
        String recipient,
        String message,
        Instant occurredAt,
        Map<String, String> metadata) {

    public static NotificationEvent of(String type, String recipient, String message,
                                       Map<String, String> metadata) {
        return new NotificationEvent(UUID.randomUUID().toString(), type, recipient, message,
                Instant.now(), metadata == null ? Map.of() : Map.copyOf(metadata));
    }
}
