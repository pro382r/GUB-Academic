import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Block class representing a single vote block
class VoteBlock {
    private String previousHash;
    private String timestamp;
    private String voterID;
    private String candidate;
    private int blockIndex;
    private String currentHash;
    
    public VoteBlock(String previousHash, String voterID, String candidate, int blockIndex) {
        this.previousHash = previousHash;
        this.voterID = voterID;
        this.candidate = candidate;
        this.blockIndex = blockIndex;
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.currentHash = calculateHash();
    }
    
    public String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String data = previousHash + timestamp + voterID + candidate + blockIndex;
            byte[] hash = digest.digest(data.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    // Getters
    public String getPreviousHash() { return previousHash; }
    public String getCurrentHash() { return currentHash; }
    public String getTimestamp() { return timestamp; }
    public String getVoterID() { return voterID; }
    public String getCandidate() { return candidate; }
    public int getBlockIndex() { return blockIndex; }
    
    @Override
    public String toString() {
        return String.format("Block #%d | Voter: %s | Vote: %s | Time: %s", 
                           blockIndex, voterID, candidate, timestamp);
    }
}

// Blockchain class to manage the chain of vote blocks
class VotingBlockchain {
    private ArrayList<VoteBlock> blockchain;
    private Map<String, Integer> voteCounts;
    private ArrayList<String> usedVoterIDs;
    
    public VotingBlockchain() {
        blockchain = new ArrayList<>();
        voteCounts = new HashMap<>();
        usedVoterIDs = new ArrayList<>();
        
        // Initialize vote counts
        voteCounts.put("Candidate A", 0);
        voteCounts.put("Candidate B", 0);
        voteCounts.put("Candidate C", 0);
        
        // Create genesis block
        createGenesisBlock();
    }
    
    private void createGenesisBlock() {
        VoteBlock genesisBlock = new VoteBlock("0", "GENESIS", "GENESIS", 0);
        blockchain.add(genesisBlock);
    }
    
    public boolean addVote(String voterID, String candidate) {
        // Check if voter has already voted
        if (usedVoterIDs.contains(voterID)) {
            return false;
        }
        
        String previousHash = getLatestBlock().getCurrentHash();
        int newIndex = blockchain.size();
        
        VoteBlock newBlock = new VoteBlock(previousHash, voterID, candidate, newIndex);
        blockchain.add(newBlock);
        
        // Update vote counts and used voter IDs
        voteCounts.put(candidate, voteCounts.get(candidate) + 1);
        usedVoterIDs.add(voterID);
        
        return true;
    }
    
    public VoteBlock getLatestBlock() {
        return blockchain.get(blockchain.size() - 1);
    }
    
    public boolean isChainValid() {
        for (int i = 1; i < blockchain.size(); i++) {
            VoteBlock currentBlock = blockchain.get(i);
            VoteBlock previousBlock = blockchain.get(i - 1);
            
            // Check if current block's hash is valid
            if (!currentBlock.getCurrentHash().equals(currentBlock.calculateHash())) {
                return false;
            }
            
            // Check if current block points to previous block
            if (!currentBlock.getPreviousHash().equals(previousBlock.getCurrentHash())) {
                return false;
            }
        }
        return true;
    }
    
    public ArrayList<VoteBlock> getBlockchain() {
        return blockchain;
    }
    
    public Map<String, Integer> getVoteCounts() {
        return voteCounts;
    }
    
    public boolean hasVoted(String voterID) {
        return usedVoterIDs.contains(voterID);
    }
    
    public String getWinner() {
        String winner = "Candidate A";
        int maxVotes = voteCounts.get("Candidate A");
        
        if (voteCounts.get("Candidate B") > maxVotes) {
            winner = "Candidate B";
            maxVotes = voteCounts.get("Candidate B");
        }
        
        if (voteCounts.get("Candidate C") > maxVotes) {
            winner = "Candidate C";
        }
        
        return winner;
    }
}

// Main GUI class
public class BlockchainVotingSystem extends JFrame {
    private VotingBlockchain blockchain;
    private JTextField voterIDField;
    private JComboBox<String> candidateCombo;
    private JTextArea blockchainDisplay;
    private JLabel[] candidateLabels;
    private JLabel[] voteCountLabels;
    private JLabel winnerLabel;
    private JLabel chainStatusLabel;
    
    public BlockchainVotingSystem() {
        blockchain = new VotingBlockchain();
        initializeGUI();
        updateDisplay();
    }
    
    private void initializeGUI() {
        setTitle("Blockchain Voting System - Secure & Transparent");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main panels
        JPanel topPanel = createVotingPanel();
        JPanel centerPanel = createResultsPanel();
        JPanel bottomPanel = createBlockchainPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private JPanel createVotingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Cast Your Vote"));
        panel.setBackground(new Color(240, 248, 255));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Voter ID input
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Voter ID:"), gbc);
        
        gbc.gridx = 1;
        voterIDField = new JTextField(15);
        voterIDField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(voterIDField, gbc);
        
        // Candidate selection
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Select Candidate:"), gbc);
        
        gbc.gridx = 1;
        String[] candidates = {"Candidate A", "Candidate B", "Candidate C"};
        candidateCombo = new JComboBox<>(candidates);
        candidateCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(candidateCombo, gbc);
        
        // Vote button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton voteButton = new JButton("Cast Vote");
        voteButton.setFont(new Font("Arial", Font.BOLD, 16));
        voteButton.setBackground(new Color(34, 139, 34));
        voteButton.setForeground(Color.WHITE);
        voteButton.addActionListener(new VoteActionListener());
        panel.add(voteButton, gbc);
        
        return panel;
    }
    
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBorder(new TitledBorder("Live Results"));
        
        // Results display panel
        JPanel resultsGrid = new JPanel(new GridLayout(1, 3, 10, 10));
        resultsGrid.setBackground(new Color(248, 248, 255));
        
        candidateLabels = new JLabel[3];
        voteCountLabels = new JLabel[3];
        String[] candidates = {"Candidate A", "Candidate B", "Candidate C"};
        Color[] colors = {new Color(255, 99, 99), new Color(99, 255, 99), new Color(99, 99, 255)};
        
        for (int i = 0; i < 3; i++) {
            JPanel candidatePanel = new JPanel(new BorderLayout());
            candidatePanel.setBackground(colors[i]);
            candidatePanel.setBorder(BorderFactory.createRaisedBevelBorder());
            
            candidateLabels[i] = new JLabel(candidates[i], SwingConstants.CENTER);
            candidateLabels[i].setFont(new Font("Arial", Font.BOLD, 16));
            
            voteCountLabels[i] = new JLabel("0 votes", SwingConstants.CENTER);
            voteCountLabels[i].setFont(new Font("Arial", Font.PLAIN, 14));
            
            candidatePanel.add(candidateLabels[i], BorderLayout.CENTER);
            candidatePanel.add(voteCountLabels[i], BorderLayout.SOUTH);
            
            resultsGrid.add(candidatePanel);
        }
        
        // Winner and status panel
        JPanel statusPanel = new JPanel(new FlowLayout());
        statusPanel.setBackground(new Color(255, 255, 224));
        
        winnerLabel = new JLabel("Current Leader: None");
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        winnerLabel.setForeground(new Color(139, 69, 19));
        
        chainStatusLabel = new JLabel("Blockchain Status: VALID ✓");
        chainStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        chainStatusLabel.setForeground(new Color(0, 128, 0));
        
        statusPanel.add(winnerLabel);
        statusPanel.add(Box.createHorizontalStrut(50));
        statusPanel.add(chainStatusLabel);
        
        panel.add(resultsGrid);
        panel.add(statusPanel);
        
        return panel;
    }
    
    private JPanel createBlockchainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Blockchain - Immutable Vote Records"));
        
        blockchainDisplay = new JTextArea(12, 50);
        blockchainDisplay.setFont(new Font("Courier New", Font.PLAIN, 11));
        blockchainDisplay.setEditable(false);
        blockchainDisplay.setBackground(new Color(32, 32, 32));
        blockchainDisplay.setForeground(new Color(0, 255, 0));
        
        JScrollPane scrollPane = new JScrollPane(blockchainDisplay);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add verify button
        JButton verifyButton = new JButton("Verify Blockchain Integrity");
        verifyButton.setFont(new Font("Arial", Font.BOLD, 12));
        verifyButton.addActionListener(e -> verifyBlockchain());
        panel.add(verifyButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private class VoteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String voterID = voterIDField.getText().trim();
            String candidate = (String) candidateCombo.getSelectedItem();
            
            if (voterID.isEmpty()) {
                JOptionPane.showMessageDialog(BlockchainVotingSystem.this, 
                    "Please enter a Voter ID!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (blockchain.hasVoted(voterID)) {
                JOptionPane.showMessageDialog(BlockchainVotingSystem.this, 
                    "Voter ID '" + voterID + "' has already voted!\nBlockchain prevents double voting.", 
                    "Vote Rejected", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (blockchain.addVote(voterID, candidate)) {
                JOptionPane.showMessageDialog(BlockchainVotingSystem.this, 
                    "Vote successfully recorded!\nBlock added to blockchain.", 
                    "Vote Confirmed", JOptionPane.INFORMATION_MESSAGE);
                voterIDField.setText("");
                updateDisplay();
            }
        }
    }
    
    private void updateDisplay() {
        // Update vote counts
        Map<String, Integer> votes = blockchain.getVoteCounts();
        voteCountLabels[0].setText(votes.get("Candidate A") + " votes");
        voteCountLabels[1].setText(votes.get("Candidate B") + " votes");
        voteCountLabels[2].setText(votes.get("Candidate C") + " votes");
        
        // Update winner
        if (votes.values().stream().mapToInt(Integer::intValue).sum() > 0) {
            String winner = blockchain.getWinner();
            winnerLabel.setText("Current Leader: " + winner + " (" + votes.get(winner) + " votes)");
        }
        
        // Update blockchain display
        StringBuilder sb = new StringBuilder();
        sb.append("=== BLOCKCHAIN STRUCTURE ===\n\n");
        
        for (VoteBlock block : blockchain.getBlockchain()) {
            sb.append(String.format("┌─ BLOCK #%d ─────────────────────────────────┐\n", block.getBlockIndex()));
            sb.append(String.format("│ Previous Hash: %s...│\n", block.getPreviousHash().substring(0, Math.min(20, block.getPreviousHash().length()))));
            sb.append(String.format("│ Current Hash:  %s...│\n", block.getCurrentHash().substring(0, 20)));
            sb.append(String.format("│ Voter ID: %-20s              │\n", block.getVoterID()));
            sb.append(String.format("│ Vote: %-20s                  │\n", block.getCandidate()));
            sb.append(String.format("│ Timestamp: %-25s       │\n", block.getTimestamp()));
            sb.append("└─────────────────────────────────────────────┘\n");
            sb.append("                      ↓\n");
        }
        
        sb.append("\n*** Each block is cryptographically linked to prevent tampering ***");
        blockchainDisplay.setText(sb.toString());
        blockchainDisplay.setCaretPosition(blockchainDisplay.getDocument().getLength());
    }
    
    private void verifyBlockchain() {
        boolean isValid = blockchain.isChainValid();
        if (isValid) {
            chainStatusLabel.setText("Blockchain Status: VALID ✓");
            chainStatusLabel.setForeground(new Color(0, 128, 0));
            JOptionPane.showMessageDialog(this, 
                "Blockchain integrity verified!\nAll votes are secure and unaltered.", 
                "Verification Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            chainStatusLabel.setText("Blockchain Status: CORRUPTED ✗");
            chainStatusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, 
                "Blockchain integrity compromised!\nSome votes may have been tampered with.", 
                "Verification Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BlockchainVotingSystem().setVisible(true);
        });
    }
}
