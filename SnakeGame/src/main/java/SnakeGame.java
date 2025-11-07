import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * A simple Snake game using Java Swing for the GUI.
 *
 * How it works:
 * - A JFrame window is created.
 * - A JPanel is used as the game board, and we draw rectangles on it.
 * - A Swing Timer runs the game loop (replaces Thread.sleep).
 * - A KeyAdapter listens for arrow key presses (solves the 'Enter' key problem).
 */
public class SnakeGame extends JPanel implements ActionListener {

    // --- Game Configuration ---
    private static final int BOARD_WIDTH = 300;
    private static final int BOARD_HEIGHT = 300;
    private static final int DOT_SIZE = 10; // Size of snake segments and food
    private static final int ALL_DOTS = (BOARD_WIDTH * BOARD_HEIGHT) / (DOT_SIZE * DOT_SIZE);
    private static final int GAME_SPEED_MS = 140; // Timer delay

    // --- Game State ---
    // Use x[] and y[] arrays for snake coordinates
    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[ALL_DOTS];

    private int snakeLength;
    private int foodX;
    private int foodY;

    // 'l' (left), 'r' (right), 'u' (up), 'd' (down)
    private char direction = 'r';
    private boolean inGame = true;
    private boolean isGameStarted = false; // For the start screen
    private final Random random = new Random();
    private Timer timer; // The main game loop timer

    /**
     * Constructor: Sets up the game panel and starts the game.
     */
    public SnakeGame() {
        initBoard();
        initGame();

        // Create the timer here, but don't start it.
        // It will be started by the KeyAdapter when the user presses Enter.
        timer = new Timer(GAME_SPEED_MS, this);
    }

    /**
     * Initializes the JPanel properties.
     */
    private void initBoard() {
        // Listens for key presses
        addKeyListener(new TAdapter());

        setBackground(Color.black);
        setFocusable(true); // Important for key listener to work
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));

        // Add an 'Escape' key binding to close the window
        setupEscapeKey();
    }

    /**
     * Sets up the 'Escape' key to close the game.
     */
    private void setupEscapeKey() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "quitGame");

        getActionMap().put("quitGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window window = SwingUtilities.getWindowAncestor(SnakeGame.this);
                if (window != null) {
                    window.dispose();
                }
            }
        });
    }

    /**
     * Initializes/resets the game state (snake, food, timer).
     */
    private void initGame() {
        snakeLength = 3;
        direction = 'r'; // Start moving right
        inGame = true;

        // Create initial snake
        for (int i = 0; i < snakeLength; i++) {
            x[i] = (BOARD_WIDTH / 2) - (i * DOT_SIZE);
            y[i] = BOARD_HEIGHT / 2;
        }

        placeFood();
    }

    /**
     * Places a new food item at a random empty spot.
     */
    private void placeFood() {
        foodX = random.nextInt(BOARD_WIDTH / DOT_SIZE) * DOT_SIZE;
        foodY = random.nextInt(BOARD_HEIGHT / DOT_SIZE) * DOT_SIZE;

        // Check if it spawned on the snake
        for (int i = 0; i < snakeLength; i++) {
            if (x[i] == foodX && y[i] == foodY) {
                placeFood(); // Try again
                return;
            }
        }
    }

    /**
     * This method is called by the Timer. It's the main game loop.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // Only update game logic if we are in game AND the game has started
        if (inGame && isGameStarted) {
            checkFood();
            checkCollisions();
            move();
        }
        // Redraw the panel
        repaint();
    }

    /**
     * Handles the drawing of all game elements.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Clears the panel
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        if (!isGameStarted) {
            drawStartScreen(g);
        } else if (inGame) {
            // Draw the food
            g.setColor(Color.red);
            g.fillRect(foodX, foodY, DOT_SIZE, DOT_SIZE);

            // Draw the snake
            for (int i = 0; i < snakeLength; i++) {
                if (i == 0) {
                    // Head
                    g.setColor(new Color(0, 180, 0)); // Darker green
                } else {
                    // Body
                    g.setColor(Color.green);
                }
                g.fillRect(x[i], y[i], DOT_SIZE, DOT_SIZE);
            }
        } else {
            // Game over
            drawGameOver(g);
        }
    }

    /**
     * Draws the "Press Enter" start screen.
     */
    private void drawStartScreen(Graphics g) {
        String msg = "Press Enter to Play";
        Font font = new Font("Helvetica", Font.BOLD, 20);
        FontMetrics metrics = getFontMetrics(font);

        g.setColor(Color.white);
        g.setFont(font);

        g.drawString(msg, (BOARD_WIDTH - metrics.stringWidth(msg)) / 2, BOARD_HEIGHT / 2);
    }

    /**
     * Draws the "Game Over" message.
     */
    private void drawGameOver(Graphics g) {
        String msg = "Game Over!";
        String scoreMsg = "Score: " + (snakeLength - 3);
        String restartMsg = "Press Enter to Restart"; // Added
        String quitMsg = "Press Esc to Quit"; // Added

        Font font = new Font("Helvetica", Font.BOLD, 24);
        FontMetrics metrics = getFontMetrics(font);

        g.setColor(Color.white);
        g.setFont(font);

        // Draw "Game Over!"
        g.drawString(msg, (BOARD_WIDTH - metrics.stringWidth(msg)) / 2, BOARD_HEIGHT / 2 - 40);

        // Draw "Score: X"
        font = new Font("Helvetica", Font.BOLD, 16);
        metrics = getFontMetrics(font);
        g.setFont(font);
        g.drawString(scoreMsg, (BOARD_WIDTH - metrics.stringWidth(scoreMsg)) / 2, BOARD_HEIGHT / 2 - 10);

        // Draw Restart Message
        g.drawString(restartMsg, (BOARD_WIDTH - metrics.stringWidth(restartMsg)) / 2, BOARD_HEIGHT / 2 + 20);

        // Draw Quit Message
        g.drawString(quitMsg, (BOARD_WIDTH - metrics.stringWidth(quitMsg)) / 2, BOARD_HEIGHT / 2 + 40);
    }

    /**
     * Moves the snake one step.
     */
    private void move() {
        // Move all body parts one position up
        for (int i = snakeLength; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // Move the head in the current direction
        switch (direction) {
            case 'u': y[0] -= DOT_SIZE; break;
            case 'd': y[0] += DOT_SIZE; break;
            case 'l': x[0] -= DOT_SIZE; break;
            case 'r': x[0] += DOT_SIZE; break;
        }
    }

    /**
     * Checks if the snake has eaten the food.
     */
    private void checkFood() {
        if (x[0] == foodX && y[0] == foodY) {
            snakeLength++;
            placeFood();
        }
    }

    /**
     * Checks for collisions with walls or the snake's own body.
     */
    private void checkCollisions() {
        // Check self-collision
        for (int i = snakeLength; i > 0; i--) {
            if (i > 3 && x[0] == x[i] && y[0] == y[i]) {
                inGame = false;
            }
        }

        // Check wall collision
        if (y[0] < 0 || y[0] >= BOARD_HEIGHT || x[0] < 0 || x[0] >= BOARD_WIDTH) {
            inGame = false;
        }

        if (!inGame) {
            timer.stop(); // Stop the timer on Game Over
        }
    }

    /**
     * Inner class to handle keyboard input.
     */
    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (!isGameStarted) {
                // On the start screen
                if (key == KeyEvent.VK_ENTER) {
                    isGameStarted = true;
                    timer.start(); // Start the timer
                }
            } else if (!inGame) {
                // On the Game Over screen
                if (key == KeyEvent.VK_ENTER) {
                    initGame(); // Restart the game
                    timer.start(); // Start the timer again
                }
            } else {
                // In-game, handle directions
                // Prevent 180-degree turns
                if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && direction != 'r') {
                    direction = 'l';
                } else if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && direction != 'l') {
                    direction = 'r';
                } else if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && direction != 'd') {
                    direction = 'u';
                } else if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && direction != 'u') {
                    direction = 'd';
                }
            }
        }
    }

    /**
     * Main method to run the game.
     */
    public static void main(String[] args) {
        // Create the window
        JFrame frame = new JFrame("SnakeGame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // Add the game panel to the window
        frame.add(new SnakeGame());

        // Pack the window to fit the panel's preferred size
        frame.pack();

        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
    }
}