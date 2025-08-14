import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockchainVotingSystem {
    private JFrame frame;
    private JPanel mainPanel, votingPanel, blockchainPanel, resultsPanel;
    private JButton voteButton;
    private JComboBox<String> voterComboBox, candidateComboBox;
    private JTextArea blockchainTextArea;
    private JLabel resultLabel;
    
    private List<Block> blockchain;
    private Map<String, Integer> voteCount;
    
    public BlockchainVotingSystem() {
        initialize();
        setupUI();
    }
    
    private void initialize() {
        blockchain = new ArrayList<>();
        voteCount = new HashMap<>();
        voteCount.put("Candidate A", 0);
        voteCount.put("Candidate B", 0);
        voteCount.put("Candidate C", 0);
        
        // Create genesis block
        Block genesis = new Block("0", "Genesis Block");
        blockchain.add(genesis);
    }
    
    private void setupUI() {
        frame = new JFrame("Blockchain Voting System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        
        mainPanel = new JPanel(new GridLayout(3, 1));
        
        // Voting Panel
        votingPanel = new JPanel(new FlowLayout());
        voterComboBox = new JComboBox<>(new String[]{"Voter 1", "Voter 2", "Voter 3", "Voter 4", "Voter 5"});
        candidateComboBox = new JComboBox<>(new String[]{"Candidate A", "Candidate B", "Candidate C"});
        voteButton = new JButton("Cast Vote");
        
        voteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                castVote();
            }
        });
        
        votingPanel.add(new JLabel("Voter: "));
        votingPanel.add(voterComboBox);
        votingPanel.add(new JLabel("Candidate: "));
        votingPanel.add(candidateComboBox);
        votingPanel.add(voteButton);
        
        // Results Panel
        resultsPanel = new JPanel(new FlowLayout());
        resultLabel = new JLabel("Current Results: A - 0, B - 0, C - 0");
        resultsPanel.add(resultLabel);
        
        // Blockchain Panel
        blockchainPanel = new JPanel(new BorderLayout());
        blockchainTextArea = new JTextArea();
        blockchainTextArea.setEditable(false);
        blockchainTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(blockchainTextArea);
        blockchainPanel.add(new JLabel("Blockchain:"), BorderLayout.NORTH);
        blockchainPanel.add(scrollPane, BorderLayout.CENTER);
        
        updateBlockchainDisplay();
        updateResults();
        
        mainPanel.add(votingPanel);
        mainPanel.add(resultsPanel);
        mainPanel.add(blockchainPanel);
        
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
    
    private void castVote() {
        String voter = (String) voterComboBox.getSelectedItem();
        String candidate = (String) candidateComboBox.getSelectedItem();
        
        // Create new block with the vote
        String previousHash = blockchain.get(blockchain.size() - 1).hash;
        String data = voter + " voted for " + candidate;
        Block newBlock = new Block(previousHash, data);
        blockchain.add(newBlock);
        
        // Update vote count
        voteCount.put(candidate, voteCount.get(candidate) + 1);
        
        // Update UI
        updateBlockchainDisplay();
        updateResults();
        
        JOptionPane.showMessageDialog(frame, "Vote cast successfully!");
    }
    
    private void updateBlockchainDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Block block : blockchain) {
            sb.append("Block #").append(blockchain.indexOf(block)).append("\n");
            sb.append("Previous Hash: ").append(block.previousHash).append("\n");
            sb.append("Data: ").append(block.data).append("\n");
            sb.append("Hash: ").append(block.hash).append("\n");
            sb.append("------------------------------------\n");
        }
        blockchainTextArea.setText(sb.toString());
    }
    
    private void updateResults() {
        String topCandidate = "Candidate A";
        int maxVotes = voteCount.get(topCandidate);
        
        for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                topCandidate = entry.getKey();
            }
        }
        
        resultLabel.setText(String.format(
            "Current Results: A - %d, B - %d, C - %d | Leading: %s",
            voteCount.get("Candidate A"),
            voteCount.get("Candidate B"),
            voteCount.get("Candidate C"),
            topCandidate
        ));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BlockchainVotingSystem();
            }
        });
    }
    
    private class Block {
        private String previousHash;
        private String data;
        private String hash;
        
        public Block(String previousHash, String data) {
            this.previousHash = previousHash;
            this.data = data;
            this.hash = calculateHash();
        }
        
        private String calculateHash() {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                String input = previousHash + data;
                byte[] hashBytes = digest.digest(input.getBytes());
                
                StringBuilder hexString = new StringBuilder();
                for (byte b : hashBytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
