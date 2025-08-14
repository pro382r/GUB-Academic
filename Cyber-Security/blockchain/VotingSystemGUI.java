import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Represents a single block in the blockchain (GMN)
class Block {
    public int index;
    public long timestamp;
    public String data; // Stores the vote data (e.g., "VoterID:123, Candidate:A")
    public String previousHash;
    public String hash;
    public int nonce; // A number used in proof-of-work mining

    // Constructor for a new block
    public Block(int index, String data, String previousHash) {
        this.index = index;
        this.timestamp = new Date().getTime();
        this.data = data;
        this.previousHash = previousHash;
        this.nonce = 0; // Initialize nonce
        this.hash = calculateHash(); // Calculate initial hash
    }

    // Calculates the SHA-256 hash of the block's contents
    public String calculateHash() {
        String calculatedhash = applySha256(
                index +
                timestamp +
                nonce +
                previousHash +
                data
        );
        return calculatedhash;
    }

    // Mines the block by finding a hash that meets the difficulty requirement (Proof-of-Work)
    public void mineBlock(int difficulty, JTextArea logArea) {
        String target = new String(new char[difficulty]).replace('\0', '0'); // Target hash prefix (e.g., "000")
        logArea.append("Mining block " + index + "...\n");
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++; // Increment nonce until a valid hash is found
            hash = calculateHash();
            // In a real scenario, this loop might take time. For demo, difficulty is low.
        }
        logArea.append("Block " + index + " Mined! Hash: " + hash + "\n");
    }

    // Helper method to apply SHA-256 hashing
    private String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        // Safely truncate hash strings for display, show full hash if less than 10 chars
        String displayPreviousHash = previousHash.length() > 10 ? previousHash.substring(0, 10) + "..." : previousHash;
        String displayHash = hash.length() > 10 ? hash.substring(0, 10) + "..." : hash;

        return "Block #" + index + "\n" +
               "  Timestamp: " + new Date(timestamp) + "\n" +
               "  Data: " + data + "\n" +
               "  Previous Hash: " + displayPreviousHash + "\n" +
               "  Hash: " + displayHash + "\n" +
               "  Nonce: " + nonce + "\n";
    }
}

// Represents the entire blockchain
class Blockchain {
    public ArrayList<Block> chain;
    public int difficulty; // Number of leading zeros required for a valid hash

    // Constructor for the blockchain, creates the genesis block
    public Blockchain(int difficulty) {
        this.difficulty = difficulty;
        chain = new ArrayList<>();
        // Create the genesis block (the first block in the chain)
        createGenesisBlock();
    }

    private void createGenesisBlock() {
        Block genesisBlock = new Block(0, "Genesis Block", "0");
        chain.add(genesisBlock); // Genesis block doesn't need mining in this simple example, or you can mine it with difficulty 0
    }

    // Returns the last block in the chain
    public Block getLastBlock() {
        return chain.get(chain.size() - 1);
    }

    // Adds a new block to the blockchain after mining it
    public void addBlock(Block newBlock, JTextArea logArea) {
        newBlock.previousHash = getLastBlock().hash;
        newBlock.mineBlock(difficulty, logArea);
        chain.add(newBlock);
    }

    // Validates the entire blockchain
    public boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;

        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        // Loop through blockchain to check hashes
        for (int i = 1; i < chain.size(); i++) {
            currentBlock = chain.get(i);
            previousBlock = chain.get(i - 1);

            // Compare registered hash and calculated hash
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("Current Hashes not equal");
                return false;
            }
            // Compare previous hash and registered previous hash
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
            // Check if hash is mined (meets difficulty criteria)
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }
        }
        return true;
    }
}

public class VotingSystemGUI extends JFrame {

    private Blockchain blockchain;
    private JTextField voterIdField;
    private ButtonGroup candidateGroup;
    private JTextArea blockchainDisplayArea;
    private JTextArea resultDisplayArea;
    private JTextArea logArea;
    private Map<String, Integer> voteCounts;
    private ExecutorService executorService; // For asynchronous mining
    private JPanel topPanel; // Declared as instance variable

    public VotingSystemGUI() {
        setTitle("Blockchain Voting System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null); // Center the window

        // Initialize blockchain with a low difficulty for faster demonstration
        // Higher difficulty means more mining time, making the demo slow.
        blockchain = new Blockchain(2); // Difficulty 2 means hash must start with "00"
        voteCounts = new HashMap<>();
        voteCounts.put("A", 0);
        voteCounts.put("B", 0);
        voteCounts.put("C", 0);

        executorService = Executors.newSingleThreadExecutor(); // For managing mining tasks

        initComponents();
        updateBlockchainDisplay(); // Display genesis block initially
        updateResultDisplay();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // Add some padding

        // --- Top Panel: Voter Input ---
        topPanel = new JPanel(new GridBagLayout()); // Initialized here
        topPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Cast Your Vote"));
        topPanel.setBackground(new Color(240, 248, 255)); // AliceBlue
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding around components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Voter ID:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        voterIdField = new JTextField(15);
        topPanel.add(voterIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(new JLabel("Choose Candidate:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        JPanel candidatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        candidateGroup = new ButtonGroup();
        JRadioButton radioA = new JRadioButton("Candidate A");
        JRadioButton radioB = new JRadioButton("Candidate B");
        JRadioButton radioC = new JRadioButton("Candidate C");

        radioA.setActionCommand("A");
        radioB.setActionCommand("B");
        radioC.setActionCommand("C");

        candidateGroup.add(radioA);
        candidateGroup.add(radioB);
        candidateGroup.add(radioC);

        candidatePanel.add(radioA);
        candidatePanel.add(radioB);
        candidatePanel.add(radioC);
        topPanel.add(candidatePanel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JButton voteButton = new JButton("Vote");
        voteButton.setFont(new Font("Arial", Font.BOLD, 14));
        voteButton.setBackground(new Color(60, 179, 113)); // MediumSeaGreen
        voteButton.setForeground(Color.WHITE);
        voteButton.setFocusPainted(false);
        voteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                castVote();
            }
        });
        topPanel.add(voteButton, gbc);

        add(topPanel, BorderLayout.NORTH);

        // --- Center Panel: Blockchain Display & Results ---
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setResizeWeight(0.7); // Blockchain display takes more space

        // Blockchain Display Area
        JPanel blockchainPanel = new JPanel(new BorderLayout());
        blockchainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Blockchain Ledger"));
        blockchainDisplayArea = new JTextArea();
        blockchainDisplayArea.setEditable(false);
        blockchainDisplayArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        blockchainPanel.add(new JScrollPane(blockchainDisplayArea), BorderLayout.CENTER);
        centerSplitPane.setLeftComponent(blockchainPanel);

        // Result Display Area
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Voting Results"));
        resultDisplayArea = new JTextArea();
        resultDisplayArea.setEditable(false);
        resultDisplayArea.setFont(new Font("Arial", Font.BOLD, 14));
        resultPanel.add(new JScrollPane(resultDisplayArea), BorderLayout.CENTER);
        centerSplitPane.setRightComponent(resultPanel);

        add(centerSplitPane, BorderLayout.CENTER);

        // --- Bottom Panel: Log Area ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "System Log"));
        logArea = new JTextArea(5, 40); // 5 rows, 40 columns
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        logArea.setForeground(Color.BLUE);
        bottomPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void castVote() {
        String voterId = voterIdField.getText().trim();
        String candidate = candidateGroup.getSelection() != null ? candidateGroup.getSelection().getActionCommand() : null;

        if (voterId.isEmpty() || candidate == null) {
            JOptionPane.showMessageDialog(this, "Please enter Voter ID and select a candidate.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable vote button during mining
        // Now topPanel is an instance variable, so it's accessible.
        JButton voteButton = (JButton) topPanel.getComponent(topPanel.getComponentCount() - 1);
        voteButton.setEnabled(false);
        logArea.append("Attempting to cast vote for Voter ID: " + voterId + ", Candidate: " + candidate + "...\n");

        // Execute mining in a separate thread to keep GUI responsive
        executorService.submit(() -> {
            try {
                // Check if voter has already voted (simple check, not robust for real system)
                // In a real system, you'd check a separate ledger or a specific transaction for this.
                boolean voterAlreadyVoted = blockchain.chain.stream()
                        .anyMatch(block -> block.data.startsWith("VoterID:" + voterId));

                if (voterAlreadyVoted) {
                    JOptionPane.showMessageDialog(this, "Voter ID " + voterId + " has already voted!", "Vote Error", JOptionPane.WARNING_MESSAGE);
                    SwingUtilities.invokeLater(() -> {
                        JButton btn = (JButton) topPanel.getComponent(topPanel.getComponentCount() - 1);
                        btn.setEnabled(true);
                        logArea.append("Vote failed: Voter already voted.\n");
                    });
                    return;
                }

                String voteData = "VoterID:" + voterId + ", Candidate:" + candidate;
                Block newBlock = new Block(blockchain.chain.size(), voteData, ""); // Previous hash will be set by addBlock
                blockchain.addBlock(newBlock, logArea);

                SwingUtilities.invokeLater(() -> {
                    updateBlockchainDisplay();
                    updateVoteCounts(candidate);
                    updateResultDisplay();
                    logArea.append("Vote successful! Block added to chain.\n");
                    // Re-enable vote button
                    JButton btn = (JButton) topPanel.getComponent(topPanel.getComponentCount() - 1);
                    btn.setEnabled(true);
                    voterIdField.setText(""); // Clear voter ID field
                    candidateGroup.clearSelection(); // Clear candidate selection
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("Error during vote: " + ex.getMessage() + "\n");
                    JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    JButton btn = (JButton) topPanel.getComponent(topPanel.getComponentCount() - 1);
                    btn.setEnabled(true);
                });
            }
        });
    }

    private void updateBlockchainDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Block block : blockchain.chain) {
            sb.append(block.toString()).append("\n----------------------------------------\n");
        }
        blockchainDisplayArea.setText(sb.toString());
        // Scroll to the bottom to show the latest block
        blockchainDisplayArea.setCaretPosition(blockchainDisplayArea.getDocument().getLength());
    }

    private void updateVoteCounts(String candidate) {
        voteCounts.put(candidate, voteCounts.getOrDefault(candidate, 0) + 1);
    }

    private void updateResultDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Current Results ---\n");
        // Sort candidates by votes in descending order
        voteCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> sb.append(String.format("Candidate %s: %d votes\n", entry.getKey(), entry.getValue())));

        sb.append("\n-----------------------\n");
        if (blockchain.isChainValid()) {
            sb.append("Blockchain is VALID! ✅\n");
        } else {
            sb.append("Blockchain is INVALID! 🚨 (Tampering Detected)\n");
        }
        resultDisplayArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new VotingSystemGUI().setVisible(true);
        });
    }
}
