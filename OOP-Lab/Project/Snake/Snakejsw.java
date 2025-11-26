import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.List;
/**
 * Snakejsw - Complete Single-File Snake Game
 * Fixed: Smooth navigation for Game Over and Escape keys.
 */
public class Snakejsw extends JFrame {
    public Snakejsw() {
        setTitle("Snake Game - Enhanced Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
       
        GamePanel gamePanel = new GamePanel();
        ScorePanel scorePanel = new ScorePanel(gamePanel);
        add(scorePanel, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);
        gamePanel.setScorePanel(scorePanel);
       
        pack(); // Size window to fit panel
        setLocationRelativeTo(null); // Center on screen
    }
    public static void main(String[] args) {
        // Run UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new Snakejsw().setVisible(true);
        });
    }
    // ==========================================
    // Core Game Panel (Controller + View)
    // ==========================================
    static class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
       
        // --- Constants & Config ---
        private static final int TILE_SIZE = 25;
        private static final int COLS = 30;
        private static final int ROWS = 20;
        private static final int BOARD_WIDTH = TILE_SIZE * COLS;
        private static final int BOARD_HEIGHT = TILE_SIZE * ROWS;
        private static final String DATA_FILE = "snake_data.dat";
        // --- Game State Enums ---
        private enum State { MENU, RUNNING, PAUSED, GAME_OVER, SETTINGS }
        private enum Mode { CLASSIC, NO_WALL, AI_MODE }
       
        // --- Runtime Variables ---
        private State state = State.MENU;
        private Mode gameMode = Mode.CLASSIC;
        private Timer gameTimer;
        private Timer shrinkTimer; // Controls lifetime of special food
        private Timer bombTimer; // Controls lifetime of bomb
       
        // Snake & Food
        private LinkedList<Point> snake;
        private Point normalFood;
        private Point shrinkFood; // Null if inactive
        private Point bomb; // Null if inactive
        private int direction; // 0:Up, 1:Right, 2:Down, 3:Left
        private boolean moveKeyProcessed = false; // Prevent double turn in one frame
        // Stats
        private int score;
        private int highScore;
        private int normalFoodEatenCount; // Tracks when to spawn special food
       
        // Settings
        private int gameSpeed = 100; // ms per frame (Lower is faster)
       
        // Visuals
        private List<FloatingText> floatingTexts = new ArrayList<>();
        private long shrinkSpawnTime; // For blinking effect
        private long bombSpawnTime; // For blinking effect
        private final int SHRINK_LIFETIME = 3000; // 3 seconds
        private final int BOMB_LIFETIME = 5000; // 5 seconds
       
        // AI Helper
        private Random random = new Random();

        // Score Panel Reference
        private ScorePanel scorePanel;

        public void setScorePanel(ScorePanel sp) {
            this.scorePanel = sp;
        }

        public State getState() {
            return state;
        }

        public int getScore() {
            return score;
        }

        public int getHighScore() {
            return highScore;
        }

        public Mode getGameMode() {
            return gameMode;
        }

        public Point getShrinkFood() {
            return shrinkFood;
        }

        public long getShrinkSpawnTime() {
            return shrinkSpawnTime;
        }

        public int getShrinkLifetime() {
            return SHRINK_LIFETIME;
        }

        public Point getBomb() {
            return bomb;
        }

        public long getBombSpawnTime() {
            return bombSpawnTime;
        }

        public int getBombLifetime() {
            return BOMB_LIFETIME;
        }

        public GamePanel() {
            setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
            setBackground(new Color(20, 20, 20)); // Dark Theme
            setFocusable(true);
            addKeyListener(this);
            addMouseListener(this);
            loadData(); // Load high score
            // Main Game Loop Timer
            gameTimer = new Timer(gameSpeed, this);
           
            // Special Food Timer (stops itself when time runs out)
            shrinkTimer = new Timer(SHRINK_LIFETIME, e -> {
                removeShrinkFood();
            });
            shrinkTimer.setRepeats(false);

            // Bomb Timer
            bombTimer = new Timer(BOMB_LIFETIME, e -> {
                removeBomb();
            });
            bombTimer.setRepeats(false);
        }
        // ============================
        // Game Logic
        // ============================
        private void startGame() {
            snake = new LinkedList<>();
            // Start in middle
            snake.add(new Point(COLS / 2, ROWS / 2));
            snake.add(new Point(COLS / 2, ROWS / 2 + 1)); // Body
            snake.add(new Point(COLS / 2, ROWS / 2 + 2)); // Tail
           
            direction = 0; // Up
            score = 0;
            normalFoodEatenCount = 0;
            floatingTexts.clear();
           
            spawnNormalFood();
            shrinkFood = null; // No special food at start
            bomb = null; // No bomb at start
            state = State.RUNNING;
            gameTimer.setDelay(gameSpeed);
            gameTimer.start();
        }
        private void stopGameToMenu() {
            gameTimer.stop();
            shrinkTimer.stop();
            bombTimer.stop();
            state = State.MENU;
            repaint();
        }
        private void spawnNormalFood() {
            while (true) {
                int x = random.nextInt(COLS);
                int y = random.nextInt(ROWS);
                Point p = new Point(x, y);
                // Ensure not on snake, not on shrink food, not on bomb
                if (!isPointOnSnake(p) && (shrinkFood == null || !p.equals(shrinkFood)) && (bomb == null || !p.equals(bomb))) {
                    normalFood = p;
                    break;
                }
            }
        }
        private void spawnShrinkFood() {
            while (true) {
                int x = random.nextInt(COLS);
                int y = random.nextInt(ROWS);
                Point p = new Point(x, y);
                if (!isPointOnSnake(p) && !p.equals(normalFood) && (bomb == null || !p.equals(bomb))) {
                    shrinkFood = p;
                    shrinkSpawnTime = System.currentTimeMillis();
                    shrinkTimer.restart(); // Start the 3s countdown
                    addFloatingText("Special Food!", p.x * TILE_SIZE, p.y * TILE_SIZE, Color.YELLOW);
                    break;
                }
            }
        }
        private void removeShrinkFood() {
            if (shrinkFood != null) {
                shrinkFood = null;
                shrinkTimer.stop();
            }
        }
        private void spawnBomb() {
            while (true) {
                int x = random.nextInt(COLS);
                int y = random.nextInt(ROWS);
                Point p = new Point(x, y);
                if (!isPointOnSnake(p) && !p.equals(normalFood) && (shrinkFood == null || !p.equals(shrinkFood))) {
                    bomb = p;
                    bombSpawnTime = System.currentTimeMillis();
                    bombTimer.restart(); // Start the 5s countdown
                    addFloatingText("Bomb!", p.x * TILE_SIZE, p.y * TILE_SIZE, Color.RED);
                    break;
                }
            }
        }
        private void removeBomb() {
            if (bomb != null) {
                bomb = null;
                bombTimer.stop();
            }
        }
        private boolean isPointOnSnake(Point p) {
            for (Point s : snake) {
                if (s.equals(p)) return true;
            }
            return false;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            // Main Game Loop
            if (state == State.RUNNING) {
                move();
                checkCollisions();
                if (gameMode == Mode.AI_MODE) {
                    performAIMove();
                }
            }
            repaint(); // Triggers paintComponent
            if (scorePanel != null) scorePanel.repaint();
        }
        private void move() {
            moveKeyProcessed = false; // Allow new input for next frame
           
            Point head = snake.getFirst();
            Point newHead = new Point(head.x, head.y);
            switch (direction) {
                case 0: newHead.y--; break; // Up
                case 1: newHead.x++; break; // Right
                case 2: newHead.y++; break; // Down
                case 3: newHead.x--; break; // Left
            }
            // Move Snake (Logic depends on Wall Mode)
            if (gameMode == Mode.NO_WALL) {
                // Wrap logic
                if (newHead.x < 0) newHead.x = COLS - 1;
                else if (newHead.x >= COLS) newHead.x = 0;
                if (newHead.y < 0) newHead.y = ROWS - 1;
                else if (newHead.y >= ROWS) newHead.y = 0;
            }
            // Move the body
            snake.addFirst(newHead); // Add new head
           
            // Check Food Eaten Logic (before removing tail)
            boolean grew = false;
            // 1. Check Normal Food
            if (newHead.equals(normalFood)) {
                score += 10;
                normalFoodEatenCount++;
                addFloatingText("+10", newHead.x * TILE_SIZE, newHead.y * TILE_SIZE, Color.WHITE);
                spawnNormalFood();
                grew = true;
                // Trigger Special Food every 5 apples
                if (normalFoodEatenCount % 5 == 0) {
                    spawnShrinkFood();
                }
                // Trigger Bomb every 10 apples
                if (normalFoodEatenCount % 10 == 0) {
                    spawnBomb();
                }
            }
           
            // 2. Check Shrink Food
            else if (shrinkFood != null && newHead.equals(shrinkFood)) {
                score += 30;
                // Shrink Logic
                int shrinkAmount = 3;
                int currentLen = snake.size();
                int minLen = 3;
               
                // Calculate how much we can actually shrink
                int actualShrink = Math.min(shrinkAmount, currentLen - minLen);
               
                if (actualShrink > 0) {
                    for (int i = 0; i < actualShrink; i++) {
                        if (!snake.isEmpty()) snake.removeLast();
                    }
                    addFloatingText("+30 / -" + actualShrink + " Len", newHead.x * TILE_SIZE, newHead.y * TILE_SIZE, Color.CYAN);
                } else {
                    addFloatingText("+30 / Min Len!", newHead.x * TILE_SIZE, newHead.y * TILE_SIZE, Color.CYAN);
                }
                removeShrinkFood();
                grew = true; // Technically we didn't grow, but we don't remove the tail this frame
            }

            // 3. Check Bomb
            else if (bomb != null && newHead.equals(bomb)) {
                score -= 100;
                addFloatingText("-100", newHead.x * TILE_SIZE, newHead.y * TILE_SIZE, Color.RED);
                removeBomb();
                grew = false; // Penalty, move without growing
            }
            // If we didn't eat normal food (or special), remove tail to simulate movement
            if (!grew) {
                snake.removeLast();
            }
        }
        private void checkCollisions() {
            Point head = snake.getFirst();
            // 1. Wall Collision (Classic Mode)
            if (gameMode != Mode.NO_WALL) {
                if (head.x < 0 || head.x >= COLS || head.y < 0 || head.y >= ROWS) {
                    gameOver();
                    return;
                }
            }
            // 2. Self Collision
            // Start checking from index 1 (ignore head itself)
            for (int i = 1; i < snake.size(); i++) {
                if (head.equals(snake.get(i))) {
                    gameOver();
                    return;
                }
            }
        }
        private void gameOver() {
            state = State.GAME_OVER;
            gameTimer.stop();
            removeShrinkFood();
            removeBomb();
            if (score > highScore) {
                highScore = score;
                saveData();
            }
        }
        // ============================
        // AI Logic
        // ============================
        private void performAIMove() {
            Point head = snake.getFirst();
            Point target = (shrinkFood != null) ? shrinkFood : normalFood;
           
            int bestDir = -1;
            double minDist = Double.MAX_VALUE;
            int[] dx = {0, 1, 0, -1}; // Up, Right, Down, Left
            int[] dy = {-1, 0, 1, 0};
            for (int i = 0; i < 4; i++) {
                // Don't reverse
                if ((direction == 0 && i == 2) || (direction == 2 && i == 0) ||
                    (direction == 1 && i == 3) || (direction == 3 && i == 1)) continue;
                int nx = head.x + dx[i];
                int ny = head.y + dy[i];
                Point next = new Point(nx, ny);
                if (gameMode != Mode.NO_WALL) {
                    if (nx < 0 || nx >= COLS || ny < 0 || ny >= ROWS) continue;
                }
               
                if (isPointOnSnake(next)) continue;
                if (bomb != null && next.equals(bomb)) continue; // Avoid bomb
                double dist = next.distance(target);
                if (dist < minDist) {
                    minDist = dist;
                    bestDir = i;
                }
            }
            if (bestDir != -1) {
                direction = bestDir;
            }
        }
        // ============================
        // Rendering / Paint
        // ============================
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Draw Background Grid (Subtle)
            g2d.setColor(new Color(30, 30, 30));
            for (int i = 0; i < COLS; i++) g2d.drawLine(i*TILE_SIZE, 0, i*TILE_SIZE, BOARD_HEIGHT);
            for (int i = 0; i < ROWS; i++) g2d.drawLine(0, i*TILE_SIZE, BOARD_WIDTH, i*TILE_SIZE);
            if (state == State.MENU) drawMenu(g2d);
            else if (state == State.SETTINGS) drawSettings(g2d);
            else {
                // Draw Game Elements
                drawSnake(g2d);
                drawFood(g2d);
                drawFloatingText(g2d);
               
                if (state == State.PAUSED) drawOverlay(g2d, "PAUSED", "Press P to Resume");
                if (state == State.GAME_OVER) drawOverlay(g2d, "GAME OVER", "ENTER to Restart | ESC for Menu");
            }
        }
        private void drawSnake(Graphics2D g) {
            for (int i = 0; i < snake.size(); i++) {
                Point p = snake.get(i);
                if (i == 0) g.setColor(new Color(0, 255, 100)); // Head
                else g.setColor(new Color(0, 180, 50)); // Body
                g.fillRoundRect(p.x * TILE_SIZE + 1, p.y * TILE_SIZE + 1, TILE_SIZE - 2, TILE_SIZE - 2, 8, 8);
            }
        }
        private void drawFood(Graphics2D g) {
            // Normal Food
            g.setColor(Color.RED);
            g.fillOval(normalFood.x * TILE_SIZE + 2, normalFood.y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
            // Special Shrink Food
            if (shrinkFood != null) {
                // Blink effect
                long timeLeft = SHRINK_LIFETIME - (System.currentTimeMillis() - shrinkSpawnTime);
                boolean visible = true;
                if (timeLeft < 1000) visible = (System.currentTimeMillis() % 200 < 100);
                if (visible) {
                    g.setColor(Color.CYAN);
                    g.fillRect(shrinkFood.x * TILE_SIZE + 4, shrinkFood.y * TILE_SIZE + 4, TILE_SIZE - 8, TILE_SIZE - 8);
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(2));
                    g.drawRect(shrinkFood.x * TILE_SIZE + 4, shrinkFood.y * TILE_SIZE + 4, TILE_SIZE - 8, TILE_SIZE - 8);
                }
            }
            // Bomb
            if (bomb != null) {
                // Blink effect
                long timeLeft = BOMB_LIFETIME - (System.currentTimeMillis() - bombSpawnTime);
                boolean visible = true;
                if (timeLeft < 1000) visible = (System.currentTimeMillis() % 200 < 100);
                if (visible) {
                    g.setColor(Color.BLACK);
                    g.fillOval(bomb.x * TILE_SIZE + 2, bomb.y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                    g.setColor(Color.RED);
                    g.setStroke(new BasicStroke(2));
                    g.drawOval(bomb.x * TILE_SIZE + 2, bomb.y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                }
            }
        }
        private void drawFloatingText(Graphics2D g) {
            for (int i = 0; i < floatingTexts.size(); i++) {
                FloatingText ft = floatingTexts.get(i);
                ft.y -= 1;
                ft.life--;
                g.setColor(ft.color);
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.drawString(ft.text, ft.x, ft.y);
                if (ft.life <= 0) {
                    floatingTexts.remove(i);
                    i--;
                }
            }
        }
       
        private void addFloatingText(String text, int x, int y, Color c) {
            floatingTexts.add(new FloatingText(x, y, text, c));
        }
        private void drawOverlay(Graphics2D g, String title, String subtitle) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
           
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(title);
            g.drawString(title, (BOARD_WIDTH - w)/2, BOARD_HEIGHT/2 - 20);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            w = g.getFontMetrics().stringWidth(subtitle);
            g.drawString(subtitle, (BOARD_WIDTH - w)/2, BOARD_HEIGHT/2 + 20);
        }
        private void drawMenu(Graphics2D g) {
            g.setColor(new Color(20, 20, 20));
            g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
           
            g.setColor(Color.GREEN);
            g.setFont(new Font("Segoe UI", Font.BOLD, 50));
            drawCenteredString(g, "SNAKE", BOARD_HEIGHT/4);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
           
            drawButton(g, "START GAME", BOARD_HEIGHT/2 - 30, true);
            drawButton(g, "SETTINGS", BOARD_HEIGHT/2 + 20, false);
            drawButton(g, "EXIT", BOARD_HEIGHT/2 + 70, false);
        }
       
        private void drawSettings(Graphics2D g) {
            g.setColor(new Color(30, 30, 40));
            g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            drawCenteredString(g, "SETTINGS", 50);
           
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("1. Difficulty: " + (gameSpeed==100?"Normal":(gameSpeed<100?"Fast":"Slow")), 150, 150);
            g.drawString("2. Mode: " + gameMode.toString(), 150, 200);
           
            g.setColor(Color.GRAY);
            drawCenteredString(g, "Press 1 to toggle Speed, 2 to toggle Mode", 350);
            drawCenteredString(g, "Press ESC to go back", 400);
        }
       
        private void drawButton(Graphics2D g, String text, int y, boolean highlight) {
            int w = 200;
            int h = 40;
            int x = (BOARD_WIDTH - w) / 2;
            if (highlight) g.setColor(new Color(50, 150, 50));
            else g.setColor(new Color(50, 50, 50));
            g.fillRoundRect(x, y, w, h, 10, 10);
            g.setColor(Color.WHITE);
            g.drawRoundRect(x, y, w, h, 10, 10);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, x + (w - fm.stringWidth(text))/2, y + h/2 + 6);
        }
        private void drawCenteredString(Graphics2D g, String text, int y) {
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, (BOARD_WIDTH - fm.stringWidth(text))/2, y);
        }
        // ============================
        // Input Handling (Fixed)
        // ============================
       
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            // 1. GAME RUNNING
            if (state == State.RUNNING) {
                // Exit to Menu
                if (key == KeyEvent.VK_ESCAPE) {
                    stopGameToMenu();
                    return;
                }
                // AI Mode Inputs
                if (gameMode == Mode.AI_MODE) {
                    if (key == KeyEvent.VK_P) state = State.PAUSED;
                    return;
                }
               
                // Normal Movement Inputs
                if (moveKeyProcessed) return;
                if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && direction != 2) {
                    direction = 0; moveKeyProcessed = true;
                }
                else if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && direction != 3) {
                    direction = 1; moveKeyProcessed = true;
                }
                else if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && direction != 0) {
                    direction = 2; moveKeyProcessed = true;
                }
                else if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && direction != 1) {
                    direction = 3; moveKeyProcessed = true;
                }
               
                if (key == KeyEvent.VK_P) state = State.PAUSED;
            }
           
            // 2. PAUSED
            else if (state == State.PAUSED) {
                if (key == KeyEvent.VK_P || key == KeyEvent.VK_ENTER) state = State.RUNNING;
                if (key == KeyEvent.VK_ESCAPE) stopGameToMenu();
            }
           
            // 3. GAME OVER (Corrected Logic)
            else if (state == State.GAME_OVER) {
                if (key == KeyEvent.VK_ENTER) {
                    startGame(); // Restart immediately
                }
                else if (key == KeyEvent.VK_ESCAPE) {
                    stopGameToMenu(); // Back to Menu
                }
            }
           
            // 4. SETTINGS
            else if (state == State.SETTINGS) {
                if (key == KeyEvent.VK_ESCAPE) state = State.MENU;
                if (key == KeyEvent.VK_1) {
                    if (gameSpeed == 100) gameSpeed = 50;
                    else if (gameSpeed == 50) gameSpeed = 150;
                    else gameSpeed = 100;
                }
                if (key == KeyEvent.VK_2) {
                    if (gameMode == Mode.CLASSIC) gameMode = Mode.NO_WALL;
                    else if (gameMode == Mode.NO_WALL) gameMode = Mode.AI_MODE;
                    else gameMode = Mode.CLASSIC;
                }
                repaint();
            }
        }
        @Override
        public void mousePressed(MouseEvent e) {
            if (state == State.MENU) {
                int mx = e.getX();
                int my = e.getY();
                int btnX = (BOARD_WIDTH - 200) / 2;
               
                // Start
                if (mx > btnX && mx < btnX+200 && my > BOARD_HEIGHT/2 - 30 && my < BOARD_HEIGHT/2 + 10) {
                    startGame();
                }
                // Settings
                if (mx > btnX && mx < btnX+200 && my > BOARD_HEIGHT/2 + 20 && my < BOARD_HEIGHT/2 + 60) {
                    state = State.SETTINGS;
                    repaint();
                }
                // Exit
                if (mx > btnX && mx < btnX+200 && my > BOARD_HEIGHT/2 + 70 && my < BOARD_HEIGHT/2 + 110) {
                    System.exit(0);
                }
            }
        }
        @Override public void keyTyped(KeyEvent e) {}
        @Override public void keyReleased(KeyEvent e) {}
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
        // ============================
        // Persistence
        // ============================
        private void loadData() {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
                highScore = ois.readInt();
            } catch (Exception e) {
                highScore = 0;
            }
        }
        private void saveData() {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
                oos.writeInt(highScore);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class ScorePanel extends JPanel {
        private static final int BOARD_WIDTH = 30 * 25;
        private GamePanel gamePanel;

        public ScorePanel(GamePanel gp) {
            gamePanel = gp;
            setPreferredSize(new Dimension(BOARD_WIDTH, 50));
            setBackground(new Color(20, 20, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (gamePanel.getState() == GamePanel.State.MENU || gamePanel.getState() == GamePanel.State.SETTINGS) {
                return;
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("Score: " + gamePanel.getScore() + "   High: " + gamePanel.getHighScore() + "   Mode: " + gamePanel.getGameMode().toString(), 10, 20);
            // Timer bar for shrink food
            if (gamePanel.getShrinkFood() != null) {
                long elapsed = System.currentTimeMillis() - gamePanel.getShrinkSpawnTime();
                float pct = 1.0f - ((float)elapsed / gamePanel.getShrinkLifetime());
                if(pct < 0) pct = 0;
               
                g.setColor(Color.CYAN);
                g.fillRect(BOARD_WIDTH/2 - 150, 30, (int)(100 * pct), 5);
                g.setColor(Color.WHITE);
                g.drawRect(BOARD_WIDTH/2 - 150, 30, 100, 5);
                g.drawString("SHRINK APPLE!", BOARD_WIDTH/2 - 145, 45);
            }
            // Timer bar for bomb
            if (gamePanel.getBomb() != null) {
                long elapsed = System.currentTimeMillis() - gamePanel.getBombSpawnTime();
                float pct = 1.0f - ((float)elapsed / gamePanel.getBombLifetime());
                if(pct < 0) pct = 0;
               
                g.setColor(Color.RED);
                g.fillRect(BOARD_WIDTH/2 + 50, 30, (int)(100 * pct), 5);
                g.setColor(Color.WHITE);
                g.drawRect(BOARD_WIDTH/2 + 50, 30, 100, 5);
                g.drawString("BOMB!", BOARD_WIDTH/2 + 55, 45);
            }
        }
    }
    static class FloatingText {
        int x, y;
        String text;
        Color color;
        int life = 30;
        public FloatingText(int x, int y, String text, Color color) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
        }
    }
}
