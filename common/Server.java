package common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Server prepares a list of random payloads and send it to the single client AFTER a client connection request
// Sends a shutdown command to dismiss client without user interface
public class Server
   {
   public static void main(String[] args)
      {
      try
         {
         System.out.println("Server starting: " + NetProtocol.getLocalIpAddress() + " @ " + NetProtocol.port);

         // Transferring control to OOP world
         (new ServerController()).start();

         System.out.println("Server is shutting down now.");
         }
      catch (Exception exceptionValue)
         {
         System.err.println("Unexpected exception: " + exceptionValue.getMessage());
         }
      }
   }

// Controller class
class ServerController
   {
   private Random randGen = new Random();

   // Creates a list with payloads
   private List<Payload> generatePayloads()
      {
      int           limit    = randGen.nextInt(64) + 1;
      List<Payload> payloads = new ArrayList<>();

      for (int count = 0; count < limit; count++)
         {
         payloads.add(new Payload(getRandomName(), count * 1000));
         }
      return payloads;
      }

   // Generate random combinations of words simulating different names
   private String getRandomName()
      {
      String first[]  = { "Ana", "Bruno", "Claudia", "Diego", "Elvira", "Fabiano", "Gertrudes", "Hilton", "Idalina", "Joao", "Kelly", "Luciano", "Maria" };
      String second[] = { "Junqueira", "Katz", "Lemos", "Monteiro", "Nunes", "Orlando", "Pinheiro", "Queiroz", "Rocha" };
      String buffer   = first[randGen.nextInt(first.length)] + " " + second[randGen.nextInt(second.length)];
      return (buffer);
      }

   void start()
      {
      // Opening the SERVER socket to accept client requests (talking through TPC/IP)
      // try-with-resources to automatically close the socket
      try (ServerSocket serverSocket = new ServerSocket(NetProtocol.port))
         {
         // Wait for a client; server does NOT initiate a connection
         System.out.println("Server listening on port " + NetProtocol.port);

         try (Socket clientSocket = serverSocket.accept())
            {
            // Opening TWO channels to the client
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream  objectInputStream  = new ObjectInputStream(clientSocket.getInputStream());

            // Preparing payloads to send
            System.out.println("Sending payloads to client...");
            List<Payload> payloads = generatePayloads();

            for (Payload payload : payloads)
               {
               // Send each payload to the client up to the end of the list
               objectOutputStream.writeObject(payload);
               System.out.print("Payload sent: " + payload + " ... ");

               // Wait for an acknowledge for the payload
               // Receive a NetControl object, looking for Acknowledge command; ignore everything else
               NetControl receivedAnswer = (NetControl) objectInputStream.readObject();
               if (receivedAnswer.getNetCommand() == NetCommand.Acknowledge)
                  {
                  System.out.println("Client acknowledged the payload.");
                  }
               }

            // Send a shutdown command to client; do not wait an answer
            System.out.println("Asking for graceful shutdown of the client.");
            objectOutputStream.writeObject(new NetControl(NetCommand.Shutdown));
            }
         catch (IOException exceptionValue)
            {
            System.err.println("Error communicating with client: " + exceptionValue.getMessage());
            }
         catch (ClassNotFoundException exceptionValue)
            {
            System.err.println("Class not found during object deserialization: " + exceptionValue.getMessage());
            }
         }
      catch (IOException exceptionValue)
         {
         System.err.println("Could not listen on port " + NetProtocol.port + ". " + exceptionValue.getMessage());
         }
      }
   }