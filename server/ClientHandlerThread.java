package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import common.ElectionData;
import common.NetCommand;
import common.Vote;

public class ClientHandlerThread extends Thread {
    
    private final Socket clientSocket;
    private final ServerController controller;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String clientAddress;

    /**
     * Constructs a new client handler thread.
     * @param clientSocket The connected client socket
     * @param controller Reference to the server controller
     */
    public ClientHandlerThread(Socket clientSocket, ServerController controller) {
        this.clientSocket = clientSocket;
        this.controller = controller;
        this.clientAddress = clientSocket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
        
            output = new ObjectOutputStream(clientSocket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(clientSocket.getInputStream());

            controller.log("← Streams established with client: " + clientAddress);

            
            sendElectionData();

            
            receiveAndProcessVote();

        } catch (IOException e) {
            controller.log("✗ Connection error with " + clientAddress + ": " + e.getMessage());
        } catch (ClassNotFoundException e) {
            controller.log("✗ Invalid data received from " + clientAddress);
        } finally {
            closeConnection();
        }
    }

    /**
     * Sends the election data to the connected client.
     * @throws IOException if communication fails
     */
    private void sendElectionData() throws IOException {
        ElectionData electionData = controller.getElectionData();

        if (electionData == null) {
            controller.log("✗ No election data available for " + clientAddress);
            
            
            NetCommand errorCmd = new NetCommand(NetCommand.CMD_ERROR, 
                "No election currently loaded on server");
            output.writeObject(errorCmd);
            output.flush();
            return;
        }

      
        output.writeObject(electionData);
        output.flush();

        controller.log("Election data sent to " + clientAddress);
    }

    /**
     * Receives a vote from the client and processes it.
     * @throws IOException if communication fails
     * @throws ClassNotFoundException if received object is invalid
     */
    private void receiveAndProcessVote() throws IOException, ClassNotFoundException {
        
        Object receivedObject = input.readObject();

        if (receivedObject instanceof Vote) {
            Vote vote = (Vote) receivedObject;
            
            controller.log("← Vote received from " + clientAddress + 
                          " (CPF: " + maskCPF(vote.getCpf()) + ")");

           
            boolean success = false;
            ServerGUI gui = controller.getGUI();
            
            if (gui != null) {
                success = gui.registerVote(vote);
            } else {
               
                controller.log("✗ Cannot register vote: GUI not available");
            }

           
            NetCommand response;
            if (success) {
                response = new NetCommand(NetCommand.CMD_OK, "Vote registered successfully");
                controller.log("✓ Vote accepted from " + clientAddress);
            } else {
                response = new NetCommand(NetCommand.CMD_ERROR, 
                    "Vote rejected - possible duplicate CPF or invalid option");
                controller.log("Vote rejected from " + clientAddress);
            }

            output.writeObject(response);
            output.flush();

        } else {
            controller.log("Invalid object received from " + clientAddress + 
                          " (expected Vote)");
            
            NetCommand errorCmd = new NetCommand(NetCommand.CMD_ERROR, 
                "Invalid data format");
            output.writeObject(errorCmd);
            output.flush();
        }
    }

    
    private void closeConnection() {
        try {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            
            controller.log("Client disconnected: " + clientAddress);
            
        } catch (IOException e) {
            controller.log("Error closing connection with " + clientAddress + 
                          ": " + e.getMessage());
        }
    }

    /**
     * Masks a CPF for privacy in logs.
     * @param cpf The full CPF
     * @return Partially masked CPF
     */
    private String maskCPF(String cpf) {
        if (cpf == null || cpf.length() < 6) {
            return "***";
        }
        return "***." + cpf.substring(3, 6) + ".***-**";
    }
}