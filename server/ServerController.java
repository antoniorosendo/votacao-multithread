package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import common.ElectionData;
import common.NetProtocol;

public class ServerController {

    private static ServerController instance;
    private ServerGUI gui;
    private ElectionData electionData;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

  
    private ServerController() {}

    
    public static synchronized ServerController getInstance() {
        if (instance == null) {
            instance = new ServerController();
        }
        return instance;
    }

    /**
     * Sets the GUI reference for logging and vote registration.
     * @param gui The ServerGUI instance
     */
    public void setGUI(ServerGUI gui) {
        this.gui = gui;
    }

    /**
     * Sets the election data that will be sent to clients.
     * @param electionData The election data loaded from file
     */
    public void setElectionData(ElectionData electionData) {
        this.electionData = electionData;
        if (gui != null) {
            gui.addLog("Election data set: " + electionData.getQuestion());
        }
    }

    /**
     * Returns the current election data.
     * @return ElectionData or null if not loaded
     */
    public ElectionData getElectionData() {
        return electionData;
    }

    /**
     * Checks if the server is currently running.
     * @return true if server is accepting connections
     */
    public boolean isRunning() {
        return running;
    }

    
    public void start() {
        try {
            serverSocket = new ServerSocket(NetProtocol.port);
            running = true;

            if (gui != null) {
                gui.addLog("Server socket opened on port " + NetProtocol.port);
            } else {
                System.out.println("Server listening on port " + NetProtocol.port);
            }

            while (running) {
                try {
                 
                    Socket clientSocket = serverSocket.accept();
                    
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    String clientHost = clientSocket.getInetAddress().getHostName();
                    
                    if (gui != null) {
                        gui.addLog("→ New client connected: " + clientAddress + 
                                   " (" + clientHost + ")");
                    } else {
                        System.out.println("New client connected: " + clientAddress);
                    }

                   
                    
                    ClientHandlerThread handler = new ClientHandlerThread(clientSocket, this);
                    handler.start();

                } catch (IOException e) {
                    if (running) {
                        // Only log error if server is still supposed to be running
                        if (gui != null) {
                            gui.addLog("✗ Error accepting client: " + e.getMessage());
                        } else {
                            System.err.println("Failed to accept client: " + e.getMessage());
                        }
                    }
                }
            }

        } catch (IOException e) {
            if (gui != null) {
                gui.addLog("✗ CRITICAL: Could not open port " + NetProtocol.port + 
                           " - " + e.getMessage());
            } else {
                System.err.println("Could not open port " + NetProtocol.port + 
                                   ". Reason: " + e.getMessage());
            }
        } finally {
            shutdown();
        }
    }

  
    public void shutdown() {
        running = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                
                if (gui != null) {
                    gui.addLog("Server socket closed");
                } else {
                    System.out.println("Server socket closed");
                }
            }
        } catch (IOException e) {
            if (gui != null) {
                gui.addLog("✗ Error closing server socket: " + e.getMessage());
            } else {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }

    /**
     * Logs a message through the GUI or console.
     * @param message The message to log
     */
    public void log(String message) {
        if (gui != null) {
            gui.addLog(message);
        } else {
            System.out.println(message);
        }
    }

    /**
     * Returns the GUI instance.
     * @return ServerGUI or null if running in console mode
     */
    public ServerGUI getGUI() {
        return gui;
    }
}