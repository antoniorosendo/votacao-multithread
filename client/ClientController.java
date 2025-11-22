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

    private void receiveElectionData() throws IOException, ClassNotFoundException {
        System.out.println("Aguardando dados da eleição...");
        Object obj = in.readObject();

        if (obj instanceof ElectionData) {
            this.currentElection = (ElectionData) obj;
            System.out.println("Eleição Recebida: " + currentElection.getQuestion());
        } else {
            System.err.println("Objeto inesperado recebido: " + obj.getClass().getName());
        }
    }

    private void sendVote(Vote vote) throws IOException {
        System.out.println("Enviando voto para o CPF: " + vote.getCpf());
        out.writeObject(vote);
        out.flush();
    }

    private void receiveConfirmation() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (obj instanceof NetControl) {
            NetControl msg = (NetControl) obj;
            
            if(msg.getNetCommand() == NetCommand.Acknowledge) {
                 System.out.println("SUCESSO: Voto computado!");
            } else {
                 System.out.println("ERRO: O servidor rejeitou o voto.");
            }
        }
    }

    //INTERFACE TEMPORÁRIA (Console)
    private Vote simulateUserInterface() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n--- TELA DE VOTAÇÃO ---");
        System.out.println("PERGUNTA: " + currentElection.getQuestion());
        System.out.println("OPÇÕES: " + currentElection.getOptions());
        
        System.out.print("Digite seu CPF: ");
        String cpf = scanner.nextLine();
        
        System.out.print("Digite o NOME da opção escolhida: ");
        String escolha = scanner.nextLine();
        
        return new Vote(cpf, escolha);
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}