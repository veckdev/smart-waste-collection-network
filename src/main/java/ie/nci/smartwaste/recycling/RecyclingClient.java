package ie.nci.smartwaste.recycling;

import ie.nci.smartwaste.discovery.DiscoveredService;
import ie.nci.smartwaste.discovery.ServiceDiscovery;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RecyclingClient {

    private static final String SERVICE_TYPE =
            "_recycling._tcp.local.";

    private static final String SERVICE_NAME =
            "Recycling Center Service";

    private final ManagedChannel channel;

    private final RecyclingCenterServiceGrpc
            .RecyclingCenterServiceBlockingStub blockingStub;

    public RecyclingClient()
            throws IOException, InterruptedException {

        ServiceDiscovery discovery =
                new ServiceDiscovery();

        DiscoveredService service =
                discovery.discoverService(
                        SERVICE_TYPE,
                        SERVICE_NAME
                );

        channel = ManagedChannelBuilder
                .forAddress(
                        service.getHost(),
                        service.getPort()
                )
                .usePlaintext()
                .build();

        blockingStub =
                RecyclingCenterServiceGrpc
                        .newBlockingStub(channel);
    }

    public boolean receiveWaste() {

        ReceiveWasteRequest request =
                ReceiveWasteRequest.newBuilder()
                        .setDeliveryId("DEL-001")
                        .setCollectionId("COL-001")
                        .setWasteType("General Waste")
                        .setAmountLitres(180)
                        .build();

        try {

            ReceiveWasteResponse response =
                    blockingStub.receiveWaste(request);

            System.out.println();
            System.out.println("=== Receive Waste ===");
            System.out.println(
                    "Success: " + response.getSuccess()
            );
            System.out.println(
                    "Message: " + response.getMessage()
            );

            return response.getSuccess();

        } catch (StatusRuntimeException exception) {

            System.err.println(
                    "Receive Waste RPC failed: "
                            + exception.getStatus().getDescription()
            );

            return false;
        }
    }

    public void getCenterCapacity() {

        GetCenterCapacityResponse response =
                blockingStub.getCenterCapacity(
                        GetCenterCapacityRequest
                                .newBuilder()
                                .setCenterId("CENTER-001")
                                .build()
                );

        System.out.println();
        System.out.println("=== Recycling Center Capacity ===");
        System.out.println(
                "Center: " + response.getCenterId()
        );
        System.out.println(
                "Maximum capacity: "
                        + response.getMaximumCapacityLitres()
                        + " litres"
        );
        System.out.println(
                "Current volume: "
                        + response.getCurrentVolumeLitres()
                        + " litres"
        );
        System.out.println(
                "Available capacity: "
                        + response.getAvailableCapacityLitres()
                        + " litres"
        );
    }

    public void processWaste() {

        ProcessWasteResponse response =
                blockingStub.processWaste(
                        ProcessWasteRequest
                                .newBuilder()
                                .setDeliveryId("DEL-001")
                                .setProcessedAmountLitres(100)
                                .build()
                );

        System.out.println();
        System.out.println("=== Process Waste ===");
        System.out.println(
                "Success: " + response.getSuccess()
        );
        System.out.println(
                "Message: " + response.getMessage()
        );
    }

    public void shutdown()
            throws InterruptedException {

        channel.shutdown()
                .awaitTermination(
                        5,
                        TimeUnit.SECONDS
                );
    }

    public static void main(String[] args) {

        RecyclingClient client = null;

        try {

            client = new RecyclingClient();

            boolean received =
                    client.receiveWaste();

            if (received) {

                client.getCenterCapacity();
                client.processWaste();

            } else {

                System.out.println();
                System.out.println(
                        "Recycling process stopped."
                );
            }

        } catch (IOException | InterruptedException exception) {

            System.err.println(
                    exception.getMessage()
            );

            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

        } finally {

            if (client != null) {

                try {
                    client.shutdown();

                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}