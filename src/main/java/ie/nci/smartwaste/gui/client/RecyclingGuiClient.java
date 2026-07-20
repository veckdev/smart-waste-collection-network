package ie.nci.smartwaste.gui.client;

import ie.nci.smartwaste.discovery.DiscoveredService;
import ie.nci.smartwaste.discovery.ServiceDiscovery;
import ie.nci.smartwaste.recycling.RecyclingCenterServiceGrpc;
import ie.nci.smartwaste.recycling.WasteDeliveryRequest;
import ie.nci.smartwaste.recycling.WasteDeliverySummaryResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class RecyclingGuiClient implements AutoCloseable {

    private static final String SERVICE_TYPE = "_recycling._tcp.local.";
    private static final String SERVICE_NAME = "Recycling Center Service";
    private static final int UPLOAD_TIMEOUT_SECONDS = 10;

    private final ManagedChannel channel;
    private final RecyclingCenterServiceGrpc.RecyclingCenterServiceStub asyncStub;

    public RecyclingGuiClient() throws IOException, InterruptedException {
        ServiceDiscovery discovery = new ServiceDiscovery();
        DiscoveredService service = discovery.discoverService(
                SERVICE_TYPE,
                SERVICE_NAME
        );

        channel = ManagedChannelBuilder
                .forAddress(service.getHost(), service.getPort())
                .usePlaintext()
                .build();

        asyncStub = RecyclingCenterServiceGrpc.newStub(channel);
    }

    public UploadResult uploadDeliveries(List<DeliveryUpload> deliveries)
            throws InterruptedException {
        if (deliveries.isEmpty()) {
            throw new IllegalArgumentException("The delivery batch cannot be empty.");
        }

        CountDownLatch completionLatch = new CountDownLatch(1);
        AtomicReference<UploadResult> resultReference = new AtomicReference<>();
        AtomicReference<Throwable> errorReference = new AtomicReference<>();

        StreamObserver<WasteDeliverySummaryResponse> responseObserver =
                new StreamObserver<>() {
                    @Override
                    public void onNext(WasteDeliverySummaryResponse response) {
                        resultReference.set(new UploadResult(
                                response.getDeliveriesProcessed(),
                                response.getTotalWeightKg(),
                                response.getAverageWeightKg(),
                                response.getMessage()
                        ));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        errorReference.set(throwable);
                        completionLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        completionLatch.countDown();
                    }
                };

        StreamObserver<WasteDeliveryRequest> requestObserver =
                asyncStub.uploadWasteDeliveries(responseObserver);

        for (DeliveryUpload delivery : deliveries) {
            requestObserver.onNext(
                    WasteDeliveryRequest.newBuilder()
                            .setTruckId(delivery.vehicleId())
                            .setWasteType(delivery.materialType())
                            .setWeightKg(delivery.weightKg())
                            .setOrigin(delivery.deliveryId())
                            .build()
            );
        }

        requestObserver.onCompleted();

        boolean completed = completionLatch.await(
                UPLOAD_TIMEOUT_SECONDS,
                TimeUnit.SECONDS
        );

        if (!completed) {
            throw new IllegalStateException("The delivery upload timed out.");
        }

        if (errorReference.get() != null) {
            throw new IllegalStateException(
                    "The recycling service rejected the delivery batch.",
                    errorReference.get()
            );
        }

        UploadResult result = resultReference.get();

        if (result == null) {
            throw new IllegalStateException("No delivery summary was received.");
        }

        return result;
    }

    @Override
    public void close() {
        channel.shutdown();

        try {
            if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                channel.shutdownNow();
            }
        } catch (InterruptedException exception) {
            channel.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public record DeliveryUpload(
            String deliveryId,
            String vehicleId,
            String materialType,
            double weightKg
    ) {
    }

    public record UploadResult(
            int deliveriesProcessed,
            double totalWeightKg,
            double averageWeightKg,
            String message
    ) {
    }
}
