package ie.nci.smartwaste.recycling;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class RecyclingServiceImpl
        extends RecyclingCenterServiceGrpc.RecyclingCenterServiceImplBase {

    private final RecyclingRepository repository =
            new RecyclingRepository();

    @Override
    public void receiveWaste(
            ReceiveWasteRequest request,
            StreamObserver<ReceiveWasteResponse> responseObserver
    ) {

        String deliveryId = request.getDeliveryId().trim();

        if (deliveryId.isEmpty()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Delivery ID cannot be empty.")
                            .asRuntimeException()
            );
            return;
        }

        if (repository.existsById(deliveryId)) {

            responseObserver.onNext(
                    ReceiveWasteResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("A delivery with this ID already exists.")
                            .build()
            );

            responseObserver.onCompleted();
            return;
        }

        WasteDelivery delivery = new WasteDelivery(
                deliveryId,
                request.getCollectionId(),
                request.getWasteType(),
                request.getAmountLitres()
        );

        repository.save(delivery);

        responseObserver.onNext(
                ReceiveWasteResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Waste received successfully.")
                        .build()
        );

        responseObserver.onCompleted();
    }

    @Override
    public void getCenterCapacity(
            GetCenterCapacityRequest request,
            StreamObserver<GetCenterCapacityResponse> responseObserver
    ) {

        GetCenterCapacityResponse response =
                GetCenterCapacityResponse.newBuilder()
                        .setCenterId(repository.getCenterId())
                        .setMaximumCapacityLitres(
                                repository.getMaximumCapacityLitres())
                        .setCurrentVolumeLitres(
                                repository.getCurrentVolumeLitres())
                        .setAvailableCapacityLitres(
                                repository.getAvailableCapacityLitres())
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void processWaste(
            ProcessWasteRequest request,
            StreamObserver<ProcessWasteResponse> responseObserver
    ) {

        boolean success = repository.processWaste(
                request.getDeliveryId(),
                request.getProcessedAmountLitres()
        );

        ProcessWasteResponse response =
                ProcessWasteResponse.newBuilder()
                        .setSuccess(success)
                        .setMessage(
                                success
                                        ? "Waste processed successfully."
                                        : "Delivery not found or invalid amount."
                        )
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}