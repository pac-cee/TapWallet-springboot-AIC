package rw.ac.auca.tapwallet.document;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("!mongodb")
public class InMemoryActivityDocumentStore implements ActivityDocumentStore {

    private final Map<String, ActivityDocument> documents = new ConcurrentHashMap<>();

    @Override
    public ActivityDocument save(ActivityDocument document) {
        documents.put(document.getId(), document);
        return document;
    }

    @Override
    public List<ActivityDocument> findRecent() {
        return documents.values().stream()
                .sorted(Comparator.comparing(ActivityDocument::getOccurredAt).reversed())
                .limit(100)
                .toList();
    }
}
