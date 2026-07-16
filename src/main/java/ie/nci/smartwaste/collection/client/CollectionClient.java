package ie.nci.smartwaste.collection.client;

import ie.nci.smartwaste.collection.AssignCollectionRequest;
import ie.nci.smartwaste.collection.AssignCollectionResponse;
import ie.nci.smartwaste.collection.CollectionServiceGrpc;
import ie.nci.smartwaste.collection.CollectionStop;
import ie.nci.smartwaste.collection.CompleteCollectionRequest;
import ie.nci.smartwaste.collection.CompleteCollectionResponse;
import ie.nci.smartwaste.collection.GetCollectionRouteRequest;
import ie.nci.smartwaste.collection.GetCollectionRouteResponse;
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

    public boolean assignCollection() {

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

            return response.getSuccess();

        } catch (StatusRuntimeException exception) {
            System.err.println(
                    "Assign Collection RPC failed: "
                            + exception.getStatus().getDescription()
            );

            return false;
        }
    }

    public void completeCollection() {

        CompleteCollectionRequest request =
                CompleteCollectionRequest.newBuilder()
                        .setCollectionId("COL-001")
                        .setCollectedAmountLitres(180)
                        .build();

        try {
            CompleteCollectionResponse response =
                    blockingStub.completeCollection(request);

            System.out.println();
            System.out.println("=== Complete Collection ===");
            System.out.println(
                    "Success: " + response.getSuccess()
            );
            System.out.println(
                    "Message: " + response.getMessage()
            );

        } catch (StatusRuntimeException exception) {
            System.err.println(
                    "Complete Collection RPC failed: "
                            + exception.getStatus().getDescription()
            );
        }
    }

    public void getCollectionRoute() {

        GetCollectionRouteRequest request =
                GetCollectionRouteRequest.newBuilder()
                        .setVehicleId("TRUCK-001")
                        .build();

        try {
            GetCollectionRouteResponse response =
                    blockingStub.getCollectionRoute(request);

            System.out.println();
            System.out.println("=== Collection Route ===");
            System.out.println(
                    "Vehicle ID: " + response.getVehicleId()
            );
            System.out.println(
                    "Number of stops: " + response.getStopsCount()
            );

            for (CollectionStop stop : response.getStopsList()) {
                System.out.println();
                System.out.println(
                        "Collection ID: " + stop.getCollectionId()
                );
                System.out.println(
                        "Bin ID: " + stop.getBinId()
                );
                System.out.println(
                        "Scheduled date: " + stop.getScheduledDate()
                );
                System.out.println(
                        "Completed: " + stop.getCompleted()
                );
            }

        } catch (StatusRuntimeException exception) {
            System.err.println(
                    "Get Collection Route RPC failed: "
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

            boolean collectionAssigned =
                    client.assignCollection();

            if (collectionAssigned) {
                client.completeCollection();
                client.getCollectionRoute();

            } else {
                System.out.println();
                System.out.println(
                        "Collection process stopped because the assignment failed."
                );
            }

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