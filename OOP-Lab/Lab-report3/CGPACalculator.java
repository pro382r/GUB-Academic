import javax.swing.*;
import java.awt.*;

public class CGPACalculator {
    public static void main(String[] args) {
        JFrame f = new JFrame("CGPA");
        JTextField tSub = new JTextField(5);
        JTextArea tMks = new JTextArea(10, 20);
        JLabel lRes = new JLabel("CGPA: 0.00");
        JButton btn = new JButton("Calculate");

        JPanel top = new JPanel(); top.add(new JLabel("Subjects:")); top.add(tSub);
        JPanel bot = new JPanel(); bot.add(btn); bot.add(lRes);

        f.add(top, "North");
        f.add(new JScrollPane(tMks));
        f.add(bot, "South");

        btn.addActionListener(e -> {
            try {
                String[] lines = tMks.getText().trim().split("\\n");
                if (lines.length != Integer.parseInt(tSub.getText().trim())) throw new Exception();

                double total = 0;
                for (String s : lines) {
                    double m = Double.parseDouble(s.trim());
                    total += m < 40 ? 0 : Math.min(4.0, 2.0 + (int)((m - 40) / 5) * 0.25);
                }
                lRes.setText(String.format("CGPA: %.2f", total / lines.length));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "Check Inputs / Counts");
            }
        });

        f.setSize(400, 400);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(3);
        f.setVisible(true);
    }
}
