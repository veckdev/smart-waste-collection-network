package ie.nci.smartwaste.discovery;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServiceDiscovery {

    private static final int DISCOVERY_TIMEOUT_SECONDS = 5;

    public DiscoveredService discoverService(
            String serviceType,
            String serviceName
    ) throws IOException, InterruptedException {

        InetAddress localAddress = InetAddress.getLocalHost();
        CountDownLatch serviceFoundLatch = new CountDownLatch(1);
        AtomicBoolean serviceResolved = new AtomicBoolean(false);

        DiscoveredService[] discoveredService =
                new DiscoveredService[1];

        try (JmDNS jmdns = JmDNS.create(localAddress)) {

            ServiceListener listener = new ServiceListener() {

                @Override
                public void serviceAdded(ServiceEvent event) {
                    jmdns.requestServiceInfo(
                            event.getType(),
                            event.getName(),
                            true
                    );
                }

                @Override
                public void serviceRemoved(ServiceEvent event) {
                    System.out.println(
                            "Service removed: " + event.getName()
                    );
                }

                @Override
                public void serviceResolved(ServiceEvent event) {
                    ServiceInfo serviceInfo = event.getInfo();

                    if (!serviceName.equals(serviceInfo.getName())) {
                        return;
                    }

                    if (!serviceResolved.compareAndSet(false, true)) {
                        return;
                    }

                    String[] addresses =
                            serviceInfo.getHostAddresses();

                    if (addresses.length == 0) {
                        serviceResolved.set(false);
                        return;
                    }

                    discoveredService[0] = new DiscoveredService(
                            addresses[0],
                            serviceInfo.getPort()
                    );

                    System.out.println("Service discovered with jmDNS");
                    System.out.println(
                            "Name: " + serviceInfo.getName()
                    );
                    System.out.println(
                            "Address: " + addresses[0]
                    );
                    System.out.println(
                            "Port: " + serviceInfo.getPort()
                    );

                    serviceFoundLatch.countDown();
                }
            };

            jmdns.addServiceListener(serviceType, listener);

            boolean found = serviceFoundLatch.await(
                    DISCOVERY_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );

            jmdns.removeServiceListener(serviceType, listener);

            if (!found || discoveredService[0] == null) {
                throw new IllegalStateException(
                        "Service not found: " + serviceName
                );
            }

            return discoveredService[0];
        }
    }
}