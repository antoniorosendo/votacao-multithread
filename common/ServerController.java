package common;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServerController {

    private static ServerController instance;
    private Random random = new Random();

    // Private constructor → prevents external instantiation
    private ServerController() {}

    // Singleton getter → returns the only instance
    public static synchronized ServerController getInstance() {
        if (instance == null) {
            instance = new ServerController();
        }
        return instance;
    }

    // Generates random payloads (same logic as before)
    protected List<Payload> generatePayloads() {
        int limit = random.nextInt(64) + 1;
        List<Payload> payloads = new ArrayList<>();

        for (int count = 0; count < limit; count++) {
            payloads.add(new Payload(getRandomName(), count * 1000));
        }

        return payloads;
    }

    // Generates random name combinations
    private String getRandomName() {
        String[] first = { "Ana", "Bruno", "Claudia", "Diego", "Elvira", "Fabiano", "Gertrudes",
                "Hilton", "Idalina", "Joao", "Kelly", "Luciano", "Maria" };

        String[] second = { "Junqueira", "Katz", "Lemos", "Monteiro", "Nunes",
                "Orlando", "Pinheiro", "Queiroz", "Rocha" };

        return first[random.nextInt(first.length)] + " " +
               second[random.nextInt(second.length)];
    }

    // Starts the multithreaded server
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(NetProtocol.port)) {

            System.out.println("Server listening on port " + NetProtocol.port);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: "
                        + clientSocket.getInetAddress().getHostAddress());

                    ClientHandlerThread handler =
                            new ClientHandlerThread(clientSocket, this);

                    handler.start();

                } catch (IOException e) {
                    System.err.println("Failed to accept client: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Could not open port " + NetProtocol.port +
                               ". Reason: " + e.getMessage());
        }
    }
}