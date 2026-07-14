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
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import ie.nci.smartwaste.smartbin.model.SmartBin;
import ie.nci.smartwaste.smartbin.repository.SmartBinRepository;

public class SmartBinServiceImpl extends SmartBinServiceGrpc.SmartBinServiceImplBase {

    private final SmartBinRepository smartBinRepository = new SmartBinRepository();

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

        if (smartBinRepository.existsById(binId)) {
            responseObserver.onNext(
                    RegisterBinResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("A bin with this ID already exists.")
                            .build()
            );
            responseObserver.onCompleted();
            return;
        }

        SmartBin bin = new SmartBin(
                binId,
                request.getLocation(),
                request.getWasteType(),
                request.getCapacityLitres()
        );

        smartBinRepository.save(bin);

        responseObserver.onNext(
                RegisterBinResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Smart bin registered successfully.")
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void updateFillLevel(
            UpdateFillLevelRequest request,
            StreamObserver<UpdateFillLevelResponse> responseObserver
    ) {
        SmartBin bin = smartBinRepository.findById(request.getBinId());

        if (bin == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Smart bin not found.")
                            .asRuntimeException()
            );
            return;
        }

        int fillLevel = request.getFillLevelPercentage();

        if (fillLevel < 0 || fillLevel > 100) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Fill level must be between 0 and 100.")
                            .asRuntimeException()
            );
            return;
        }

        bin.setFillLevelPercentage(fillLevel);

        responseObserver.onNext(
                UpdateFillLevelResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Fill level updated successfully.")
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void getBinStatus(
            GetBinStatusRequest request,
            StreamObserver<GetBinStatusResponse> responseObserver
    ) {
        SmartBin bin = smartBinRepository.findById(request.getBinId());

        if (bin == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Smart bin not found.")
                            .asRuntimeException()
            );
            return;
        }

        responseObserver.onNext(
                GetBinStatusResponse.newBuilder()
                        .setBinId(bin.getBinId())
                        .setLocation(bin.getLocation())
                        .setWasteType(bin.getWasteType())
                        .setCapacityLitres(bin.getCapacityLitres())
                        .setFillLevelPercentage(bin.getFillLevelPercentage())
                        .setDamaged(bin.isDamaged())
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void reportDamage(
            ReportDamageRequest request,
            StreamObserver<ReportDamageResponse> responseObserver
    ) {
        SmartBin bin = smartBinRepository.findById(request.getBinId());

        if (bin == null) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Smart bin not found.")
                            .asRuntimeException()
            );
            return;
        }

        bin.setDamaged(true);
        bin.setDamageDescription(request.getDamageDescription());

        responseObserver.onNext(
                ReportDamageResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Damage report recorded successfully.")
                        .build()
        );
        responseObserver.onCompleted();
    }

    private static class SmartBinRecord {

        private final String binId;
        private final String location;
        private final String wasteType;
        private final int capacityLitres;

        private int fillLevelPercentage;
        private boolean damaged;
        private String damageDescription;

        private SmartBinRecord(
                String binId,
                String location,
                String wasteType,
                int capacityLitres
        ) {
            this.binId = binId;
            this.location = location;
            this.wasteType = wasteType;
            this.capacityLitres = capacityLitres;
            this.fillLevelPercentage = 0;
            this.damaged = false;
            this.damageDescription = "";
        }

        public String getBinId() {
            return binId;
        }

        public String getLocation() {
            return location;
        }

        public String getWasteType() {
            return wasteType;
        }

        public int getCapacityLitres() {
            return capacityLitres;
        }

        public int getFillLevelPercentage() {
            return fillLevelPercentage;
        }

        public void setFillLevelPercentage(int fillLevelPercentage) {
            this.fillLevelPercentage = fillLevelPercentage;
        }

        public boolean isDamaged() {
            return damaged;
        }

        public void setDamaged(boolean damaged) {
            this.damaged = damaged;
        }

        public void setDamageDescription(String damageDescription) {
            this.damageDescription = damageDescription;
        }
    }
}