package server;

import common.ElectionData;
import common.Vote;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ServerGUI extends JFrame {
    
    private JButton btnStartServer;
    private JButton btnLoadElection;
    private JButton btnEndElection;
    private JTextArea txtLogs;
    private JScrollPane scrollLogs;
    private JPanel pnlResults;
    private JProgressBar[] voteBars;
    private JLabel[] voteLabels;
    private JLabel lblServerStatus;
    private JLabel lblTotalVotes;
    
  
    private boolean serverRunning = false;
    private Thread serverThread;
    
   
    private ElectionData currentElection;
    private ConcurrentHashMap<String, Integer> voteCount;
    private Set<String> registeredCPFs;
    private Timer updateTimer;
    
    
    private static final Color COLOR_PRIMARY = new Color(41, 128, 185);
    private static final Color COLOR_SUCCESS = new Color(39, 174, 96);
    private static final Color COLOR_DANGER = new Color(231, 76, 60);
    private static final Color COLOR_WARNING = new Color(243, 156, 18);
    
    public ServerGUI() {
        initializeComponents();
        setupLayout();
        setupMenuBar();
        setupListeners();
        initializeVoteCount();
        startResultsUpdateTimer();
    }
    
    private void initializeComponents() {
        setTitle("Distributed Voting System - Server");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);
        
       
        voteCount = new ConcurrentHashMap<>();
        registeredCPFs = Collections.synchronizedSet(new HashSet<>());
        
        
        lblServerStatus = new JLabel("Server Status: Stopped");
        lblServerStatus.setFont(new Font("Arial", Font.BOLD, 14));
        lblServerStatus.setForeground(COLOR_DANGER);
        
        
        lblTotalVotes = new JLabel("Total Votes: 0");
        lblTotalVotes.setFont(new Font("Arial", Font.BOLD, 13));
        lblTotalVotes.setForeground(COLOR_PRIMARY);
        
        
        btnStartServer = new JButton("Start Server");
        styleButton(btnStartServer, COLOR_SUCCESS);
        
        btnLoadElection = new JButton("Load Election");
        styleButton(btnLoadElection, COLOR_PRIMARY);
        btnLoadElection.setEnabled(false);
        
        btnEndElection = new JButton("End Election");
        styleButton(btnEndElection, COLOR_DANGER);
        btnEndElection.setEnabled(false);
        
        
        txtLogs = new JTextArea();
        txtLogs.setEditable(false);
        txtLogs.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtLogs.setBackground(new Color(40, 44, 52));
        txtLogs.setForeground(new Color(171, 178, 191));
        txtLogs.setMargin(new Insets(5, 5, 5, 5));
        scrollLogs = new JScrollPane(txtLogs);
        scrollLogs.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_PRIMARY, 2),
            "Server Logs",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            COLOR_PRIMARY
        ));
        
        
        pnlResults = new JPanel();
        pnlResults.setLayout(new BoxLayout(pnlResults, BoxLayout.Y_AXIS));
        pnlResults.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COLOR_PRIMARY, 2),
            "Partial Results - Real Time",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            COLOR_PRIMARY
        ));
        pnlResults.setBackground(Color.WHITE);
    }
    
    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bgColor.brighter());
                }
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(Color.WHITE);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        statusPanel.setBackground(Color.WHITE);
        statusPanel.add(lblServerStatus);
        statusPanel.add(new JLabel(" | "));
        statusPanel.add(lblTotalVotes);
        topPanel.add(statusPanel, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnStartServer);
        buttonPanel.add(btnLoadElection);
        buttonPanel.add(btnEndElection);
        topPanel.add(buttonPanel, BorderLayout.CENTER);
        
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(scrollLogs);
        
        JScrollPane resultsScroll = new JScrollPane(pnlResults);
        resultsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        splitPane.setRightComponent(resultsScroll);
        
        splitPane.setDividerLocation(520);
        splitPane.setResizeWeight(0.55);
        
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menuHelp = new JMenu("Help");
        JMenuItem itemHelp = new JMenuItem("How to Use");
        JMenuItem itemAbout = new JMenuItem("About");
        
        itemHelp.addActionListener(e -> showHelpDialog());
        itemAbout.addActionListener(e -> showCreditsDialog());
        
        menuHelp.add(itemHelp);
        menuHelp.addSeparator();
        menuHelp.add(itemAbout);
        
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);
    }
    
    private void setupListeners() {
        btnStartServer.addActionListener(e -> startServer());
        btnLoadElection.addActionListener(e -> loadElection());
        btnEndElection.addActionListener(e -> endElection());
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });
    }
    
    private void handleWindowClose() {
        if (serverRunning) {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Server is still running. Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                shutdownServer();
                dispose();
                System.exit(0);
            }
        } else {
            dispose();
            System.exit(0);
        }
    }
    
    private void initializeVoteCount() {
        voteCount.clear();
        registeredCPFs.clear();
    }
    
    private void startResultsUpdateTimer() {
        updateTimer = new Timer(1000, e -> updateResultsDisplay());
        updateTimer.start();
    }
    
    
    
    private void startServer() {
        serverThread = new Thread(() -> {
            ServerController controller = ServerController.getInstance();
            controller.setGUI(this);
            controller.start();
        });
        
        serverThread.setDaemon(true);
        serverThread.start();
        
        serverRunning = true;
        
        SwingUtilities.invokeLater(() -> {
            lblServerStatus.setText("Server Status: Running");
            lblServerStatus.setForeground(COLOR_SUCCESS);
            btnStartServer.setEnabled(false);
            btnLoadElection.setEnabled(true);
            addLog(" Server started and listening for connections");
        });
    }
    
    private void loadElection() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Election Data File");
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".ser");
            }
            public String getDescription() {
                return "Election Data Files (*.ser)";
            }
        });
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                loadElectionFromFile(file);
                btnEndElection.setEnabled(true);
                addLog("Election loaded: " + currentElection.getQuestion());
                JOptionPane.showMessageDialog(
                    this,
                    "Election loaded successfully!\n\n" +
                    "Question: " + currentElection.getQuestion() + "\n" +
                    "Options: " + currentElection.getOptions().size(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to load election file:\n" + ex.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
                );
                addLog(" ERROR: Failed to load election - " + ex.getMessage());
            }
        }
    }
    
    private void loadElectionFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            currentElection = (ElectionData) ois.readObject();
            
            
            if (currentElection.getQuestion() == null || currentElection.getQuestion().trim().isEmpty()) {
                throw new IllegalArgumentException("Election question cannot be empty");
            }
            if (currentElection.getOptions() == null || currentElection.getOptions().isEmpty()) {
                throw new IllegalArgumentException("Election must have at least one option");
            }
            
           
            initializeVoteCount();
            for (String option : currentElection.getOptions()) {
                voteCount.put(option, 0);
            }
            
            // pdate ServerController with election data
            ServerController.getInstance().setElectionData(currentElection);
            
           
            setupResultsDisplay();
        }
    }
    
    private void setupResultsDisplay() {
        SwingUtilities.invokeLater(() -> {
            pnlResults.removeAll();
            
            if (currentElection == null) {
                return;
            }
            
            
            JLabel lblQuestion = new JLabel("<html><div style='padding: 5px;'><b>Question:</b><br>" + 
                currentElection.getQuestion() + "</div></html>");
            lblQuestion.setFont(new Font("Arial", Font.PLAIN, 13));
            lblQuestion.setBorder(new EmptyBorder(10, 10, 15, 10));
            lblQuestion.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlResults.add(lblQuestion);
            
            // sparador
            JSeparator separator = new JSeparator();
            separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
            pnlResults.add(separator);
            pnlResults.add(Box.createVerticalStrut(10));
            
            // barra progessor
            List<String> options = currentElection.getOptions();
            voteBars = new JProgressBar[options.size()];
            voteLabels = new JLabel[options.size()];
            
            for (int i = 0; i < options.size(); i++) {
                JPanel optionPanel = new JPanel();
                optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
                optionPanel.setBorder(new EmptyBorder(5, 15, 10, 15));
                optionPanel.setBackground(Color.WHITE);
                optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                optionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
                
                voteLabels[i] = new JLabel(options.get(i) + ": 0 votes (0%)");
                voteLabels[i].setFont(new Font("Arial", Font.BOLD, 12));
                voteLabels[i].setAlignmentX(Component.LEFT_ALIGNMENT);
                
                voteBars[i] = new JProgressBar(0, 100);
                voteBars[i].setValue(0);
                voteBars[i].setStringPainted(true);
                voteBars[i].setString("0%");
                voteBars[i].setForeground(getColorForOption(i));
                voteBars[i].setAlignmentX(Component.LEFT_ALIGNMENT);
                voteBars[i].setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                voteBars[i].setPreferredSize(new Dimension(300, 30));
                
                optionPanel.add(voteLabels[i]);
                optionPanel.add(Box.createVerticalStrut(5));
                optionPanel.add(voteBars[i]);
                
                pnlResults.add(optionPanel);
            }
            
            pnlResults.add(Box.createVerticalGlue());
            pnlResults.revalidate();
            pnlResults.repaint();
        });
    }
    
    private Color getColorForOption(int index) {
        Color[] colors = {
            new Color(52, 152, 219),   
            new Color(46, 204, 113),   
            new Color(155, 89, 182),   
            new Color(241, 196, 15),   
            new Color(230, 126, 34),   
            new Color(26, 188, 156),   
            new Color(231, 76, 60),    
            new Color(52, 73, 94)      
        };
        return colors[index % colors.length];
    }
    
    private void updateResultsDisplay() {
        if (currentElection == null || voteBars == null) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            int totalVotes = voteCount.values().stream().mapToInt(Integer::intValue).sum();
            lblTotalVotes.setText("Total Votes: " + totalVotes);
            
            List<String> options = currentElection.getOptions();
            for (int i = 0; i < options.size(); i++) {
                int votes = voteCount.getOrDefault(options.get(i), 0);
                int percentage = totalVotes > 0 ? (votes * 100) / totalVotes : 0;
                
                voteBars[i].setValue(percentage);
                voteBars[i].setString(percentage + "%");
                voteLabels[i].setText(String.format("%s: %d votes (%d%%)", 
                    options.get(i), votes, percentage));
            }
        });
    }
    
    private void endElection() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to end the election?\n" +
            "This will generate the final report and stop accepting new votes.",
            "End Election",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                String filename = generateFinalReport();
                btnEndElection.setEnabled(false);
                addLog(" Election ended. Final report generated: " + filename);
                
                JOptionPane.showMessageDialog(
                    this,
                    "Election ended successfully!\n\n" +
                    "Final report saved as:\n" + filename,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to generate final report:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                addLog(" ERROR: Failed to generate report " + ex.getMessage());
            }
        }
    }
    
    private String generateFinalReport() throws IOException {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
            .format(new java.util.Date());
        String filename = "election_report_" + timestamp + ".txt";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("═".repeat(70));
            writer.println("              DISTRIBUTED VOTING SYSTEM");
            writer.println("                  FINAL ELECTION REPORT");
            writer.println("═".repeat(70));
            writer.println();
            writer.println("Generated: " + new java.util.Date());
            writer.println();
            
            writer.println("─".repeat(70));
            writer.println("ELECTION QUESTION:");
            writer.println("─".repeat(70));
            writer.println(currentElection.getQuestion());
            writer.println();
            
            writer.println("─".repeat(70));
            writer.println("RESULTS:");
            writer.println("─".repeat(70));
            
            int totalVotes = voteCount.values().stream().mapToInt(Integer::intValue).sum();
            
         
            List<Map.Entry<String, Integer>> sortedResults = new ArrayList<>(voteCount.entrySet());
            sortedResults.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            
            int rank = 1;
            for (Map.Entry<String, Integer> entry : sortedResults) {
                int votes = entry.getValue();
                double percentage = totalVotes > 0 ? (votes * 100.0) / totalVotes : 0;
                writer.printf("%d. %-40s: %5d votes (%6.2f%%)%n", 
                    rank++, entry.getKey(), votes, percentage);
            }
            
            writer.println();
            writer.println("─".repeat(70));
            writer.printf("TOTAL VOTES CAST: %d%n", totalVotes);
            writer.printf("UNIQUE VOTERS (by CPF): %d%n", registeredCPFs.size());
            writer.println("─".repeat(70));
            
            writer.println();
            writer.println("═".repeat(70));
            writer.println("              End of Report");
            writer.println("═".repeat(70));
        }
        
        return filename;
    }
    
    private void shutdownServer() {
        serverRunning = false;
        if (updateTimer != null) {
            updateTimer.stop();
        }
        addLog(" Server shutdown initiated");
    }
    
 
    
    public void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss")
                .format(new java.util.Date());
            txtLogs.append("[" + timestamp + "] " + message + "\n");
            txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
        });
    }
    
    public synchronized boolean registerVote(Vote vote) {
        if (currentElection == null) {
            addLog(" Vote rejected: No election loaded");
            return false;
        }
        
        // Check for duplicate CPF
        if (registeredCPFs.contains(vote.getCpf())) {
            addLog(" Vote rejected: CPF " + maskCPF(vote.getCpf()) + " already voted");
            return false;
        }
        
        // Validate option exists
        if (!currentElection.getOptions().contains(vote.getChosenOption())) {
            addLog(" Vote rejected: Invalid option");
            return false;
        }
        
        // Register vote
        registeredCPFs.add(vote.getCpf());
        voteCount.merge(vote.getChosenOption(), 1, Integer::sum);
        addLog(" Vote registered: " + vote.getChosenOption() + 
               " (CPF: " + maskCPF(vote.getCpf()) + ")");
        
        return true;
    }
    
    public ElectionData getElectionData() {
        return currentElection;
    }
    
    private String maskCPF(String cpf) {
        if (cpf == null || cpf.length() < 6) return "***";
        return "***." + cpf.substring(3, 6) + ".***-**";
    }
    

    
    private void showHelpDialog() {
        String helpText = """
            <html>
            <body style='width: 450px; padding: 10px; font-family: Arial;'>
            <h2 style='color: #2980b9;'>Distributed Voting System - Server Guide</h2>
            
            <h3>Getting Started:</h3>
            <ol>
                <li><b>Start Server:</b> Click 'Start Server' to begin accepting client connections on the network.</li>
                <li><b>Load Election:</b> Load an election data file (.ser) containing the question and voting options.</li>
                <li><b>Monitor Voting:</b> Watch real-time results and server logs as clients connect and cast their votes.</li>
                <li><b>End Election:</b> Generate the final report with complete results and voter statistics.</li>
            </ol>
            
            <h3>Features:</h3>
            <ul>
                <li><b>Real-time Updates:</b> Vote counts update automatically every second with visual progress bars</li>
                <li><b>CPF Validation:</b> Prevents duplicate voting using CPF verification</li>
                <li><b>Activity Logging:</b> Complete server activity log with timestamps</li>
                <li><b>Multi-threaded:</b> Handles multiple simultaneous client connections</li>
                <li><b>Final Reports:</b> Generates detailed text reports with all voting statistics</li>
            </ul>
            
            <h3>Technical Requirements:</h3>
            <ul>
                <li>Election data must be serialized in .ser format</li>
                <li>Server must be started before loading election data</li>
                <li>Uses TCP/IP communication with ObjectStreams</li>
                <li>Default listening port is configured in NetProtocol</li>
            </ul>
            
            <h3>Security Features:</h3>
            <ul>
                <li>CPF validation to ensure voter authenticity</li>
                <li>Duplicate vote prevention</li>
                <li>Option validation against loaded election data</li>
                <li>Thread-safe vote counting using ConcurrentHashMap</li>
            </ul>
            </body>
            </html>
            """;
        
        JEditorPane editorPane = new JEditorPane("text/html", helpText);
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(550, 450));
        
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Help",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    private void showCreditsDialog() {
        String creditsText = """
            <html>
            <body style='width: 400px; text-align: center; padding: 20px; font-family: Arial;'>
                <h1 style='color: #2980b9;'>Distributed Voting System</h1>
                <h3 style='color: #7f8c8d;'>Version 1.0  2025</h3>
                <br>
                
                <p><b>SI400  Object-Oriented Programming II</b><br>
                Java Programming<br>/p>
                <br>
                
                <h3 style='color: #34495e;'>Project Team:</h3>
                <p>
                 Gabriel Colombo  RA: 283993<br>
                 Leonardo Bonfá Schroeder  RA: 289156<br>
                 Raissa Toassa Martinelli  RA: 184404<br>
                 Antonio Carlos Rosendo  RA: 174258<br>
                 Isabella Julia dos Santos RA: 169048<br>
                </p>
                <br>
            
                
                <p style='font-size: 11px; color: #95a5a6; margin-top: 20px;'>
                © 2025 - Educational Project<br>
                All rights reserved
                </p>
            </body>
            </html>
            """;
        
        JEditorPane editorPane = new JEditorPane("text/html", creditsText);
        editorPane.setEditable(false);
        editorPane.setCaretPosition(0);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        
        JOptionPane.showMessageDialog(
            this,
            editorPane,
            "Credits",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
 
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            ServerGUI gui = new ServerGUI();
            gui.setVisible(true);
        });
    }
}