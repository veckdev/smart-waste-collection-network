package ie.nci.smartwaste.smartbin.client;

import ie.nci.smartwaste.smartbin.RegisterBinRequest;
import ie.nci.smartwaste.smartbin.RegisterBinResponse;
import ie.nci.smartwaste.smartbin.SmartBinServiceGrpc;
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
            System.out.println("Success : " + response.getSuccess());
            System.out.println("Message : " + response.getMessage());

        } catch (StatusRuntimeException exception) {

            System.err.println("RPC Error: " + exception.getStatus());

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

        } finally {

            try {

                client.shutdown();

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();

            }

        }

    }

}