package ie.nci.smartwaste.collection.server;

import ie.nci.smartwaste.collection.service.CollectionServiceImpl;
import ie.nci.smartwaste.discovery.ServiceRegistration;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class CollectionServer {

    private static final int PORT = 50052;

    public static void main(String[] args)
            throws Exception {

        Server server = ServerBuilder
                .forPort(PORT)
                .addService(new CollectionServiceImpl())
                .build()
                .start();

        System.out.println(
                "Collection Service started on port " + PORT
        );

        ServiceRegistration registration =
                new ServiceRegistration();

        registration.registerService(
                "_collection._tcp.local.",
                "Collection Service",
                PORT,
                "Collection gRPC Service"
        );

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    System.out.println(
                            "Shutting down Collection Service..."
                    );
                    server.shutdown();
                })
        );

        server.awaitTermination();
    }
}