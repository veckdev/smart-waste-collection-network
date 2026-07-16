package ie.nci.smartwaste.collection.repository;

import ie.nci.smartwaste.collection.model.CollectionTask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CollectionRepository {

    private final Map<String, CollectionTask> collections =
            new ConcurrentHashMap<>();

    public boolean existsById(String collectionId) {
        return collections.containsKey(collectionId);
    }

    public void save(CollectionTask task) {
        collections.put(task.getCollectionId(), task);
    }

    public CollectionTask findById(String collectionId) {
        return collections.get(collectionId);
    }

    public boolean completeCollection(
            String collectionId,
            int collectedAmountLitres
    ) {
        CollectionTask task = collections.get(collectionId);

        if (task == null) {
            return false;
        }

        task.complete(collectedAmountLitres);
        return true;
    }

    public List<CollectionTask> findByVehicleId(String vehicleId) {
        return collections.values()
                .stream()
                .filter(task -> task.getVehicleId().equals(vehicleId))
                .toList();
    }
}