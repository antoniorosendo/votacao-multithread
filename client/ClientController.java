package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import common.ElectionData;
import common.NetCommand;
import common.NetControl;
import common.NetProtocol;
import common.Vote;

public class ClientController {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ElectionData currentElection;

    public void start() {
        try {
            connect();
            receiveElectionData();

            if (currentElection != null) {
                Vote vote = simulateUserInterface();
                sendVote(vote);
                receiveConfirmation();
            }

        } catch (Exception e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void connect() throws IOException {
        System.out.println("Conectando ao servidor...");
        socket = new Socket(NetProtocol.serverAddress, NetProtocol.port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Conectado!");
    }

    public void connect(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public ElectionData receiveElectionData() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();

        if (obj instanceof ElectionData) {
            this.currentElection = (ElectionData) obj;
            return currentElection;
        } else {
            return null;
        }
    }

    private void sendVote(Vote vote) throws IOException {
        out.writeObject(vote);
        out.flush();
    }

    public void sendVote(String cpf, String option) throws IOException {
        Vote vote = new Vote(cpf, option);
        sendVote(vote);
    }

    public boolean receiveConfirmation() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (obj instanceof NetControl) {
            NetControl msg = (NetControl) obj;

            if (msg.getNetCommand() == NetCommand.Acknowledge) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private Vote simulateUserInterface() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- Votação ---");
        System.out.println("Pergunta: " + currentElection.getQuestion());
        System.out.println("Opções: " + currentElection.getOptions());

        System.out.print("Digite seu CPF: ");
        String cpf = scanner.nextLine();

        System.out.print("Digite o nome da opção escolhida: ");
        String escolha = scanner.nextLine();

        return new Vote(cpf, escolha);
    }

    public ElectionData getElection() {
        return currentElection;
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
