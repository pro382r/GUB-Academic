import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Blockchain Voting System (Swing)
 * --------------------------------
 * - Candidates: A, B, C
 * - Each vote becomes a Block linked to the previous hash
 * - The chain is displayed; results are tallied live
 * - Basic protections: prevents duplicate voting by voter ID; chain verification
 * - Simple proof-of-work so tampering becomes obvious (adjust DIFFICULTY to taste)
 *
 * Build & Run (Terminal):
 *   javac BlockchainVotingApp.java && java BlockchainVotingApp
 */
public class BlockchainVotingApp extends JFrame {
    // --- Blockchain Core ---
    static class Block {
        final int index;
        final long timestamp;
        final String voterId;
        final String candidate; // "A", "B", or "C"
        final String previousHash;
        long nonce = 0L;
        String hash;

        Block(int index, String voterId, String candidate, String previousHash) {
            this.index = index;
            this.voterId = voterId;
            this.candidate = candidate;
            this.previousHash = previousHash;
            this.timestamp = System.currentTimeMillis();
            this.hash = computeHash();
        }

        String computeHash() {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                String data = index + "|" + timestamp + "|" + voterId + "|" + candidate + "|" + previousHash + "|" + nonce;
                byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) sb.append(String.format("%02x", b));
                return sb.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void mine(int difficulty) {
            String prefix = "0".repeat(Math.max(difficulty, 0));
            while (!hash.startsWith(prefix)) {
                nonce++;
                hash = computeHash();
            }
        }
    }

    static class Blockchain {
        private static final int DIFFICULTY = 3; // increase for slower, harder mining
        private final java.util.List<Block> chain = new ArrayList<>();
        private final Set<String> voted = new HashSet<>(); // voterId -> has voted

        Blockchain() {
            // Genesis block
            Block genesis = new Block(0, "GENESIS", "-", "0");
            genesis.mine(DIFFICULTY);
            chain.add(genesis);
        }

        synchronized boolean hasVoted(String voterId) {
            return voted.contains(voterId.toLowerCase(Locale.ROOT));
        }

        synchronized Block addVote(String voterId, String candidate) {
            if (voterId == null || voterId.trim().isEmpty()) throw new IllegalArgumentException("Voter ID required");
            voterId = voterId.trim();
            if (hasVoted(voterId)) throw new IllegalStateException("This voter has already voted");
            String prevHash = chain.get(chain.size() - 1).hash;
            Block b = new Block(chain.size(), voterId, candidate, prevHash);
            b.mine(DIFFICULTY);
            chain.add(b);
            voted.add(voterId.toLowerCase(Locale.ROOT));
            return b;
        }

        synchronized boolean isValid() {
            String prefix = "0".repeat(Math.max(DIFFICULTY, 0));
            for (int i = 1; i < chain.size(); i++) {
                Block curr = chain.get(i);
                Block prev = chain.get(i - 1);
                if (!curr.hash.equals(curr.computeHash())) return false;           // data integrity
                if (!curr.hash.startsWith(prefix)) return false;                   // proof of work
                if (!curr.previousHash.equals(prev.hash)) return false;            // linkage
            }
            return true;
        }

        synchronized java.util.List<Block> getBlocks() { return Collections.unmodifiableList(chain); }

        synchronized Map<String, Integer> tally() {
            Map<String, Integer> map = new HashMap<>();
            map.put("A", 0); map.put("B", 0); map.put("C", 0);
            for (Block b : chain) {
                if (b.index == 0) continue; // skip genesis
                map.put(b.candidate, map.getOrDefault(b.candidate, 0) + 1);
            }
            return map;
        }
    }

    // --- UI ---
    private final Blockchain blockchain = new Blockchain();

    private final JTextField voterField = new JTextField();
    private final JRadioButton candA = new JRadioButton("A");
    private final JRadioButton candB = new JRadioButton("B");
    private final JRadioButton candC = new JRadioButton("C");

    private final JButton voteBtn = new JButton("Cast Vote");
    private final JButton verifyBtn = new JButton("Verify Chain");
    private final JLabel statusLabel = new JLabel("Ready");

    private final JTable resultsTable = new JTable();
    private final DefaultTableModel resultsModel = new DefaultTableModel(new Object[]{"Candidate","Votes"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JPanel chainPanel = new JPanel();

    public BlockchainVotingApp() {
        super("Blockchain Voting – A/B/C");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        // Top: voter input, candidate choice, actions, results
        JPanel top = new JPanel(new BorderLayout(10,10));
        top.setBorder(new EmptyBorder(10,10,10,10));

        // Left: Voter + Candidate selectors
        JPanel votePanel = new JPanel();
        votePanel.setLayout(new BoxLayout(votePanel, BoxLayout.Y_AXIS));
        votePanel.setBorder(new TitledBorder("Voter & Candidate"));
        voterField.setColumns(20);
        JPanel voterRow = row(new JLabel("Voter ID:"), voterField);
        ButtonGroup group = new ButtonGroup();
        group.add(candA); group.add(candB); group.add(candC);
        candA.setSelected(true);
        JPanel candRow = row(new JLabel("Choose Candidate:"), candA, candB, candC);
        JPanel actions = row(voteBtn, verifyBtn);
        votePanel.add(voterRow); votePanel.add(Box.createVerticalStrut(6));
        votePanel.add(candRow); votePanel.add(Box.createVerticalStrut(6));
        votePanel.add(actions);

        // Right: Results table + top candidate label
        JPanel results = new JPanel(new BorderLayout(6,6));
        results.setBorder(new TitledBorder("Results (Top Candidate Up)"));
        resultsTable.setModel(resultsModel);
        resultsTable.setRowHeight(22);
        resultsTable.setFillsViewportHeight(true);
        results.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        JLabel topLabel = new JLabel("Top: —", SwingConstants.CENTER);
        topLabel.setFont(topLabel.getFont().deriveFont(Font.BOLD, 16f));
        results.add(topLabel, BorderLayout.NORTH);

        top.add(votePanel, BorderLayout.WEST);
        top.add(results, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);

        // Bottom: Blockchain visualization panel
        chainPanel.setLayout(new GridBagLayout());
        chainPanel.setBorder(new TitledBorder("Blockchain (newest on the right)"));
        JScrollPane chainScroller = new JScrollPane(chainPanel);
        add(chainScroller, BorderLayout.CENTER);

        // Status bar
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(new EmptyBorder(4,10,4,10));
        statusLabel.setForeground(new Color(0, 102, 0));
        status.add(statusLabel, BorderLayout.WEST);
        add(status, BorderLayout.SOUTH);

        // Events
        voteBtn.addActionListener(e -> onCastVote(topLabel));
        verifyBtn.addActionListener(e -> onVerify());

        // Init tables & chain view
        refreshResults(topLabel);
        redrawChain();
    }

    private JPanel row(Component... comps) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (Component c : comps) p.add(c);
        return p;
    }

    private void onCastVote(JLabel topLabel) {
        String voter = voterField.getText().trim();
        String candidate = candA.isSelected() ? "A" : candB.isSelected() ? "B" : "C";

        if (voter.isEmpty()) {
            warn("Please enter a Voter ID.");
            return;
        }

        setBusy(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            String message = "";
            @Override protected Void doInBackground() {
                try {
                    blockchain.addVote(voter, candidate);
                    message = "Vote recorded for candidate " + candidate + ".";
                } catch (IllegalStateException dup) {
                    message = "Duplicate vote blocked: this voter already voted.";
                } catch (Exception ex) {
                    message = "Error: " + ex.getMessage();
                }
                return null;
            }
            @Override protected void done() {
                setBusy(false);
                info(message);
                refreshResults(topLabel);
                redrawChain();
            }
        };
        worker.execute();
    }

    private void onVerify() {
        boolean ok = blockchain.isValid();
        if (ok) info("Chain valid ✅"); else warn("Chain INVALID ❌ (data was tampered)");
    }

    private void refreshResults(JLabel topLabel) {
        Map<String, Integer> tallies = blockchain.tally();
        // Sort by votes desc with A/B/C tie-breaker
        java.util.List<Map.Entry<String,Integer>> list = new ArrayList<>(tallies.entrySet());
        list.sort((e1, e2) -> {
            int c = Integer.compare(e2.getValue(), e1.getValue());
            if (c != 0) return c;
            return e1.getKey().compareTo(e2.getKey());
        });
        resultsModel.setRowCount(0);
        for (Map.Entry<String,Integer> e : list) resultsModel.addRow(new Object[]{e.getKey(), e.getValue()});
        String top = list.isEmpty() ? "—" : list.get(0).getKey() + " (" + list.get(0).getValue() + ")";
        topLabel.setText("Top: " + top);
    }

    private void redrawChain() {
        chainPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridy = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.List<Block> blocks = blockchain.getBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            Block b = blocks.get(i);
            JPanel card = makeBlockCard(b, sdf);
            gbc.gridx = i * 2; // leave space for arrow
            chainPanel.add(card, gbc);
            if (i < blocks.size() - 1) {
                JLabel arrow = new JLabel("→");
                arrow.setFont(arrow.getFont().deriveFont(Font.BOLD, 20f));
                gbc.gridx = i * 2 + 1;
                chainPanel.add(arrow, gbc);
            }
        }
        chainPanel.revalidate();
        chainPanel.repaint();
    }

    private JPanel makeBlockCard(Block b, SimpleDateFormat sdf) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(260, 170));
        card.setBorder(new CompoundBorder(new LineBorder(new Color(180,180,180), 1, true), new EmptyBorder(8,8,8,8)));

        JLabel title = new JLabel("Block #" + b.index);
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        card.add(title);

        String time = sdf.format(new Date(b.timestamp));
        String voter = (b.index == 0) ? "—" : b.voterId;
        String cand = (b.index == 0) ? "—" : b.candidate;

        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setText("Time: " + time +
                "\nVoter: " + voter +
                "\nCandidate: " + cand +
                "\nPrevHash: " + abbreviate(b.previousHash) +
                "\nNonce: " + b.nonce +
                "\nHash: " + abbreviate(b.hash));
        info.setBackground(card.getBackground());
        card.add(Box.createVerticalStrut(6));
        card.add(info);
        return card;
    }

    private String abbreviate(String h) {
        if (h == null) return "";
        if (h.length() <= 16) return h;
        return h.substring(0, 8) + "…" + h.substring(h.length() - 8);
    }

    private void info(String msg) {
        statusLabel.setText(msg);
        statusLabel.setForeground(new Color(0, 102, 0));
    }

    private void warn(String msg) {
        statusLabel.setText(msg);
        statusLabel.setForeground(new Color(170, 0, 0));
    }

    private void setBusy(boolean busy) {
        voteBtn.setEnabled(!busy);
        verifyBtn.setEnabled(!busy);
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BlockchainVotingApp().setVisible(true));
    }
}
