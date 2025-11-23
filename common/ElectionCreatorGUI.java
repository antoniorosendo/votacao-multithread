package common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ElectionCreatorGUI extends JFrame {

    private JTextField txtQuestion;
    private JTextField txtOption;
    private DefaultListModel<String> listModel;
    private JList<String> listOptions;

    public ElectionCreatorGUI() {
        setTitle("Election Creator Tool");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setupUI();
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel lblQuestion = new JLabel("Election Question:");
        lblQuestion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(lblQuestion, gbc);

        gbc.gridy = 1;
        gbc.weightx = 1.0;
        txtQuestion = new JTextField();
        txtQuestion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtQuestion.setPreferredSize(new Dimension(0, 30));
        mainPanel.add(txtQuestion, gbc);

        gbc.gridy = 2;
        gbc.weightx = 0;
        JLabel lblOption = new JLabel("Add Option:");
        lblOption.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(lblOption, gbc);

        gbc.gridy = 3;
        gbc.weightx = 1.0;
        JPanel pnlAdd = new JPanel(new BorderLayout(10, 0));
        
        txtOption = new JTextField();
        txtOption.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtOption.setPreferredSize(new Dimension(0, 30));
        
        JButton btnAdd = new JButton("Add Option");
        btnAdd.setFocusPainted(false);
        btnAdd.addActionListener(e -> addOption());
        
        txtOption.addActionListener(e -> addOption());

        pnlAdd.add(txtOption, BorderLayout.CENTER);
        pnlAdd.add(btnAdd, BorderLayout.EAST);
        mainPanel.add(pnlAdd, gbc);

        gbc.gridy = 4;
        JLabel lblList = new JLabel("Voting Options List:");
        lblList.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainPanel.add(lblList, gbc);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        
        listModel = new DefaultListModel<>();
        listOptions = new JList<>(listModel);
        listOptions.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        listOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(listOptions);
        mainPanel.add(scrollPane, gbc);

        gbc.gridy = 6;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        
        JButton btnRemove = new JButton("Remove Selected");
        btnRemove.setFocusPainted(false);
        btnRemove.addActionListener(e -> removeSelectedOption());
        mainPanel.add(btnRemove, gbc);

        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(new JSeparator(), gbc);

        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 0, 5);
        
        JButton btnSave = new JButton("SAVE ELECTION FILE (.SER)");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setBackground(new Color(33, 140, 116));
        btnSave.setForeground(Color.BLACK);
        btnSave.setFocusPainted(false);
        btnSave.setPreferredSize(new Dimension(0, 45));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveElection());
        
        mainPanel.add(btnSave, gbc);

        add(mainPanel);
        
        SwingUtilities.invokeLater(() -> txtQuestion.requestFocusInWindow());
    }

    private void addOption() {
        String option = txtOption.getText().trim();
        if (!option.isEmpty()) {
            if (listModel.contains(option)) {
                JOptionPane.showMessageDialog(this, "Option already exists!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            listModel.addElement(option);
            txtOption.setText("");
            txtOption.requestFocus();
        }
    }

    private void removeSelectedOption() {
        int selectedIndex = listOptions.getSelectedIndex();
        if (selectedIndex != -1) {
            listModel.remove(selectedIndex);
        } else {
            JOptionPane.showMessageDialog(this, "Select an option to remove.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveElection() {
        String question = txtQuestion.getText().trim();
        
        if (question.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a question.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (listModel.size() < 2) {
            JOptionPane.showMessageDialog(this, "You need at least 2 options.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<String> optionsList = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            optionsList.add(listModel.get(i));
        }

        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setDialogTitle("Save Election Data");
        fileChooser.setSelectedFile(new File("election.ser"));
        
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            
            if (!filePath.toLowerCase().endsWith(".ser")) {
                filePath += ".ser";
            }

            try {
                ElectionData electionData = new ElectionData(question, optionsList);
                
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
                    oos.writeObject(electionData);
                }

                JOptionPane.showMessageDialog(this, 
                    "Election saved successfully!\nFile: " + filePath, 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
                txtQuestion.setText("");
                listModel.clear();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving file: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ElectionCreatorGUI().setVisible(true);
        });
    }
}