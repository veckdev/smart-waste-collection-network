package ie.nci.smartwaste.collection.model;

public class CollectionTask {

    private final String collectionId;
    private final String binId;
    private final String vehicleId;
    private final String scheduledDate;

    private boolean completed;
    private int collectedAmountLitres;

    public CollectionTask(
            String collectionId,
            String binId,
            String vehicleId,
            String scheduledDate
    ) {
        this.collectionId = collectionId;
        this.binId = binId;
        this.vehicleId = vehicleId;
        this.scheduledDate = scheduledDate;
        this.completed = false;
        this.collectedAmountLitres = 0;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public String getBinId() {
        return binId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getCollectedAmountLitres() {
        return collectedAmountLitres;
    }

    public void complete(int collectedAmountLitres) {
        this.completed = true;
        this.collectedAmountLitres = collectedAmountLitres;
    }
}