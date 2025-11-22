package client;

import common.NetProtocol;

public class Client {
    public static void main(String[] args) {
        try {
            System.out.println("Client starting...");
            System.out.println("Looking for server at " + NetProtocol.serverAddress + " @ " + NetProtocol.port);

            ClientController controller = new ClientController();
            controller.start();

            System.out.println("Client is shutting down.");
        } catch (Exception exceptionValue) {
            System.err.println("Unexpected exception: " + exceptionValue.getMessage());
            exceptionValue.printStackTrace();
        }
    }
}