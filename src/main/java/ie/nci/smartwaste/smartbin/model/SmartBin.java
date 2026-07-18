package ie.nci.smartwaste.smartbin.model;


public class SmartBin {

    private final String binId;
    private final String location;
    private final String wasteType;
    private final int capacityLitres;

    private int fillLevelPercentage;
    private boolean damaged;
    private String damageDescription;

    public SmartBin(
            String binId,
            String location,
            String wasteType,
            int capacityLitres
    ) {
        this.binId = binId;
        this.location = location;
        this.wasteType = wasteType;
        this.capacityLitres = capacityLitres;
        this.fillLevelPercentage = 0;
        this.damaged = false;
        this.damageDescription = "";
    }

    public String getBinId() {
        return binId;
    }

    public String getLocation() {
        return location;
    }

    public String getWasteType() {
        return wasteType;
    }

    public int getCapacityLitres() {
        return capacityLitres;
    }

    public int getFillLevelPercentage() {
        return fillLevelPercentage;
    }

    public void setFillLevelPercentage(int fillLevelPercentage) {
        this.fillLevelPercentage = fillLevelPercentage;
    }

    public boolean isDamaged() {
        return damaged;
    }

    public void setDamaged(boolean damaged) {
        this.damaged = damaged;
    }

    public String getDamageDescription() {
        return damageDescription;
    }

    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
    }
}