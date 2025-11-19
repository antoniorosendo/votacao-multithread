package client;

import common.NetProtocol;

// A única função desta classe é iniciar o programa
public class Client {
    public static void main(String[] args) {
        try {
            System.out.println("Client starting...");
            System.out.println("Looking for server at " + NetProtocol.serverAddress + " @ " + NetProtocol.port);

            // Instancia o controlador (que agora está em outro arquivo) e inicia
            ClientController controller = new ClientController();
            controller.start();

            System.out.println("Client is shutting down.");
        } catch (Exception exceptionValue) {
            System.err.println("Unexpected exception: " + exceptionValue.getMessage());
            exceptionValue.printStackTrace();
        }
    }
}