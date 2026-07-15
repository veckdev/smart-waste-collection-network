package ie.nci.smartwaste.smartbin.client;

import ie.nci.smartwaste.discovery.DiscoveredService;
import ie.nci.smartwaste.discovery.ServiceDiscovery;
import ie.nci.smartwaste.smartbin.GetBinStatusRequest;
import ie.nci.smartwaste.smartbin.GetBinStatusResponse;
import ie.nci.smartwaste.smartbin.RegisterBinRequest;
import ie.nci.smartwaste.smartbin.RegisterBinResponse;
import ie.nci.smartwaste.smartbin.ReportDamageRequest;
import ie.nci.smartwaste.smartbin.ReportDamageResponse;
import ie.nci.smartwaste.smartbin.SmartBinServiceGrpc;
import ie.nci.smartwaste.smartbin.UpdateFillLevelRequest;
import ie.nci.smartwaste.smartbin.UpdateFillLevelResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SmartBinClient {

    private static final String SERVICE_TYPE =
            "_smartbin._tcp.local.";

    private static final String SERVICE_NAME =
            "Smart Bin Service";

    private final ManagedChannel channel;
    private final SmartBinServiceGrpc.SmartBinServiceBlockingStub blockingStub;

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

        blockingStub =
                SmartBinServiceGrpc.newBlockingStub(channel);
    }

    public void registerBin() {
        RegisterBinRequest request =
                RegisterBinRequest.newBuilder()
                        .setBinId("BIN-001")
                        .setLocation("O'Connell Street, Dublin")
                        .setWasteType("General Waste")
                        .setCapacityLitres(240)
                        .build();

        try {
            RegisterBinResponse response =
                    blockingStub.registerBin(request);

            System.out.println();
            System.out.println("=== Register Bin ===");
            System.out.println("Success: " + response.getSuccess());
            System.out.println("Message: " + response.getMessage());

        } catch (StatusRuntimeException exception) {
            System.err.println(
                    "Register Bin RPC failed: "
                            + exception.getStatus().getDescription()
            );
        }
    }

    public void updateFillLevel() {
        UpdateFillLevelRequest request =
                UpdateFillLevelRequest.newBuilder()
                        .setBinId("BIN-001")
                        .setFillLevelPercentage(75)
                        .build();

        try {
            UpdateFillLevelResponse response =
                    blockingStub.updateFillLevel(request);

            System.out.println();
            System.out.println("=== Update Fill Level ===");
            System.out.println("Success: " + response.getSuccess());
            System.out.println("Message: " + response.getMessage());

        } catch (StatusRuntimeException exception) {
            System.err.println(
                    "Update Fill Level RPC failed: "
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

    public void shutdown() throws InterruptedException {
        channel.shutdown()
                .awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {

        SmartBinClient client = null;

        try {
            client = new SmartBinClient();

            client.registerBin();
            client.updateFillLevel();
            client.reportDamage();
            client.getBinStatus();

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