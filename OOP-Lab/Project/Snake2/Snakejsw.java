import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class Snakejsw extends JFrame {
    public Snakejsw() {
        setTitle("Snake Game - Enhanced Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        GamePanel gp = new GamePanel();
        ScorePanel sp = new ScorePanel(gp);
        gp.setScorePanel(sp);
        add(sp, BorderLayout.NORTH);
        add(gp, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Snakejsw().setVisible(true));
    }

    static class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
        static final int TILE = 25, COLS = 30, ROWS = 20, W = TILE * COLS, H = TILE * ROWS;
        static final String FILE = "snake_data.dat";
        enum State { MENU, RUNNING, PAUSED, OVER, SETTINGS }
        enum Mode { CLASSIC, NO_WALL, AI }

        State state = State.MENU;
        Mode mode = Mode.CLASSIC;
        Timer timer, shrinkTimer, bombTimer;
        LinkedList<Point> snake;
        Point food, sFood, bomb;
        int dir = 0, score, high, foodCount, speed = 100;
        boolean moved = false;
        List<FText> texts = new ArrayList<>();
        long sTime, bTime;
        final int SLIFE = 3000, BLIFE = 5000;
        Random rand = new Random();
        ScorePanel scorePanel;

        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        public GamePanel() {
            setPreferredSize(new Dimension(W, H));
            setBackground(new Color(20, 20, 20));
            setFocusable(true);
            addKeyListener(this);
            addMouseListener(this);
            loadData();
            timer = new Timer(speed, this);
            shrinkTimer = new Timer(SLIFE, e -> stopSpecial(0));
            shrinkTimer.setRepeats(false);
            bombTimer = new Timer(BLIFE, e -> stopSpecial(1));
            bombTimer.setRepeats(false);
        }

        void setScorePanel(ScorePanel sp) { this.scorePanel = sp; }

        void start() {
            snake = new LinkedList<>();
            snake.add(new Point(COLS/2, ROWS/2));
            snake.add(new Point(COLS/2, ROWS/2+1));
            snake.add(new Point(COLS/2, ROWS/2+2));
            dir = 0; score = 0; foodCount = 0; texts.clear();
            stopSpecial(-1);
            spawnFood(0);
            state = State.RUNNING;
            timer.setDelay(speed);
            timer.start();
        }

        void stop() {
            timer.stop(); shrinkTimer.stop(); bombTimer.stop();
            state = State.MENU; repaint();
        }

        void stopSpecial(int type) {
            if(type == 0 || type == -1) { sFood = null; shrinkTimer.stop(); }
            if(type == 1 || type == -1) { bomb = null; bombTimer.stop(); }
        }

        Point getFreePoint() {
            Point p;
            do { p = new Point(rand.nextInt(COLS), rand.nextInt(ROWS)); } 
            while (snake.contains(p) || (food!=null && p.equals(food)) || (sFood!=null && p.equals(sFood)) || (bomb!=null && p.equals(bomb)));
            return p;
        }

        void spawnFood(int type) {
            Point p = getFreePoint();
            if(type == 0) food = p;
            else if(type == 1) { 
                sFood = p; sTime = System.currentTimeMillis(); shrinkTimer.restart(); 
                addTxt("Special!", p, Color.YELLOW);
            } else { 
                bomb = p; bTime = System.currentTimeMillis(); bombTimer.restart(); 
                addTxt("Bomb!", p, Color.RED);
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (state == State.RUNNING) {
                if (mode == Mode.AI) runAI();
                move();
                checkCollisions();
            }
            repaint();
            if (scorePanel != null) scorePanel.repaint();
        }

        void move() {
            moved = false;
            Point h = snake.getFirst(), n = new Point(h.x + dx[dir], h.y + dy[dir]);
            if (mode == Mode.NO_WALL) {
                n.x = (n.x + COLS) % COLS;
                n.y = (n.y + ROWS) % ROWS;
            }
            snake.addFirst(n);
            boolean grow = false;

            if (n.equals(food)) {
                score += 10; foodCount++; addTxt("+10", n, Color.WHITE);
                spawnFood(0); grow = true;
                if (foodCount % 5 == 0) spawnFood(1);
                if (foodCount % 10 == 0) spawnFood(2);
            } else if (sFood != null && n.equals(sFood)) {
                score += 30;
                int drop = Math.min(3, snake.size() - 3);
                for (int i=0; i<drop; i++) snake.removeLast();
                addTxt(drop>0 ? "+30/-"+drop : "+30", n, Color.CYAN);
                stopSpecial(0); grow = true;
            } else if (bomb != null && n.equals(bomb)) {
                score -= 100; addTxt("-100", n, Color.RED);
                stopSpecial(1);
            }
            if (!grow) snake.removeLast();
        }

        void checkCollisions() {
            Point h = snake.getFirst();
            if (mode != Mode.NO_WALL && (h.x < 0 || h.x >= COLS || h.y < 0 || h.y >= ROWS)) gameOver();
            for (int i = 1; i < snake.size(); i++) if (h.equals(snake.get(i))) gameOver();
        }

        void gameOver() {
            state = State.OVER; timer.stop(); stopSpecial(-1);
            if (score > high) { high = score; saveData(); }
        }

        void runAI() {
            Point h = snake.getFirst(), t = (sFood != null) ? sFood : food;
            int best = -1; double min = 1e9;
            for (int i = 0; i < 4; i++) {
                if ((dir^2) == i) continue; 
                Point next = new Point(h.x + dx[i], h.y + dy[i]);
                if (mode != Mode.NO_WALL && (next.x < 0 || next.x >= COLS || next.y < 0 || next.y >= ROWS)) continue;
                if (snake.contains(next) || (bomb != null && next.equals(bomb))) continue;
                double d = next.distance(t);
                if (d < min) { min = d; best = i; }
            }
            if (best != -1) dir = best;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(30, 30, 30));
            for(int i=0; i<COLS; i++) g2.drawLine(i*TILE,0,i*TILE,H);
            for(int i=0; i<ROWS; i++) g2.drawLine(0,i*TILE,W,i*TILE);

            if(state == State.MENU) drawMenu(g2);
            else if(state == State.SETTINGS) drawSets(g2);
            else {
                for(int i=0; i<snake.size(); i++) {
                    g2.setColor(i==0 ? new Color(0,255,100) : new Color(0,180,50));
                    Point p = snake.get(i);
                    g2.fillRoundRect(p.x*TILE+1, p.y*TILE+1, TILE-2, TILE-2, 8, 8);
                }
                g2.setColor(Color.RED);
                g2.fillOval(food.x*TILE+2, food.y*TILE+2, TILE-4, TILE-4);
                drawItem(g2, sFood, Color.CYAN, sTime, SLIFE, true);
                drawItem(g2, bomb, Color.BLACK, bTime, BLIFE, false);
                for (int i = 0; i < texts.size(); i++) {
                    FText t = texts.get(i);
                    t.y--; t.l--;
                    g2.setColor(t.c); g2.setFont(new Font("Arial",1,12));
                    g2.drawString(t.t, t.x, t.y);
                    if (t.l <= 0) texts.remove(i--);
                }
                if (state == State.PAUSED) drawOver(g2, "PAUSED", "P to Resume");
                if (state == State.OVER) drawOver(g2, "GAME OVER", "Enter to Restart");
            }
        }

        void drawItem(Graphics2D g, Point p, Color c, long t, int life, boolean rect) {
            if (p == null) return;
            long rem = life - (System.currentTimeMillis() - t);
            if (rem > 1000 || (System.currentTimeMillis() % 200 < 100)) {
                g.setColor(c);
                if(rect) g.fillRect(p.x*TILE+4, p.y*TILE+4, TILE-8, TILE-8);
                else g.fillOval(p.x*TILE+2, p.y*TILE+2, TILE-4, TILE-4);
                g.setColor(rect ? Color.WHITE : Color.RED); g.setStroke(new BasicStroke(2));
                if(rect) g.drawRect(p.x*TILE+4, p.y*TILE+4, TILE-8, TILE-8);
                else g.drawOval(p.x*TILE+2, p.y*TILE+2, TILE-4, TILE-4);
            }
        }

        void addTxt(String t, Point p, Color c) { texts.add(new FText(p.x*TILE, p.y*TILE, t, c)); }
        void drawOver(Graphics g, String m, String s) {
            g.setColor(new Color(0,0,0,150)); g.fillRect(0,0,W,H);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial",1,40));
            drawC(g, m, H/2-20); g.setFont(new Font("Arial",0,18)); drawC(g, s, H/2+20);
        }
        void drawMenu(Graphics g) {
            g.setColor(Color.GREEN); g.setFont(new Font("Segoe UI",1,50)); drawC(g,"SNAKE", H/4);
            g.setFont(new Font("Arial",0,20));
            drawBtn(g,"START GAME", H/2-30, true);
            drawBtn(g,"SETTINGS", H/2+20, false);
            drawBtn(g,"EXIT", H/2+70, false);
        }
        void drawSets(Graphics g) {
            g.setColor(new Color(30,30,40)); g.fillRect(0,0,W,H);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial",1,30)); drawC(g,"SETTINGS",50);
            g.setFont(new Font("Arial",0,18));
            g.drawString("1. Difficulty: " + (speed==100?"Normal":(speed<100?"Fast":"Slow")), 150,150);
            g.drawString("2. Mode: " + mode, 150,200);
            g.setColor(Color.GRAY); drawC(g,"Press 1 or 2. ESC to return.", 400);
        }
        void drawBtn(Graphics g, String t, int y, boolean h) {
            g.setColor(h ? new Color(50,150,50) : new Color(50,50,50));
            int x = (W-200)/2; g.fillRoundRect(x,y,200,40,10,10);
            g.setColor(Color.WHITE); g.drawRoundRect(x,y,200,40,10,10);
            drawC(g, t, y+26);
        }
        void drawC(Graphics g, String t, int y) { g.drawString(t, (W-g.getFontMetrics().stringWidth(t))/2, y); }

        public void keyPressed(KeyEvent e) {
            int k = e.getKeyCode();
            if (state == State.RUNNING) {
                if (k == KeyEvent.VK_ESCAPE) stop();
                if (mode == Mode.AI && k == KeyEvent.VK_P) state = State.PAUSED;
                else if (mode != Mode.AI) {
                    if (moved) return;
                    if ((k == KeyEvent.VK_UP || k == KeyEvent.VK_W) && dir != 2) dir = 0;
                    else if ((k == KeyEvent.VK_RIGHT || k == KeyEvent.VK_D) && dir != 3) dir = 1;
                    else if ((k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) && dir != 0) dir = 2;
                    else if ((k == KeyEvent.VK_LEFT || k == KeyEvent.VK_A) && dir != 1) dir = 3;
                    moved = true;
                    if (k == KeyEvent.VK_P) state = State.PAUSED;
                }
            } else if (state == State.PAUSED) {
                if (k == KeyEvent.VK_P || k == KeyEvent.VK_ENTER) state = State.RUNNING;
                if (k == KeyEvent.VK_ESCAPE) stop();
            } else if (state == State.OVER) {
                if (k == KeyEvent.VK_ENTER) start();
                if (k == KeyEvent.VK_ESCAPE) stop();
            } else if (state == State.SETTINGS) {
                if (k == KeyEvent.VK_ESCAPE) state = State.MENU;
                if (k == KeyEvent.VK_1) speed = (speed==100?50:(speed==50?150:100));
                if (k == KeyEvent.VK_2) mode = Mode.values()[(mode.ordinal()+1)%3];
                repaint();
            }
        }
        public void mousePressed(MouseEvent e) {
            if (state != State.MENU) return;
            int mx = e.getX(), my = e.getY(), bx = (W-200)/2;
            if (mx>bx && mx<bx+200) {
                if (my>H/2-30 && my<H/2+10) start();
                else if (my>H/2+20 && my<H/2+60) { state=State.SETTINGS; repaint(); }
                else if (my>H/2+70 && my<H/2+110) System.exit(0);
            }
        }
        public void keyTyped(KeyEvent e) {} public void keyReleased(KeyEvent e) {}
        public void mouseClicked(MouseEvent e) {} public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {} public void mouseExited(MouseEvent e) {}

        void loadData() { try(ObjectInputStream i=new ObjectInputStream(new FileInputStream(FILE)))
            { high=i.readInt(); }catch(Exception e){ high=0; } }
        void saveData() { try(ObjectOutputStream o=new ObjectOutputStream(new FileOutputStream(FILE)))
            { o.writeInt(high); }catch(Exception e){} }
    }

    static class ScorePanel extends JPanel {
        GamePanel gp;
        ScorePanel(GamePanel g) { gp = g; setPreferredSize(new Dimension(30*25, 50)); setBackground(new Color(20,20,20)); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (gp.state == GamePanel.State.MENU || gp.state == GamePanel.State.SETTINGS) return;
            g.setColor(Color.WHITE); g.setFont(new Font("Arial",1,14));
            g.drawString("Score: " + gp.score + "  Hi: " + gp.high + "  Mode: " + gp.mode, 10, 20);
            drawBar(g, gp.sFood, gp.sTime, gp.SLIFE, "SHRINK!", Color.CYAN, 1);
            drawBar(g, gp.bomb, gp.bTime, gp.BLIFE, "BOMB!", Color.RED, -1);
        }
        void drawBar(Graphics g, Point p, long t, int l, String s, Color c, int pos) {
            if (p == null) return;
            float pct = Math.max(0, 1f - (System.currentTimeMillis()-t)/(float)l);
            int w = (30*25), x = w/2 + (pos==-1 ? 50 : -150);
            g.setColor(c); g.fillRect(x, 30, (int)(100*pct), 5);
            g.setColor(Color.WHITE); g.drawRect(x, 30, 100, 5);
            g.drawString(s, x+5, 45);
        }
    }
    static class FText {
        int x, y, l=30; String t; Color c;
        FText(int x, int y, String t, Color c) { this.x=x; this.y=y; this.t=t; this.c=c; }
    }
}
