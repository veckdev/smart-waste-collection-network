package ie.nci.smartwaste.collection.service;

import ie.nci.smartwaste.collection.AssignCollectionRequest;
import ie.nci.smartwaste.collection.AssignCollectionResponse;
import ie.nci.smartwaste.collection.CollectionServiceGrpc;
import ie.nci.smartwaste.collection.CollectionStop;
import ie.nci.smartwaste.collection.CompleteCollectionRequest;
import ie.nci.smartwaste.collection.CompleteCollectionResponse;
import ie.nci.smartwaste.collection.GetCollectionRouteRequest;
import ie.nci.smartwaste.collection.GetCollectionRouteResponse;
import ie.nci.smartwaste.collection.model.CollectionTask;
import ie.nci.smartwaste.collection.repository.CollectionRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.List;

public class CollectionServiceImpl
        extends CollectionServiceGrpc.CollectionServiceImplBase {

    private final CollectionRepository repository =
            new CollectionRepository();

    @Override
    public void assignCollection(
            AssignCollectionRequest request,
            StreamObserver<AssignCollectionResponse> responseObserver
    ) {
        String collectionId = request.getCollectionId().trim();

        if (collectionId.isEmpty()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(
                                    "Collection ID cannot be empty."
                            )
                            .asRuntimeException()
            );
            return;
        }

        if (repository.existsById(collectionId)) {
            AssignCollectionResponse response =
                    AssignCollectionResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage(
                                    "A collection with this ID already exists."
                            )
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        CollectionTask task = new CollectionTask(
                collectionId,
                request.getBinId(),
                request.getVehicleId(),
                request.getScheduledDate()
        );

        repository.save(task);

        AssignCollectionResponse response =
                AssignCollectionResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage(
                                "Collection assigned successfully."
                        )
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void completeCollection(
            CompleteCollectionRequest request,
            StreamObserver<CompleteCollectionResponse> responseObserver
    ) {
        int collectedAmountLitres =
                request.getCollectedAmountLitres();

        if (collectedAmountLitres < 0) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(
                                    "Collected amount cannot be negative."
                            )
                            .asRuntimeException()
            );
            return;
        }

        boolean completed = repository.completeCollection(
                request.getCollectionId(),
                collectedAmountLitres
        );

        if (!completed) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Collection not found.")
                            .asRuntimeException()
            );
            return;
        }

        CompleteCollectionResponse response =
                CompleteCollectionResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage(
                                "Collection completed successfully."
                        )
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getCollectionRoute(
            GetCollectionRouteRequest request,
            StreamObserver<GetCollectionRouteResponse> responseObserver
    ) {
        String vehicleId = request.getVehicleId().trim();

        if (vehicleId.isEmpty()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(
                                    "Vehicle ID cannot be empty."
                            )
                            .asRuntimeException()
            );
            return;
        }

        List<CollectionTask> tasks =
                repository.findByVehicleId(vehicleId);

        GetCollectionRouteResponse.Builder responseBuilder =
                GetCollectionRouteResponse.newBuilder()
                        .setVehicleId(vehicleId);

        for (CollectionTask task : tasks) {
            CollectionStop stop =
                    CollectionStop.newBuilder()
                            .setCollectionId(
                                    task.getCollectionId()
                            )
                            .setBinId(task.getBinId())
                            .setScheduledDate(
                                    task.getScheduledDate()
                            )
                            .setCompleted(task.isCompleted())
                            .build();

            responseBuilder.addStops(stop);
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}