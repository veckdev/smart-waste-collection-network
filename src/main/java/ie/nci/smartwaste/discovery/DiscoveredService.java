package ie.nci.smartwaste.discovery;

public class DiscoveredService {

    private final String host;
    private final int port;

    public DiscoveredService(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}