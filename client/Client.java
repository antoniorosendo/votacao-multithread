package client;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import common.NetCommand;
import common.NetControl;
import common.NetProtocol;
import common.Payload;

// Client connects to the server and receive a sequence of payloads
// Shutdown according to a command received through the networK (no user interface)
public class Client {
   public static void main(String[] args) {
      try {
         System.out.println("Client starting: " + NetProtocol.getLocalIpAddress());
         System.out.println("\tLoocking for server at " + NetProtocol.serverAddress + " @ " + NetProtocol.port);

         // Transferring control to OOP world
         (new ClientController()).start();

         System.out.println("Client is shutting down now.");
      } catch (Exception exceptionValue) {
         System.err.println("Unexpected exception: " + exceptionValue.getMessage());
      }
   }
}

// Controller class
class ClientController {
   private static final String FILE_PATH = "payload_data.ser";

   // orchestrator method
   void start() {
      // Not an attribute to show the data persistence in file and disk I/O operations
      List<Payload> dataSet;

      dataSet = receiveData();
      saveData(dataSet);

      // just to avoid misinterpretation
      dataSet.clear();

      dataSet = readData();
      makeReport(dataSet);
   }

   // Formating data only to show that they are from a file and not from the
   // network
   private void makeReport(List<Payload> dataSet) {
      String decorator = "****************************************";
      long sum = 0L;

      System.out.println("Preparing report");
      System.out.println();
      System.out.println(decorator);
      System.out.println("Report: dataset received from Server");
      System.out.println(decorator);

      for (int count = 0; count < dataSet.size(); count++) {
         System.out.println(count + "\t" + dataSet.get(count).getValue() + "\t" + dataSet.get(count).getMessage());
         sum += dataSet.get(count).getValue();
      }

      System.out.println(decorator);
      System.out.println("Overal summ: " + sum + " for " + dataSet.size() + " records.");
      System.out.println(decorator);
   }

   // Read from file using streams
   private List<Payload> readData() {
      List<Payload> dataSet = new ArrayList<>();
      try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
         while (true) {
            dataSet.add((Payload) objectInputStream.readObject());
         }
      } catch (FileNotFoundException exceptionValue) {
         System.err.println("File not found " + FILE_PATH + ": " + exceptionValue.getMessage());
      } catch (ClassNotFoundException exceptionValue) {
         System.err.println("Payload class not found: " + exceptionValue.getMessage());
      } catch (IOException exceptionValue) {
         // EOF marker
      }
      return (dataSet);
   }

   // Initiate connection with the server, receive a set of Payload objects using
   // streams
   // Bidirectional communication with the server (receives data and commnads,
   // sends ack)
   private List<Payload> receiveData() {
      List<Payload> dataSet = new ArrayList<>();
      boolean isRunning = true;

      // Getting the socket (talking through TPC/IP)
      // try-with-resources to automatically close the socket
      try (Socket socket = new Socket(NetProtocol.serverAddress, NetProtocol.port)) {
         // Opening TWO channels to server
         ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
         ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

         // Opening the channels automatically connect to the other side
         System.out.println("Connected to server. Waiting for payloads...");

         while (isRunning) {
            // 1. LEIA COMO UM 'Object' GENÉRICO
            Object receivedObject = objectInputStream.readObject();

            // 2. AGORA SIM, VERIFIQUE O TIPO
            if (receivedObject instanceof Payload) {
               // É um payload, adicione na lista
               System.out.println("Payload received.");
               dataSet.add((Payload) receivedObject);

               // Envie o Acknowledge (confirmação)
               objectOutputStream.writeObject(new NetControl(NetCommand.Acknowledge));
            } else if (receivedObject instanceof NetControl) {
               // É um comando de controle
               NetControl netControl = (NetControl) receivedObject;
               System.out.println("NetControl received: " + netControl.getNetCommand());

               // Verifique se é o comando de Shutdown
               isRunning = (netControl.getNetCommand() != NetCommand.Shutdown);
            }
            System.out.println("Connection finished.");
         }
      } catch (IOException exceptionValue) {
         System.err.println("Error connecting to server or reading data: " + exceptionValue.getMessage());
      } catch (ClassNotFoundException exceptionValue) {
         System.err.println("Class not found during object deserialization: " + exceptionValue.getMessage());
      }
      return (dataSet);
   }

   // Saving data to a file using streams
   private void saveData(List<Payload> dataSet) {
      System.out.println("Saving received dataset to " + FILE_PATH);
      try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
         for (Payload payload : dataSet) {
            objectOutputStream.writeObject(payload);
         }
         System.out.println("Successfully saved " + dataSet.size() + " objects");
      } catch (IOException exceptionValue) {
         System.err.println("Error saving data: " + exceptionValue.getMessage());
      }
      dataSet.clear();
   }
}
