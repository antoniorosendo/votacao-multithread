package common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandlerThread extends Thread {

    private Socket clientSocket;
    private ServerController controller;

    public ClientHandlerThread(Socket socket, ServerController controller) {
        this.clientSocket = socket;
        this.controller = controller;
    }

    @Override
    public void run() {
        try (Socket s = this.clientSocket;
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s.getInputStream())) {

            System.out.println("Sending payloads to client: "
                + s.getInetAddress().getHostAddress());

            List<Payload> payloads = controller.generatePayloads();

            for (Payload payload : payloads) {
                out.writeObject(payload);
                System.out.print("Payload sent... ");

                NetControl ack = (NetControl) in.readObject();

                if (ack.getNetCommand() == NetCommand.Acknowledge) {
                    System.out.println("Acknowledged.");
                }
            }

            System.out.println("Sending shutdown command to client: "
                + s.getInetAddress().getHostAddress());

            out.writeObject(new NetControl(NetCommand.Shutdown));

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with client "
                + clientSocket.getInetAddress().getHostAddress()
                + ": " + e.getMessage());
        } finally {
            System.out.println("Client handler ended for: "
                + clientSocket.getInetAddress().getHostAddress());
        }
    }
}
