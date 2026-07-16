package ie.nci.smartwaste.recycling;

import ie.nci.smartwaste.discovery.ServiceRegistration;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class RecyclingServer {

    private static final int PORT = 50053;

    public static void main(String[] args)
            throws Exception {

        Server server = ServerBuilder
                .forPort(PORT)
                .addService(new RecyclingServiceImpl())
                .build()
                .start();

        System.out.println(
                "Recycling Center Service started on port " + PORT
        );

        ServiceRegistration registration =
                new ServiceRegistration();

        registration.registerService(
                "_recycling._tcp.local.",
                "Recycling Center Service",
                PORT,
                "Recycling Center gRPC Service"
        );

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    System.out.println(
                            "Shutting down Recycling Center Service..."
                    );
                    server.shutdown();
                })
        );

        server.awaitTermination();
    }
}