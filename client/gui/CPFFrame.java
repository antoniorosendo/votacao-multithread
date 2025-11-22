package client.gui;

import client.ClientController;
import common.CPFValidator;

import javax.swing.*;
import java.awt.*;

public class CPFFrame extends JFrame {

    private final ClientController controller;

    public CPFFrame(ClientController controller) {
        this.controller = controller;

        setTitle("User Identification");
        setSize(400, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel container = new JPanel();
        container.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Enter your CPF");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel cpfLabel = new JLabel("CPF:");
        cpfLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField cpfField = new JTextField();
        cpfField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JButton continueBtn = new JButton("Continue");
        continueBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        continueBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        continueBtn.addActionListener(e -> {
            String cpf = cpfField.getText();

            if (!CPFValidator.isCPF(cpf)) {
                JOptionPane.showMessageDialog(this, "Invalid CPF.");
                return;
            }

            if (controller.getElection() == null) {
                JOptionPane.showMessageDialog(this, "Election data was not received.");
                return;
            }

            new VoteFrame(controller, cpf).setVisible(true);
            dispose();
        });

        container.add(title);
        container.add(Box.createRigidArea(new Dimension(0, 15)));
        container.add(cpfLabel);
        container.add(cpfField);
        container.add(Box.createRigidArea(new Dimension(0, 15)));
        container.add(continueBtn);

        add(container);
    }
}
