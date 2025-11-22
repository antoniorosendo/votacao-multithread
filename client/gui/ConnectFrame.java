package client.gui;

import client.ClientController;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ConnectFrame extends JFrame {

    private final ClientController controller;

    public ConnectFrame(ClientController controller) {
        this.controller = controller;

        setTitle("Connect to Server");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel container = new JPanel();
        container.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Server Connection");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel ipLabel = new JLabel("Server IP:");
        ipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField ipField = new JTextField("127.0.0.1");
        ipField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel portLabel = new JLabel("Port:");
        portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField portField = new JTextField("5000");
        portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JButton connectBtn = new JButton("Connect");
        connectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        connectBtn.addActionListener(e -> {
            try {
                String ip = ipField.getText();
                int port = Integer.parseInt(portField.getText());

                controller.connect(ip, port);
                controller.receiveElectionData();

                new CPFFrame(controller).setVisible(true);
                dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Port must be a number.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not connect: " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Error receiving election data.");
            }
        });

        container.add(title);
        container.add(Box.createRigidArea(new Dimension(0, 15)));

        container.add(ipLabel);
        container.add(ipField);
        container.add(Box.createRigidArea(new Dimension(0, 12)));

        container.add(portLabel);
        container.add(portField);
        container.add(Box.createRigidArea(new Dimension(0, 20)));

        container.add(connectBtn);

        add(container);
    }
}
