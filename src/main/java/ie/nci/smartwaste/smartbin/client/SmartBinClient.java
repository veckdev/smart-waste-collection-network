package ie.nci.smartwaste.smartbin.client;

import ie.nci.smartwaste.smartbin.RegisterBinRequest;
import ie.nci.smartwaste.smartbin.RegisterBinResponse;
import ie.nci.smartwaste.smartbin.SmartBinServiceGrpc;
import ie.nci.smartwaste.smartbin.UpdateFillLevelRequest;
import ie.nci.smartwaste.smartbin.UpdateFillLevelResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

public class SmartBinClient {

    private static final String HOST = "localhost";
    private static final int PORT = 50051;

    private final ManagedChannel channel;
    private final SmartBinServiceGrpc.SmartBinServiceBlockingStub blockingStub;

    public SmartBinClient() {
        channel = ManagedChannelBuilder
                .forAddress(HOST, PORT)
                .usePlaintext()
                .build();

        blockingStub = SmartBinServiceGrpc.newBlockingStub(channel);
    }

    public void registerBin() {
        RegisterBinRequest request = RegisterBinRequest.newBuilder()
                .setBinId("BIN-001")
                .setLocation("O'Connell Street, Dublin")
                .setWasteType("General Waste")
                .setCapacityLitres(240)
                .build();

        try {
            RegisterBinResponse response = blockingStub.registerBin(request);

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
        UpdateFillLevelRequest request = UpdateFillLevelRequest.newBuilder()
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

    public void shutdown() throws InterruptedException {
        channel.shutdown()
                .awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {
        SmartBinClient client = new SmartBinClient();

        try {
            client.registerBin();
            client.updateFillLevel();

        } finally {
            try {
                client.shutdown();

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                System.err.println("Client shutdown was interrupted.");
            }
        }
    }
}