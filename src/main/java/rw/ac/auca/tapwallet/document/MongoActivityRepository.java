package rw.ac.auca.tapwallet.document;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoActivityRepository extends MongoRepository<ActivityDocument, String> {
    List<ActivityDocument> findTop100ByOrderByOccurredAtDesc();
}
