package ie.nci.smartwaste.collection.integration;

import ie.nci.smartwaste.discovery.DiscoveredService;
import ie.nci.smartwaste.discovery.ServiceDiscovery;
import ie.nci.smartwaste.recycling.ReceiveWasteRequest;
import ie.nci.smartwaste.recycling.ReceiveWasteResponse;
import ie.nci.smartwaste.recycling.RecyclingCenterServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RecyclingServiceClient {

    private static final String SERVICE_TYPE =
            "_recycling._tcp.local.";

    private static final String SERVICE_NAME =
            "Recycling Center Service";

    public boolean sendWaste(
            String deliveryId,
            String collectionId,
            String wasteType,
            int amountLitres
    ) throws IOException, InterruptedException {

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
            RecyclingCenterServiceGrpc
                    .RecyclingCenterServiceBlockingStub stub =
                    RecyclingCenterServiceGrpc
                            .newBlockingStub(channel);

            ReceiveWasteRequest request =
                    ReceiveWasteRequest.newBuilder()
                            .setDeliveryId(deliveryId)
                            .setCollectionId(collectionId)
                            .setWasteType(wasteType)
                            .setAmountLitres(amountLitres)
                            .build();

            ReceiveWasteResponse response =
                    stub.receiveWaste(request);

            return response.getSuccess();

        } catch (StatusRuntimeException exception) {
            System.err.println(
                    "Receive Waste RPC failed: "
                            + exception.getStatus().getDescription()
            );

            return false;

        } finally {
            channel.shutdown()
                    .awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}