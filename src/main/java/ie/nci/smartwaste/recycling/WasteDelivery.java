package ie.nci.smartwaste.recycling;

public class WasteDelivery {

    private final String deliveryId;
    private final String collectionId;
    private final String wasteType;
    private final int amountLitres;

    private int processedAmountLitres;

    public WasteDelivery(
            String deliveryId,
            String collectionId,
            String wasteType,
            int amountLitres
    ) {
        this.deliveryId = deliveryId;
        this.collectionId = collectionId;
        this.wasteType = wasteType;
        this.amountLitres = amountLitres;
        this.processedAmountLitres = 0;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public String getWasteType() {
        return wasteType;
    }

    public int getAmountLitres() {
        return amountLitres;
    }

    public int getProcessedAmountLitres() {
        return processedAmountLitres;
    }

    public int getRemainingAmountLitres() {
        return amountLitres - processedAmountLitres;
    }

    public boolean processWaste(int processedAmountLitres) {
        if (processedAmountLitres <= 0
                || processedAmountLitres > getRemainingAmountLitres()) {
            return false;
        }

        this.processedAmountLitres += processedAmountLitres;
        return true;
    }
}