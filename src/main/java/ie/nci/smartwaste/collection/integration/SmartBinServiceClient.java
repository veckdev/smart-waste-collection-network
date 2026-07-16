package ie.nci.smartwaste.collection.integration;

import ie.nci.smartwaste.discovery.DiscoveredService;
import ie.nci.smartwaste.discovery.ServiceDiscovery;
import ie.nci.smartwaste.smartbin.GetBinStatusRequest;
import ie.nci.smartwaste.smartbin.SmartBinServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SmartBinServiceClient {

    private static final String SERVICE_TYPE =
            "_smartbin._tcp.local.";

    private static final String SERVICE_NAME =
            "Smart Bin Service";

    public boolean binExists(String binId)
            throws IOException, InterruptedException {

        ServiceDiscovery serviceDiscovery =
                new ServiceDiscovery();

        DiscoveredService discoveredService =
                serviceDiscovery.discoverService(
                        SERVICE_TYPE,
                        SERVICE_NAME
                );

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(
                        discoveredService.getHost(),
                        discoveredService.getPort()
                )
                .usePlaintext()
                .build();

        try {
            SmartBinServiceGrpc.SmartBinServiceBlockingStub stub =
                    SmartBinServiceGrpc.newBlockingStub(channel);

            GetBinStatusRequest request =
                    GetBinStatusRequest.newBuilder()
                            .setBinId(binId)
                            .build();

            stub.getBinStatus(request);
            return true;

        } catch (StatusRuntimeException exception) {
            if (exception.getStatus().getCode()
                    == Status.Code.NOT_FOUND) {
                return false;
            }

            throw exception;

        } finally {
            channel.shutdown()
                    .awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}