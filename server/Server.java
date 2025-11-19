package server;

import common.NetProtocol;

public class Server {

    public static void main(String[] args) {
        try {
            System.out.println("Server starting at " +
                NetProtocol.getLocalIpAddress() + " : " + NetProtocol.port);

            ServerController controller = ServerController.getInstance();
            controller.start();

            System.out.println("Server shutting down...");

        } catch (Exception e) {
            System.err.println("Unexpected exception: " + e.getMessage());
        }
    }
}
