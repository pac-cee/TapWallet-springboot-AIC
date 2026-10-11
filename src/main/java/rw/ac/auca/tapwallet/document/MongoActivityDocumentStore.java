package rw.ac.auca.tapwallet.document;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("mongodb")
public class MongoActivityDocumentStore implements ActivityDocumentStore {

    private final MongoActivityRepository repository;

    public MongoActivityDocumentStore(MongoActivityRepository repository) {
        this.repository = repository;
    }

    @Override
    public ActivityDocument save(ActivityDocument document) {
        return repository.save(document);
    }

    @Override
    public List<ActivityDocument> findRecent() {
        return repository.findTop100ByOrderByOccurredAtDesc();
    }
}
