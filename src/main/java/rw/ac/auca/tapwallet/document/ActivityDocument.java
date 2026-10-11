package rw.ac.auca.tapwallet.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Document(collection = "activity_log")
@CompoundIndexes({
        @CompoundIndex(name = "idx_activity_event_occurred", def = "{'eventType': 1, 'occurredAt': -1}"),
        @CompoundIndex(name = "idx_activity_actor_occurred", def = "{'actorUserId': 1, 'occurredAt': -1}")
})
public class ActivityDocument {

    @Id
    private String id;

    @Indexed
    private String eventType;

    @Indexed
    private Long actorUserId;

    private String entityType;
    private String entityId;
    private String message;

    @Indexed
    private Instant occurredAt;

    private Map<String, String> metadata;

    public ActivityDocument() {
    }

    public ActivityDocument(String eventType, Long actorUserId, String entityType, String entityId,
                            String message, Map<String, String> metadata) {
        this.id = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.actorUserId = actorUserId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.message = message;
        this.occurredAt = Instant.now();
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public String getId() { return id; }
    public String getEventType() { return eventType; }
    public Long getActorUserId() { return actorUserId; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getMessage() { return message; }
    public Instant getOccurredAt() { return occurredAt; }
    public Map<String, String> getMetadata() { return metadata; }
}
