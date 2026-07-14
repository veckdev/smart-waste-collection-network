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

    public SmartBin findById(String binId) {
        return bins.get(binId);
    }
}