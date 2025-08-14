import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.UnsupportedEncodingException;

class Block {
    public String hash;
    public String previousHash;
    private String data; // the vote
    private long timeStamp;

    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        String input = previousHash + Long.toString(timeStamp) + data;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    // Getter for data (used in counting and display)
    public String getData() {
        return data;
    }

    // Setter for data (used in tampering demo)
    public void setData(String data) {
        this.data = data;
    }
}

class Blockchain {
    private ArrayList<Block> chain = new ArrayList<>();

    public Blockchain() {
        chain.add(createGenesisBlock());
    }

    private Block createGenesisBlock() {
        return new Block("Genesis Block", "0");
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public void addBlock(String vote) {
        Block newBlock = new Block(vote, getLatestBlock().hash);
        chain.add(newBlock);
    }

    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block current = chain.get(i);
            Block previous = chain.get(i - 1);
            if (!current.previousHash.equals(previous.hash)) {
                return false;
            }
            if (!current.hash.equals(current.calculateHash())) {
                return false;
            }
        }
        return true;
    }

    public int countVotes(String candidate) {
        int count = 0;
        for (Block b : chain) {
            if (b.getData().equals(candidate)) {
                count++;
            }
        }
        return count;
    }

    public ArrayList<Block> getChain() {
        return chain;
    }
}

public class VotingSystem extends JFrame {
    private Blockchain blockchain = new Blockchain();
    private JTextArea chainDisplay = new JTextArea();
    private JLabel resultA = new JLabel("A: 0");
    private JLabel resultB = new JLabel("B: 0");
    private JLabel resultC = new JLabel("C: 0");
    private JLabel topCandidate = new JLabel("Top Candidate: None");
    private JLabel validity = new JLabel("Chain Valid: Yes");

    public VotingSystem() {
        setTitle("Blockchain Voting System");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Upper side: Voting panel with buttons for candidates
        JPanel votingPanel = new JPanel();
        votingPanel.setLayout(new FlowLayout());
        JLabel voterLabel = new JLabel("Voter: Cast your vote for Candidate");
        JButton voteA = new JButton("A");
        JButton voteB = new JButton("B");
        JButton voteC = new JButton("C");
        JButton tamperButton = new JButton("Simulate Tamper (on second block)");

        voteA.addActionListener(new VoteListener("A"));
        voteB.addActionListener(new VoteListener("B"));
        voteC.addActionListener(new VoteListener("C"));

        tamperButton.addActionListener(e -> {
            if (blockchain.getChain().size() > 2) {
                // Tamper by changing data without updating hash
                Block blockToTamper = blockchain.getChain().get(1);
                blockToTamper.setData("Tampered Vote");
                // Do not recalculate hash to simulate detectable tampering
                updateDisplay();
            } else {
                JOptionPane.showMessageDialog(this, "Need at least two votes to tamper the second one.");
            }
        });

        votingPanel.add(voterLabel);
        votingPanel.add(voteA);
        votingPanel.add(voteB);
        votingPanel.add(voteC);
        votingPanel.add(tamperButton);

        add(votingPanel, BorderLayout.NORTH);

        // Center: Blockchain display
        chainDisplay.setEditable(false);
        add(new JScrollPane(chainDisplay), BorderLayout.CENTER);

        // Bottom: Results panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new GridLayout(2, 3));
        resultsPanel.add(resultA);
        resultsPanel.add(resultB);
        resultsPanel.add(resultC);
        resultsPanel.add(topCandidate);
        resultsPanel.add(validity);

        add(resultsPanel, BorderLayout.SOUTH);

        updateDisplay();
    }

    private void updateDisplay() {
        StringBuilder sb = new StringBuilder();
        for (Block b : blockchain.getChain()) {
            sb.append("Block Data (Vote): ").append(b.getData()).append("\n");
            sb.append("Previous Hash: ").append(b.previousHash).append("\n");
            sb.append("Hash: ").append(b.hash).append("\n\n");
        }
        chainDisplay.setText(sb.toString());

        int countA = blockchain.countVotes("A");
        int countB = blockchain.countVotes("B");
        int countC = blockchain.countVotes("C");

        resultA.setText("A: " + countA);
        resultB.setText("B: " + countB);
        resultC.setText("C: " + countC);

        // Find top candidate
        Map<String, Integer> votesMap = new HashMap<>();
        votesMap.put("A", countA);
        votesMap.put("B", countB);
        votesMap.put("C", countC);

        String top = "None";
        int maxVotes = -1;
        for (Map.Entry<String, Integer> entry : votesMap.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                top = entry.getKey();
            }
        }
        topCandidate.setText("Top Candidate: " + top);

        validity.setText("Chain Valid: " + (blockchain.isChainValid() ? "Yes" : "No"));
    }

    class VoteListener implements ActionListener {
        private String candidate;

        public VoteListener(String candidate) {
            this.candidate = candidate;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            blockchain.addBlock(candidate);
            updateDisplay();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VotingSystem().setVisible(true));
    }
}