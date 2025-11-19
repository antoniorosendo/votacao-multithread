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

// Controlador da Pessoa 3: Gerencia a lógica de votação do Cliente
public class ClientController {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ElectionData currentElection;

    // Método principal que organiza o fluxo
    public void start() {
        try {
            connect();

            // 1. Recebe a Eleição do Servidor
            receiveElectionData();

            // 2. Se recebeu a eleição corretamente, inicia a votação
            if (currentElection != null) {
                // Simula a interface (A Pessoa 5 vai trocar isso pela tela gráfica depois)
                Vote vote = simulateUserInterface();
                
                // 3. Envia o voto para o servidor
                sendVote(vote);
                
                // 4. Espera a confirmação
                receiveConfirmation();
            }

        } catch (Exception e) {
            System.err.println("Erro no cliente: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    // Conecta ao servidor
    private void connect() throws IOException {
        System.out.println("Conectando ao servidor...");
        socket = new Socket(NetProtocol.serverAddress, NetProtocol.port);
        
        // A ordem é importante: Output primeiro para evitar travamento
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Conectado!");
    }

    // Recebe o objeto com a Pergunta e Opções
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

    // Envia o objeto Vote para o servidor
    private void sendVote(Vote vote) throws IOException {
        System.out.println("Enviando voto para o CPF: " + vote.getCpf());
        out.writeObject(vote);
        out.flush();
    }

    // Recebe a confirmação do servidor
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

    // INTERFACE TEMPORÁRIA (Console)
    // Isso permite você testar AGORA, antes da Pessoa 5 fazer a janela bonita
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