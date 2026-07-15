package ie.nci.smartwaste.smartbin.repository;

import ie.nci.smartwaste.smartbin.model.SmartBin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SmartBinRepository {

    private final Map<String, SmartBin> bins = new ConcurrentHashMap<>();

    public boolean existsById(String binId) {
        return bins.containsKey(binId);
    }

    public void save(SmartBin smartBin) {
        bins.put(smartBin.getBinId(), smartBin);
    }

    public SmartBin findBinById(String binId) {
        return bins.get(binId);
    }

    public boolean updateFillLevel(String binId, int fillLevelPercentage) {
        SmartBin bin = bins.get(binId);

        if (bin == null) {
            return false;
        }

        bin.setFillLevelPercentage(fillLevelPercentage);
        return true;
    }

    public boolean reportDamage(String binId, String damageDescription) {
        SmartBin bin = bins.get(binId);

        if (bin == null) {
            return false;
        }

        bin.setDamaged(true);
        bin.setDamageDescription(damageDescription);
        return true;
    }
}