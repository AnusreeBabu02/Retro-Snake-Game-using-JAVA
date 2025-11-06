import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

/**
 * Main Snake Game Panel (Alby & Anusree's Snake Game)
 * Handles the game logic, rendering, input, and UI transitions.
 */
public class AASnakeGame extends JPanel implements ActionListener, KeyListener {

    // ==== GAME DIMENSIONS ====
    private final int cellSize = 25;
    private final int nCols = 40;
    private final int nRows = 25;
    private final int statsHeight = 50;
    private final int widthPx = nCols * cellSize;
    private final int heightPx = nRows * cellSize;

    // ==== GAME SPEED SETTINGS ====
    private int baseDelay = 180;
    private final int minDelay = 40;
    private int currentDelay = baseDelay;
    private final int levelCap = 30;

    // ==== TIMERS ====
    private Timer gameTimer;
    private Timer countdownTimer;

    // ==== GAME STATE FLAGS ====
    // Uses the custom linked list logic
    private CustomSnakeLogic snakeGame; 
    private boolean isRunning = false;
    private boolean inCountdown = false;
    private int countdown = 3;
    private boolean showGo = false;

    // ==== MENU CALLBACK ====
    private Runnable showMenuCallback;

    // ==== BUTTONS ====
    private JButton restartBtn;
    private JButton menuBtn;

    // ==== SCORE ====
    private int highScore = 0;

    public AASnakeGame() {
        setPreferredSize(new Dimension(widthPx, heightPx + statsHeight));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        gameTimer = new Timer(currentDelay, this);
        gameTimer.setRepeats(true);
    }

    /** Starts a new game with the selected difficulty */
    public void startGame(String difficulty) {
        try {
            switch (difficulty) {
                case "Easy" -> baseDelay = 240;
                case "Normal" -> baseDelay = 180;
                case "Hard" -> baseDelay = 120;
            }

            currentDelay = baseDelay;
            gameTimer.setDelay(currentDelay);

            // Using the new custom logic class
            snakeGame = new CustomSnakeLogic(nCols, nRows, cellSize);
            snakeGame.createSnake(3);
            snakeGame.spawnFood();

            inCountdown = true;
            isRunning = false;
            countdown = 3;
            showGo = false;

            requestFocusInWindow();

            if (countdownTimer != null && countdownTimer.isRunning()) countdownTimer.stop();
            countdownTimer = new Timer(1000, e -> {
                countdown--;
                if (countdown <= 0) showGo = true;
                repaint();
                if (countdown < 0) {
                    ((Timer) e.getSource()).stop();
                    inCountdown = false;
                    showGo = false;
                    isRunning = true;
                    gameTimer.start();
                    requestFocusInWindow();
                }
            });
            countdownTimer.start();

            revalidate();
            repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error starting game: " + ex.getMessage());
        }
    }

    public void setShowMenuCallback(Runnable callback) {
        this.showMenuCallback = callback;
    }

    /** Draw everything (snake, food, UI, etc.) */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        try {
            // Draw Stats Bar
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, widthPx, statsHeight);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            int score = (snakeGame != null) ? snakeGame.score : 0;
            g.drawString("Score: " + score, 8, 30);
            g.drawString("High Score: " + highScore, widthPx - 180, 30);

            // Draw Game Border
            g.setColor(Color.WHITE);
            g.drawRect(0, statsHeight, widthPx - 1, heightPx - 1);

            // Handle Countdown Display
            if (inCountdown) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(255, 255, 0, 180));
                g2.setFont(new Font("Consolas", Font.BOLD, 80));
                if (showGo) drawCentered(g2, "GO!", statsHeight + heightPx / 2);
                else drawCentered(g2, String.valueOf(Math.max(0, countdown)), statsHeight + heightPx / 2);
                return;
            }

            // Handle Game Over Display
            if (!isRunning && snakeGame != null) {
                g.setColor(Color.BLACK);
                g.fillRect(0, statsHeight, widthPx, heightPx);
                g.setColor(Color.RED.darker());
                g.setFont(new Font("Consolas", Font.BOLD, 46));
                drawCentered(g, "GAME OVER", statsHeight + heightPx / 2 - 40);
                return;
            }

            // Draw Game Content
            if (snakeGame != null) snakeGame.draw(g, statsHeight);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Helper to draw text centered horizontally */
    private void drawCentered(Graphics g, String s, int y) {
        FontMetrics fm = g.getFontMetrics();
        int x = (widthPx - fm.stringWidth(s)) / 2;
        g.drawString(s, x, y);
    }

    /** Main game update loop */
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (!isRunning || snakeGame == null) return;

            snakeGame.move();

            if (snakeGame.checkCollision()) {
                if (snakeGame.score > highScore) highScore = snakeGame.score;
                isRunning = false;
                gameTimer.stop();
                SwingUtilities.invokeLater(this::showGameOverButtons);
                repaint();
                return;
            }

            if (snakeGame.snakeHead().equals(snakeGame.food)) {
                snakeGame.grow();
                snakeGame.score += 5;

                // Speed up logic
                int level = Math.min(levelCap, snakeGame.score / 5);
                int newDelay = baseDelay - level * 5;
                if (newDelay < minDelay) newDelay = minDelay;
                if (newDelay != currentDelay) {
                    currentDelay = newDelay;
                    gameTimer.setDelay(currentDelay);
                }
                snakeGame.spawnFood();
            }

            repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Show restart + menu buttons after game over */
    private void showGameOverButtons() {
        try {
            // Remove previous buttons if they exist
            if (restartBtn != null) remove(restartBtn);
            if (menuBtn != null) remove(menuBtn);

            setLayout(null); // Use absolute positioning for buttons
            int btnW = 200, btnH = 48;
            int x = (widthPx - btnW) / 2;
            int y = statsHeight + heightPx / 2 - 10;

            // Restart Button
            restartBtn = new JButton("Restart");
            restartBtn.setBounds(x, y, btnW, btnH);
            restartBtn.setFont(new Font("Consolas", Font.BOLD, 18));
            restartBtn.addActionListener(evt -> {
                remove(restartBtn);
                remove(menuBtn);
                revalidate();
                repaint();
                restartGame();
            });
            add(restartBtn);

            // Main Menu Button
            menuBtn = new JButton("Main Menu");
            menuBtn.setBounds(x, y + btnH + 12, btnW, btnH);
            menuBtn.setFont(new Font("Consolas", Font.BOLD, 18));
            menuBtn.addActionListener(evt -> {
                remove(restartBtn);
                remove(menuBtn);
                revalidate();
                repaint();
                gameTimer.stop();
                inCountdown = false;
                isRunning = false;
                if (showMenuCallback != null) showMenuCallback.run();
            });
            add(menuBtn);

            revalidate();
            repaint();
            requestFocusInWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Restart logic */
    private void restartGame() {
        try {
            currentDelay = baseDelay;
            gameTimer.setDelay(currentDelay);

            // Resetting game state
            snakeGame.createSnake(3);
            snakeGame.spawnFood();
            snakeGame.score = 0;

            inCountdown = true;
            isRunning = false;
            countdown = 3;
            showGo = false;

            // Start countdown timer
            if (countdownTimer != null && countdownTimer.isRunning()) countdownTimer.stop();
            countdownTimer = new Timer(1000, ev -> {
                countdown--;
                if (countdown <= 0) showGo = true;
                repaint();
                if (countdown < 0) {
                    ((Timer) ev.getSource()).stop();
                    inCountdown = false;
                    isRunning = true;
                    gameTimer.start();
                    requestFocusInWindow();
                }
            });
            countdownTimer.start();
            requestFocusInWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        try {
            if (snakeGame == null || !isRunning) return;
            int code = e.getKeyCode();
            // Directional input handling
            switch (code) {
                case KeyEvent.VK_UP, KeyEvent.VK_W -> snakeGame.changeDirection(0, -1);
                case KeyEvent.VK_DOWN, KeyEvent.VK_S -> snakeGame.changeDirection(0, 1);
                case KeyEvent.VK_LEFT, KeyEvent.VK_A -> snakeGame.changeDirection(-1, 0);
                case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> snakeGame.changeDirection(1, 0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    /** Main launcher */
    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    JFrame frame = new JFrame("Alby & Anusree's Snake Game");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    CardLayout cards = new CardLayout();
                    Container cp = frame.getContentPane();
                    cp.setLayout(cards);

                    AASnakeGame gamePanel = new AASnakeGame();

                    // Main Menu Panel setup
                    JPanel menu = new JPanel(null);
                    menu.setBackground(Color.BLACK);
                    menu.setPreferredSize(new Dimension(gamePanel.widthPx, gamePanel.heightPx + gamePanel.statsHeight));

                    // Title
                    JLabel title = new JLabel("SNAKE GAME");
                    title.setForeground(Color.WHITE);
                    title.setFont(new Font("Consolas", Font.BOLD, 48));
                    title.setHorizontalAlignment(SwingConstants.CENTER);
                    title.setBounds(0, 30, gamePanel.widthPx, 60);
                    menu.add(title);

                    // Menu Buttons
                    int btnW = 220, btnH = 50;
                    int cx = (gamePanel.widthPx - btnW) / 2;
                    int sy = 140;

                    JButton start = new JButton("Start Game");
                    start.setBounds(cx, sy, btnW, btnH);
                    start.setFont(new Font("Consolas", Font.BOLD, 20));
                    menu.add(start);

                    JButton credits = new JButton("Credits");
                    credits.setBounds(cx, sy + 70, btnW, btnH);
                    credits.setFont(new Font("Consolas", Font.BOLD, 20));
                    menu.add(credits);

                    JButton quit = new JButton("Quit");
                    quit.setBounds(cx, sy + 140, btnW, btnH);
                    quit.setFont(new Font("Consolas", Font.BOLD, 20));
                    menu.add(quit);

                    // Instructions
                    JLabel instr = new JLabel("<html><div style='text-align:center'>Use Arrow Keys or WASD to move.<br>" +
                            "Avoid hitting yourself or the wall.<br>Press Restart or Main Menu after Game Over.</div></html>");
                    instr.setForeground(Color.WHITE);
                    instr.setFont(new Font("Consolas", Font.PLAIN, 14));
                    instr.setHorizontalAlignment(SwingConstants.CENTER);
                    instr.setBounds(0, sy + 210, gamePanel.widthPx, 80);
                    menu.add(instr);

                    // Button Listeners
                    start.addActionListener(evt -> {
                        String[] options = {"Easy", "Normal", "Hard", "Cancel"};
                        int choice = JOptionPane.showOptionDialog(frame, "Select difficulty:", "Difficulty",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
                        if (choice >= 0 && choice <= 2) {
                            cards.show(cp, "GAME");
                            gamePanel.setShowMenuCallback(() -> cards.show(cp, "MENU"));
                            gamePanel.startGame(options[choice]);
                            SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
                        }
                    });

                    credits.addActionListener(evt -> JOptionPane.showMessageDialog(frame,
                            "Credits:\nALBY MATHEW BIJU\nANUSREE BABU", "Credits", JOptionPane.INFORMATION_MESSAGE));

                    quit.addActionListener(evt -> System.exit(0));

                    // Add panels to card layout
                    cp.add(menu, "MENU");
                    cp.add(gamePanel, "GAME");

                    frame.pack();
                    frame.setResizable(false);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);

                    cards.show(cp, "MENU");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

/**
 * Core Snake Logic refactored to use a custom Singly Linked List (SLL) 
 * defined internally using the SegmentNode class.
 */
class CustomSnakeLogic {
    final int cols, rows, cell;
    int score = 0;
    Point food;
    
    // Custom Linked List Structure (Singly Linked List)
    private SegmentNode head;    // Pointer to the first segment (head of the snake)
    private SegmentNode tail;    // Pointer to the last segment (tail of the snake)
    private int snakeLength;     // Total number of segments
    
    // Flag to control growth: prevents tail removal on the next move cycle
    private boolean shouldGrowNextMove = false;
    
    Point dir;
    final Random rand = new Random();

    /**
     * Node structure for the custom Singly Linked List.
     * Each node represents one segment of the snake.
     */
    static class SegmentNode {
        Point position;
        SegmentNode next;

        SegmentNode(Point p) {
            this.position = p;
            this.next = null;
        }
    }

    CustomSnakeLogic(int cols, int rows, int cell) {
        this.cols = cols;
        this.rows = rows;
        this.cell = cell;
    }

    /** Initializes or resets the snake's body using manual Linked List creation. */
    void createSnake(int len) {
        head = null;
        tail = null;
        snakeLength = 0;
        
        int startX = cols / 2;
        int startY = rows / 2;
        
        // Manual Linked List construction
        for (int i = 0; i < len; i++) {
            Point p = new Point(startX - i, startY);
            SegmentNode newNode = new SegmentNode(p);
            
            if (head == null) {
                head = newNode;
                tail = newNode;
            } else {
                tail.next = newNode;
                tail = newNode;
            }
            snakeLength++;
        }
        
        dir = new Point(1, 0); // Start moving right
        score = 0;
        shouldGrowNextMove = false;
    }

    /** Generates food at a location not occupied by the snake. */
    void spawnFood() {
        while (true) {
            Point f = new Point(rand.nextInt(cols), rand.nextInt(rows));
            boolean onSnake = false;
            
            // Manual traversal check for collision with snake body
            SegmentNode current = head;
            while (current != null) {
                if (current.position.equals(f)) { 
                    onSnake = true; 
                    break; 
                }
                current = current.next;
            }
            
            if (!onSnake) { 
                food = f; 
                return; 
            }
        }
    }

    /** * Moves the snake by prepending a new head and deleting the tail.
     * This is the linked list equivalent of shifting the body.
     */
    void move() {
        try {
            if (head == null) return;

            // 1. Calculate new head position
            Point currentHeadPos = head.position;
            Point newHeadPos = new Point(currentHeadPos.x + dir.x, currentHeadPos.y + dir.y);
            
            // 2. Prepend a new head node (O(1) operation)
            SegmentNode newHead = new SegmentNode(newHeadPos);
            newHead.next = head;
            head = newHead;
            snakeLength++;
            
            // 3. Delete the tail node (O(N) operation for SLL)
            if (!shouldGrowNextMove) {
                // Traverse to the node BEFORE the current tail
                SegmentNode current = head;
                // Stop when current.next is the tail (current.next.next == null)
                while (current.next != null && current.next.next != null) {
                    current = current.next;
                }
                
                // If snake has only 1 segment, head.next will be null, and we do nothing here.
                if (current.next != null) {
                    current.next = null; // Detach the old tail
                    tail = current;       // Update the new tail
                    snakeLength--;
                }
            } else {
                // If we are growing, we skip tail deletion and reset the flag
                shouldGrowNextMove = false; 
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Signals to the next move cycle that the snake should grow. */
    void grow() {
        shouldGrowNextMove = true;
    }

    /** Returns the position of the head segment. */
    Point snakeHead() { 
        // Note: The head is guaranteed to be non-null when game is running.
        return head.position; 
    }

    /** Checks for collision with walls or self. */
    boolean checkCollision() {
        if (head == null) return true;
        
        Point headPos = head.position;
        
        // Wall collision
        if (headPos.x < 0 || headPos.y < 0 || headPos.x >= cols || headPos.y >= rows) return true;
        
        // Self collision (Start check from the second segment: head.next)
        SegmentNode current = head.next; 
        while (current != null) {
            if (current.position.equals(headPos)) return true;
            current = current.next;
        }
        return false;
    }

    /** Changes the direction, preventing immediate 180-degree turns. */
    void changeDirection(int dx, int dy) {
        if (head == null) return;
        
        // We only check for a 180 turn if the snake has more than one segment.
        if (head.next != null) { 
            Point nextPos = head.next.position;
            Point headPos = head.position;
            
            // If the potential new position is the same as the second segment's current position, 
            // it means we are trying to reverse (180 degree turn).
            if (nextPos.x == headPos.x + dx && nextPos.y == headPos.y + dy) return;
        }
        dir = new Point(dx, dy);
    }

    /** Draws the snake and food via manual list traversal. */
    void draw(Graphics g, int offsetY) {
        // Draw snake
        SegmentNode current = head;
        while (current != null) {
            Point p = current.position;
            g.setColor(new Color(0, 150, 0)); // Snake outline
            g.fillRect(p.x * cell, offsetY + p.y * cell, cell, cell);
            g.setColor(new Color(0, 200, 0)); // Main body
            g.fillRect(p.x * cell + 2, offsetY + p.y * cell + 2, cell - 4, cell - 4);
            
            current = current.next;
        }

        // Draw food
        g.setColor(Color.RED.darker());
        g.fillOval(food.x * cell + 3, offsetY + food.y * cell + 3, cell - 6, cell - 6);
        g.setColor(Color.PINK);
        g.fillOval(food.x * cell + 4, offsetY + food.y * cell + 4, 3, 3);
    }
}