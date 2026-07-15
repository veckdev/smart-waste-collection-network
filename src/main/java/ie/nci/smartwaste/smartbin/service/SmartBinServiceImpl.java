package ie.nci.smartwaste.smartbin.service;

import ie.nci.smartwaste.smartbin.GetBinStatusRequest;
import ie.nci.smartwaste.smartbin.GetBinStatusResponse;
import ie.nci.smartwaste.smartbin.RegisterBinRequest;
import ie.nci.smartwaste.smartbin.RegisterBinResponse;
import ie.nci.smartwaste.smartbin.ReportDamageRequest;
import ie.nci.smartwaste.smartbin.ReportDamageResponse;
import ie.nci.smartwaste.smartbin.SmartBinServiceGrpc;
import ie.nci.smartwaste.smartbin.UpdateFillLevelRequest;
import ie.nci.smartwaste.smartbin.UpdateFillLevelResponse;
import ie.nci.smartwaste.smartbin.model.SmartBin;
import ie.nci.smartwaste.smartbin.repository.SmartBinRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class SmartBinServiceImpl
        extends SmartBinServiceGrpc.SmartBinServiceImplBase {

    private final SmartBinRepository repository =
            new SmartBinRepository();

    @Override
    public void registerBin(
            RegisterBinRequest request,
            StreamObserver<RegisterBinResponse> responseObserver
    ) {
        String binId = request.getBinId().trim();

        if (binId.isEmpty()) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Bin ID cannot be empty.")
                            .asRuntimeException()
            );
            return;
        }

        if (repository.existsById(binId)) {
            RegisterBinResponse response =
                    RegisterBinResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("A bin with this ID already exists.")
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        SmartBin smartBin = new SmartBin(
                binId,
                request.getLocation(),
                request.getWasteType(),
                request.getCapacityLitres()
        );

        repository.save(smartBin);

        RegisterBinResponse response =
                RegisterBinResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Smart bin registered successfully.")
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateFillLevel(
            UpdateFillLevelRequest request,
            StreamObserver<UpdateFillLevelResponse> responseObserver
    ) {
        int fillLevel = request.getFillLevelPercentage();

        if (fillLevel < 0 || fillLevel > 100) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription(
                                    "Fill level must be between 0 and 100."
                            )
                            .asRuntimeException()
            );
            return;
        }

        boolean updated = repository.updateFillLevel(
                request.getBinId(),
                fillLevel
        );

        if (!updated) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Smart bin not found.")
                            .asRuntimeException()
            );
            return;
        }

        UpdateFillLevelResponse response =
                UpdateFillLevelResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Fill level updated successfully.")
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getBinStatus(
            GetBinStatusRequest request,
            StreamObserver<GetBinStatusResponse> responseObserver
    ) {
        SmartBin smartBin =
                repository.findBinById(request.getBinId());

        if (smartBin == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Smart bin not found.")
                            .asRuntimeException()
            );
            return;
        }

        GetBinStatusResponse response =
                GetBinStatusResponse.newBuilder()
                        .setBinId(smartBin.getBinId())
                        .setLocation(smartBin.getLocation())
                        .setWasteType(smartBin.getWasteType())
                        .setCapacityLitres(
                                smartBin.getCapacityLitres()
                        )
                        .setFillLevelPercentage(
                                smartBin.getFillLevelPercentage()
                        )
                        .setDamaged(smartBin.isDamaged())
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void reportDamage(
            ReportDamageRequest request,
            StreamObserver<ReportDamageResponse> responseObserver
    ) {
        SmartBin smartBin =
                repository.findBinById(request.getBinId());

        if (smartBin == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Smart bin not found.")
                            .asRuntimeException()
            );
            return;
        }

        smartBin.setDamaged(true);
        smartBin.setDamageDescription(
                request.getDamageDescription()
        );

        ReportDamageResponse response =
                ReportDamageResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage(
                                "Damage report recorded successfully."
                        )
                        .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}