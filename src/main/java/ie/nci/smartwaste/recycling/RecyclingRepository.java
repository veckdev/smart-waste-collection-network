package ie.nci.smartwaste.recycling;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RecyclingRepository {

    private final Map<String, WasteDelivery> deliveries =
            new ConcurrentHashMap<>();

    private static final String CENTER_ID = "CENTER-001";
    private static final int MAXIMUM_CAPACITY_LITRES = 10000;

    public boolean existsById(String deliveryId) {
        return deliveries.containsKey(deliveryId);
    }

    public void save(WasteDelivery delivery) {
        deliveries.put(delivery.getDeliveryId(), delivery);
    }

    public WasteDelivery findById(String deliveryId) {
        return deliveries.get(deliveryId);
    }

    public int getCurrentVolumeLitres() {

        return deliveries.values()
                .stream()
                .mapToInt(WasteDelivery::getRemainingAmountLitres)
                .sum();
    }

    public int getAvailableCapacityLitres() {
        return MAXIMUM_CAPACITY_LITRES
                - getCurrentVolumeLitres();
    }

    public String getCenterId() {
        return CENTER_ID;
    }

    public int getMaximumCapacityLitres() {
        return MAXIMUM_CAPACITY_LITRES;
    }

    public boolean processWaste(
            String deliveryId,
            int processedAmountLitres
    ) {

        WasteDelivery delivery = deliveries.get(deliveryId);

        if (delivery == null) {
            return false;
        }

        return delivery.processWaste(
                processedAmountLitres
        );
    }
}