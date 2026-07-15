package ie.nci.smartwaste.smartbin.server;

import ie.nci.smartwaste.discovery.ServiceRegistration;
import ie.nci.smartwaste.smartbin.service.SmartBinServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class SmartBinServer {

    private static final int PORT = 50051;
    private static final String SERVICE_TYPE = "_smartbin._tcp.local.";
    private static final String SERVICE_NAME = "Smart Bin Service";

    private final Server server;
    private final ServiceRegistration serviceRegistration;

    public SmartBinServer() {
        this.server = ServerBuilder
                .forPort(PORT)
                .addService(new SmartBinServiceImpl())
                .build();

        this.serviceRegistration = new ServiceRegistration();
    }

    public void start() throws IOException {
        server.start();

        System.out.println("Smart Bin Service started on port " + PORT);

        serviceRegistration.registerService(
                SERVICE_TYPE,
                SERVICE_NAME,
                PORT,
                "Smart Bin gRPC Service"
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Smart Bin Service...");
            SmartBinServer.this.stop();
        }));
    }

    public void stop() {
        serviceRegistration.unregisterService();

        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args)
            throws IOException, InterruptedException {

        SmartBinServer smartBinServer = new SmartBinServer();

        smartBinServer.start();
        smartBinServer.blockUntilShutdown();
    }
}