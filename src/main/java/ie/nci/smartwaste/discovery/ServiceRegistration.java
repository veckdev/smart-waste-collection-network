package ie.nci.smartwaste.discovery;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;

public class ServiceRegistration {

    private JmDNS jmdns;
    private ServiceInfo serviceInfo;

    public void registerService(
            String serviceType,
            String serviceName,
            int port,
            String description
    ) throws IOException {

        InetAddress localAddress = InetAddress.getLocalHost();

        jmdns = JmDNS.create(localAddress);

        serviceInfo = ServiceInfo.create(
                serviceType,
                serviceName,
                port,
                description
        );

        jmdns.registerService(serviceInfo);

        System.out.println("Service registered with jmDNS");
        System.out.println("Name: " + serviceName);
        System.out.println("Type: " + serviceType);
        System.out.println("Address: " + localAddress.getHostAddress());
        System.out.println("Port: " + port);
    }

    public void unregisterService() {
        if (jmdns != null) {
            if (serviceInfo != null) {
                jmdns.unregisterService(serviceInfo);
            }

            try {
                jmdns.close();
            } catch (IOException exception) {
                System.err.println(
                        "Could not close jmDNS: "
                                + exception.getMessage()
                );
            }
        }
    }
}