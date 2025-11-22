package client.gui;

import client.ClientController;
import common.ElectionData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VoteFrame extends JFrame {

    private final ClientController controller;
    private final String cpf;

    public VoteFrame(ClientController controller, String cpf) {
        this.controller = controller;
        this.cpf = cpf;

        setTitle("Vote");
        setSize(500, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ElectionData election = controller.getElection();
        if (election == null) {
            JOptionPane.showMessageDialog(this, "Election data not available.");
            dispose();
            return;
        }

        JPanel container = new JPanel();
        container.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(election.getQuestion());
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ButtonGroup group = new ButtonGroup();
        List<JRadioButton> radios = new ArrayList<>();

        for (String option : election.getOptions()) {
            JRadioButton rb = new JRadioButton(option);
            rb.setAlignmentX(Component.LEFT_ALIGNMENT);
            radios.add(rb);
            group.add(rb);
            optionsPanel.add(rb);
            optionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        JScrollPane scrollPane = new JScrollPane(optionsPanel);
        scrollPane.setBorder(null);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton voteBtn = new JButton("Submit Vote");
        voteBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        voteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        voteBtn.addActionListener(e -> {
            String choice = radios.stream()
                                  .filter(JRadioButton::isSelected)
                                  .map(AbstractButton::getText)
                                  .findFirst()
                                  .orElse(null);

            if (choice == null) {
                JOptionPane.showMessageDialog(this, "Select one option.");
                return;
            }

            try {
                controller.sendVote(cpf, choice);

                boolean success = controller.receiveConfirmation();
                if (success) {
                    JOptionPane.showMessageDialog(this, "Vote sent successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Vote rejected by server.");
                }

                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error sending vote: " + ex.getMessage());
            }
        });

        container.add(title);
        container.add(Box.createRigidArea(new Dimension(0, 15)));
        container.add(scrollPane);
        container.add(Box.createRigidArea(new Dimension(0, 15)));
        container.add(voteBtn);

        add(container);

        setJMenuBar(buildMenu());
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("Credits");

        about.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "Distributed Voting System\nDeveloped by the group")
        );

        help.add(about);
        bar.add(help);
        return bar;
    }
}
