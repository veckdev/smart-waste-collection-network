package ie.nci.smartwaste.collection.client;

import ie.nci.smartwaste.collection.AssignCollectionRequest;
import ie.nci.smartwaste.collection.AssignCollectionResponse;
import ie.nci.smartwaste.collection.CollectionServiceGrpc;
import ie.nci.smartwaste.discovery.DiscoveredService;
import ie.nci.smartwaste.discovery.ServiceDiscovery;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CollectionClient {

    private static final String SERVICE_TYPE =
            "_collection._tcp.local.";

    private static final String SERVICE_NAME =
            "Collection Service";

    private final ManagedChannel channel;

    private final CollectionServiceGrpc.CollectionServiceBlockingStub
            blockingStub;

    public CollectionClient()
            throws IOException, InterruptedException {

        ServiceDiscovery serviceDiscovery =
                new ServiceDiscovery();

        DiscoveredService discoveredService =
                serviceDiscovery.discoverService(
                        SERVICE_TYPE,
                        SERVICE_NAME
                );

        channel = ManagedChannelBuilder
                .forAddress(
                        discoveredService.getHost(),
                        discoveredService.getPort()
                )
                .usePlaintext()
                .build();

        blockingStub =
                CollectionServiceGrpc.newBlockingStub(channel);
    }

    public void assignCollection() {

        AssignCollectionRequest request =
                AssignCollectionRequest.newBuilder()
                        .setCollectionId("COL-001")
                        .setBinId("BIN-001")
                        .setVehicleId("TRUCK-001")
                        .setScheduledDate("2026-07-20")
                        .build();

        try {

            AssignCollectionResponse response =
                    blockingStub.assignCollection(request);

            System.out.println();
            System.out.println("=== Assign Collection ===");
            System.out.println(
                    "Success: " + response.getSuccess()
            );
            System.out.println(
                    "Message: " + response.getMessage()
            );

        } catch (StatusRuntimeException exception) {

            System.err.println(
                    "Assign Collection RPC failed: "
                            + exception.getStatus().getDescription()
            );
        }
    }

    public void shutdown() throws InterruptedException {

        channel.shutdown()
                .awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {

        CollectionClient client = null;

        try {

            client = new CollectionClient();
            client.assignCollection();

        } catch (IOException | InterruptedException exception) {

            System.err.println(
                    "Could not discover Collection Service: "
                            + exception.getMessage()
            );

            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

        } finally {

            if (client != null) {

                try {

                    client.shutdown();

                } catch (InterruptedException exception) {

                    Thread.currentThread().interrupt();

                    System.err.println(
                            "Client shutdown was interrupted."
                    );
                }
            }
        }
    }
}