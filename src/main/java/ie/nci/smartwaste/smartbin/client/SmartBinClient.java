package ie.nci.smartwaste.smartbin.client;

import ie.nci.smartwaste.discovery.DiscoveredService;
import ie.nci.smartwaste.discovery.ServiceDiscovery;
import ie.nci.smartwaste.smartbin.BinsNeedingCollectionRequest;
import ie.nci.smartwaste.smartbin.GetBinStatusRequest;
import ie.nci.smartwaste.smartbin.GetBinStatusResponse;
import ie.nci.smartwaste.smartbin.RegisterBinRequest;
import ie.nci.smartwaste.smartbin.RegisterBinResponse;
import ie.nci.smartwaste.smartbin.ReportDamageRequest;
import ie.nci.smartwaste.smartbin.ReportDamageResponse;
import ie.nci.smartwaste.smartbin.SmartBinServiceGrpc;
import ie.nci.smartwaste.smartbin.UpdateFillLevelRequest;
import ie.nci.smartwaste.smartbin.UpdateFillLevelResponse;
import ie.nci.smartwaste.smartbin.CollectionUpdate;
import ie.nci.smartwaste.smartbin.CollectionFeedback;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;

public class SmartBinClient {

    private static final String SERVICE_TYPE =
            "_smartbin._tcp.local.";

    private static final String SERVICE_NAME =
            "Smart Bin Service";

    private final ManagedChannel channel;
    private final SmartBinServiceGrpc.SmartBinServiceBlockingStub blockingStub;
    private final SmartBinServiceGrpc.SmartBinServiceStub asyncStub;

    public SmartBinClient() throws IOException, InterruptedException {

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

        blockingStub = SmartBinServiceGrpc.newBlockingStub(channel);
        asyncStub = SmartBinServiceGrpc.newStub(channel);
    }

    private void registerBin(
            String binId,
            String location,
            String wasteType,
            int capacityLitres
    ) {
        RegisterBinRequest request =
                RegisterBinRequest.newBuilder()
                        .setBinId(binId)
                        .setLocation(location)
                        .setWasteType(wasteType)
                        .setCapacityLitres(capacityLitres)
                        .build();

        try {
            RegisterBinResponse response =
                    blockingStub.registerBin(request);

            System.out.println();
            System.out.println("=== Register Bin " + binId + " ===");
            System.out.println("Success: " + response.getSuccess());
            System.out.println("Message: " + response.getMessage());

        } catch (StatusRuntimeException exception) {
            System.err.println(
                    "Register Bin RPC failed for "
                            + binId
                            + ": "
                            + exception.getStatus().getDescription()
            );
        }
    }

    private void updateFillLevel(
            String binId,
            int fillLevelPercentage
    ) {
        UpdateFillLevelRequest request =
                UpdateFillLevelRequest.newBuilder()
                        .setBinId(binId)
                        .setFillLevelPercentage(fillLevelPercentage)
                        .build();

        try {
            UpdateFillLevelResponse response =
                    blockingStub.updateFillLevel(request);

            System.out.println();
            System.out.println(
                    "=== Update Fill Level " + binId + " ==="
            );
            System.out.println("Success: " + response.getSuccess());
            System.out.println("Message: " + response.getMessage());

        } catch (StatusRuntimeException exception) {
            System.err.println(
                    "Update Fill Level RPC failed for "
                            + binId
                            + ": "
                            + exception.getStatus().getDescription()
            );
        }
    }

    public void reportDamage() {
        ReportDamageRequest request =
                ReportDamageRequest.newBuilder()
                        .setBinId("BIN-001")
                        .setDamageDescription("Broken lid")
                        .build();

        try {
            ReportDamageResponse response =
                    blockingStub.reportDamage(request);

            System.out.println();
            System.out.println("=== Report Damage ===");
            System.out.println("Success: " + response.getSuccess());
            System.out.println("Message: " + response.getMessage());

        } catch (StatusRuntimeException exception) {
            System.err.println(
                    "Report Damage RPC failed: "
                            + exception.getStatus().getDescription()
            );
        }
    }

    public void getBinStatus() {
        GetBinStatusRequest request =
                GetBinStatusRequest.newBuilder()
                        .setBinId("BIN-001")
                        .build();

        try {
            GetBinStatusResponse response =
                    blockingStub.getBinStatus(request);

            System.out.println();
            System.out.println("=== Smart Bin Status ===");
            System.out.println("Bin ID: " + response.getBinId());
            System.out.println("Location: " + response.getLocation());
            System.out.println("Waste type: " + response.getWasteType());
            System.out.println(
                    "Capacity: "
                            + response.getCapacityLitres()
                            + " litres"
            );
            System.out.println(
                    "Fill level: "
                            + response.getFillLevelPercentage()
                            + "%"
            );
            System.out.println("Damaged: " + response.getDamaged());

        } catch (StatusRuntimeException exception) {
            System.err.println(
                    "Get Bin Status RPC failed: "
                            + exception.getStatus().getDescription()
            );
        }
    }

    public void prepareStreamingDemo() {
        registerBin(
                "BIN-001",
                "O'Connell Street, Dublin",
                "General Waste",
                240
        );

        registerBin(
                "BIN-002",
                "Grafton Street, Dublin",
                "Recycling",
                240
        );

        registerBin(
                "BIN-003",
                "Temple Bar, Dublin",
                "General Waste",
                360
        );

        registerBin(
                "BIN-004",
                "Parnell Square, Dublin",
                "Organic Waste",
                240
        );

        registerBin(
                "BIN-005",
                "Docklands, Dublin",
                "Recycling",
                360
        );

        updateFillLevel("BIN-001", 75);
        updateFillLevel("BIN-002", 40);
        updateFillLevel("BIN-003", 92);
        updateFillLevel("BIN-004", 65);
        updateFillLevel("BIN-005", 81);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown()
                .awaitTermination(5, TimeUnit.SECONDS);
    }

    public void streamBinsNeedingCollection() {

        BinsNeedingCollectionRequest request =
                BinsNeedingCollectionRequest.newBuilder()
                        .setMinimumFillLevel(70)
                        .build();

        try {

            Iterator<GetBinStatusResponse> responses =
                    blockingStub.streamBinsNeedingCollection(request);

            List<GetBinStatusResponse> damagedBins = new ArrayList<>();
            List<GetBinStatusResponse> collectionBins = new ArrayList<>();

            while (responses.hasNext()) {

                GetBinStatusResponse response = responses.next();

                if (response.getDamaged()) {
                    damagedBins.add(response);
                }

                collectionBins.add(response);
            }

            damagedBins.sort(
                    Comparator.comparingInt(
                            GetBinStatusResponse::getFillLevelPercentage
                    ).reversed()
            );

            collectionBins.sort(
                    Comparator.comparingInt(
                            GetBinStatusResponse::getFillLevelPercentage
                    ).reversed()
            );

            System.out.println();
            System.out.println("==================================================");
            System.out.println("SMART WASTE OPERATIONAL REPORT");
            System.out.println("==================================================");

            System.out.println();
            System.out.println("CRITICAL MAINTENANCE");
            System.out.println("--------------------------------------------------");

            if (damagedBins.isEmpty()) {

                System.out.println("No damaged bins.");

            } else {

                for (GetBinStatusResponse bin : damagedBins) {

                    System.out.println();
                    System.out.println("Bin ID        : " + bin.getBinId());
                    System.out.println("Location      : " + bin.getLocation());
                    System.out.println("Fill Level    : "
                            + bin.getFillLevelPercentage() + "%");
                    System.out.println("Status        : DAMAGED");
                    System.out.println("--------------------------------------------------");
                }
            }

            System.out.println();
            System.out.println("==================================================");

            System.out.println();
            System.out.println("COLLECTION PRIORITY");
            System.out.println("--------------------------------------------------");

            if (collectionBins.isEmpty()) {

                System.out.println("No bins require collection.");

            } else {

                int priority = 1;

                for (GetBinStatusResponse bin : collectionBins) {

                    System.out.println();
                    System.out.println("Priority #" + priority++);
                    System.out.println("--------------------------------------------------");
                    System.out.println("Bin ID        : " + bin.getBinId());
                    System.out.println("Location      : " + bin.getLocation());
                    System.out.println("Fill Level    : "
                            + bin.getFillLevelPercentage() + "%");
                    System.out.println("Waste Type    : " + bin.getWasteType());
                }
            }

            System.out.println();
            System.out.println("==================================================");

            System.out.println();
            System.out.println("SUMMARY");
            System.out.println();
            System.out.println("Damaged Bins          : "
                    + damagedBins.size());
            System.out.println("Bins Requiring Pickup : "
                    + collectionBins.size());

            System.out.println();
            System.out.println("==================================================");

        } catch (StatusRuntimeException exception) {

            System.err.println(
                    "Stream RPC failed: "
                            + exception.getStatus().getDescription()
            );
        }
    }

    public void monitorCollectionRoute() {

        CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<CollectionFeedback> responseObserver =
                new StreamObserver<CollectionFeedback>() {

                    @Override
                    public void onNext(CollectionFeedback response) {

                        System.out.println("--------------------------------");
                        System.out.println("Bin: " + response.getBinId());
                        System.out.println("Instruction: " + response.getInstruction());
                        System.out.println("Priority: " + response.getPriority());
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        finishLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println();
                        System.out.println("Collection route completed.");
                        finishLatch.countDown();
                    }
                };

        StreamObserver<CollectionUpdate> requestObserver =
                asyncStub.monitorCollectionRoute(responseObserver);

        requestObserver.onNext(
                CollectionUpdate.newBuilder()
                        .setTruckId("TRUCK-001")
                        .setBinId("BIN-001")
                        .setFillLevelPercentage(75)
                        .setCollected(true)
                        .setDamaged(false)
                        .build());

        requestObserver.onNext(
                CollectionUpdate.newBuilder()
                        .setTruckId("TRUCK-001")
                        .setBinId("BIN-003")
                        .setFillLevelPercentage(92)
                        .setCollected(true)
                        .setDamaged(false)
                        .build());

        requestObserver.onNext(
                CollectionUpdate.newBuilder()
                        .setTruckId("TRUCK-001")
                        .setBinId("BIN-005")
                        .setFillLevelPercentage(81)
                        .setCollected(true)
                        .setDamaged(false)
                        .build());

        requestObserver.onCompleted();

        try {
            finishLatch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {

        SmartBinClient client = null;

        try {
            client = new SmartBinClient();

            client.prepareStreamingDemo();
            client.reportDamage();
            client.getBinStatus();
            client.streamBinsNeedingCollection();

            System.out.println();
            System.out.println("=== Bidirectional Streaming Demo ===");
            client.monitorCollectionRoute();

        } catch (IOException | InterruptedException exception) {
            System.err.println(
                    "Could not discover Smart Bin Service: "
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