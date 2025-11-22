package client;

public class Client {
    public static void main(String[] args) {
        try {
            System.out.println("Starting Client GUI...");

            ClientController controller = new ClientController();
            new client.gui.ConnectFrame(controller).setVisible(true);

        } catch (Exception exceptionValue) {
            System.err.println("Unexpected exception: " + exceptionValue.getMessage());
            exceptionValue.printStackTrace();
        }
    }
}