package rw.ac.auca.tapwallet.document;

import java.util.List;

public interface ActivityDocumentStore {
    ActivityDocument save(ActivityDocument document);
    List<ActivityDocument> findRecent();
}
