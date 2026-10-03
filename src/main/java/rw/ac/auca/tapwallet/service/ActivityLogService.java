package rw.ac.auca.tapwallet.service;

import org.springframework.stereotype.Service;
import rw.ac.auca.tapwallet.document.ActivityDocument;
import rw.ac.auca.tapwallet.document.ActivityDocumentStore;

import java.util.List;
import java.util.Map;

@Service
public class ActivityLogService {

    private final ActivityDocumentStore store;

    public ActivityLogService(ActivityDocumentStore store) {
        this.store = store;
    }

    public void record(String eventType, Long actorUserId, String entityType, Object entityId,
                       String message, Map<String, String> metadata) {
        store.save(new ActivityDocument(eventType, actorUserId, entityType,
                String.valueOf(entityId), message, metadata));
    }

    public List<ActivityDocument> findRecent() {
        return store.findRecent();
    }
}
