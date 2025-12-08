import java.awt.*;
import java.awt.event.ActionEvent;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;  

class Block {
    int index, nonce;
    long timestamp;
    String data, previousHash, hash;

    Block(int index, String data, String previousHash) {
        this.index = index;
        this.timestamp = new Date().getTime();
        this.data = data;
        this.previousHash = previousHash;
        this.hash = calculateHash();
    }

    String calculateHash() {
        return sha256(index + timestamp + nonce + previousHash + data);
    }

    void mineBlock(int difficulty, JTextArea logArea) {
        String target = "0".repeat(difficulty);
        logArea.append("Mining block " + index + "...\n");
        while (!hash.startsWith(target)) {
            nonce++;
            hash = calculateHash();
        }
        logArea.append("Block " + index + " Mined! Hash: " + hash + "\n");
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public String toString() {
        return String.format(
            "Block #%d\n  Timestamp: %s\n  Data: %s\n  Previous Hash: %s\n  Hash: %s\n  Nonce: %d\n",
            index, new Date(timestamp), data,
            shortHash(previousHash), shortHash(hash), nonce
        );
    }

    private static String shortHash(String h) {
        return h.length() > 10 ? h.substring(0, 10) + "..." : h;
    }
}

class Blockchain {
    List<Block> chain = new ArrayList<>();
    int difficulty;

    Blockchain(int difficulty) {
        this.difficulty = difficulty;
        chain.add(new Block(0, "Genesis Block", "0"));
    }

    Block lastBlock() { return chain.get(chain.size() - 1); }

    void addBlock(Block b, JTextArea logArea) {
        b.previousHash = lastBlock().hash;
        b.mineBlock(difficulty, logArea);
        chain.add(b);
    }

    boolean isValid() {
        String target = "0".repeat(difficulty);
        for (int i = 1; i < chain.size(); i++) {
            Block cur = chain.get(i), prev = chain.get(i - 1);
            if (!cur.hash.equals(cur.calculateHash()) ||
                !prev.hash.equals(cur.previousHash) ||
                !cur.hash.startsWith(target)) return false;
        }
        return true;
    }
}

public class VotingSystemGUI extends JFrame {
    Blockchain blockchain = new Blockchain(2);
    JTextField voterIdField;
    ButtonGroup candidateGroup;
    JTextArea blockchainArea, resultArea, logArea;
    Map<String, Integer> voteCounts = new HashMap<>(Map.of("A", 0, "B", 0, "C", 0));
    ExecutorService executor = Executors.newSingleThreadExecutor();
    JButton voteButton;

    VotingSystemGUI() {
        setTitle("Blockchain Voting System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        add(topPanel(), BorderLayout.NORTH);
        add(centerPane(), BorderLayout.CENTER);
        add(bottomPanel(), BorderLayout.SOUTH);
        updateDisplays();
    }

    private JPanel topPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Cast Your Vote"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        voterIdField = new JTextField(15);
        candidateGroup = new ButtonGroup();
        JPanel candidatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        for (String c : new String[]{"A", "B", "C"}) {
            JRadioButton rb = new JRadioButton("Candidate " + c);
            rb.setActionCommand(c);
            candidateGroup.add(rb);
            candidatePanel.add(rb);
        }
        voteButton = new JButton("Vote");
        voteButton.addActionListener(this::castVote);

        gbc.gridx = 0; p.add(new JLabel("Voter ID:"), gbc);
        gbc.gridx = 1; p.add(voterIdField, gbc);
        gbc.gridy = 1; gbc.gridx = 0; p.add(new JLabel("Choose Candidate:"), gbc);
        gbc.gridx = 1; p.add(candidatePanel, gbc);
        gbc.gridy = 2; p.add(voteButton, gbc);
        return p;
    }

    private JSplitPane centerPane() {
        blockchainArea = new JTextArea(); blockchainArea.setEditable(false);
        resultArea = new JTextArea(); resultArea.setEditable(false);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            scrollPanel(blockchainArea, "Blockchain Ledger"),
            scrollPanel(resultArea, "Voting Results"));
        split.setResizeWeight(0.7);
        return split;
    }

    private JPanel bottomPanel() {
        logArea = new JTextArea(5, 40); logArea.setEditable(false);
        return scrollPanel(logArea, "System Log");
    }

    private JPanel scrollPanel(JTextArea area, String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }

    private void castVote(ActionEvent e) {
        String voterId = voterIdField.getText().trim();
        String candidate = Optional.ofNullable(candidateGroup.getSelection())
                                   .map(ButtonModel::getActionCommand).orElse(null);
        if (voterId.isEmpty() || candidate == null) {
            JOptionPane.showMessageDialog(this, "Enter Voter ID & select a candidate.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        voteButton.setEnabled(false);
        logArea.append("Casting vote for " + voterId + " -> " + candidate + "\n");

        executor.submit(() -> {
            if (blockchain.chain.stream().anyMatch(b -> b.data.startsWith("VoterID:" + voterId))) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Voter already voted!", "Error", JOptionPane.WARNING_MESSAGE);
                    voteButton.setEnabled(true);
                });
                logArea.append("Vote failed: duplicate voter.\n");
                return;
            }
            blockchain.addBlock(new Block(blockchain.chain.size(), "VoterID:" + voterId + ", Candidate:" + candidate, ""), logArea);
            SwingUtilities.invokeLater(() -> {
                voteCounts.merge(candidate, 1, Integer::sum);
                updateDisplays();
                logArea.append("Vote successful!\n");
                voteButton.setEnabled(true);
                voterIdField.setText("");
                candidateGroup.clearSelection();
            });
        });
    }

    private void updateDisplays() {
        blockchainArea.setText(String.join("\n----------------------------------------\n",
            blockchain.chain.stream().map(Block::toString).toList()));
        resultArea.setText(String.format("--- Current Results ---\n%s\n%s",
            voteCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> "Candidate " + e.getKey() + ": " + e.getValue() + " votes")
                .reduce("", (a, b) -> a + b + "\n"),
            blockchain.isValid() ? "Blockchain is VALID! ✅" : "Blockchain INVALID!"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VotingSystemGUI().setVisible(true));
    }
}
//rz-final
