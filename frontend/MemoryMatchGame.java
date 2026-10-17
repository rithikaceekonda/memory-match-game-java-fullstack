import javax.swing.*;
import javax.swing.border.*;
import javax.swing.JScrollPane;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * Memory Match Game - A polished Swing-based card matching game.
 * OOP Design: MemoryMatchGame (JFrame) → GameBoard (JPanel) → CardButton (JButton)
 */
public class MemoryMatchGame extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(13,  17,  30);
    private static final Color BG_PANEL      = new Color(20,  26,  46);
    private static final Color CARD_BACK     = new Color(35,  45,  80);
    private static final Color CARD_FRONT    = new Color(25,  35,  65);
    private static final Color ACCENT_CYAN   = new Color(0,  220, 200);
    private static final Color ACCENT_PURPLE = new Color(140, 80, 255);
    private static final Color ACCENT_GOLD   = new Color(255, 200,  60);
    private static final Color TEXT_LIGHT    = new Color(220, 230, 255);
    private static final Color TEXT_DIM      = new Color(100, 120, 170);
    private static final Color MATCHED_BG    = new Color(20,  70,  60);
    private static final Color MATCHED_BORDER= new Color(0,  200, 160);

    // ── Symbol sets ──────────────────────────────────────────────────────────
    private static final String[] SYMBOLS_4x4 = {
        "★","♦","♠","♥","☀","♛","♞","⚡"
    };
    private static final String[] SYMBOLS_6x6 = {
        "★","♦","♠","♥","☀","♛","♞","⚡","☂","✿","⚽","♪","⚓","✈","♟","⚜","♋","✦"
    };

    // ── State ─────────────────────────────────────────────────────────────────
    private int gridSize = 4;
    private GameBoard gameBoard;
    private String playerName;
    private int playerId;
    private final MemoryMatchApiClient apiClient =
        new MemoryMatchApiClient(); 
    private JLabel movesLabel, timerLabel, titleLabel;
    private JButton newGameBtn, sizeBtn;
    private int moves = 0;
    private javax.swing.Timer countTimer;
    private int elapsedSeconds = 0;
    private int score = 0;

    // ─────────────────────────────────────────────────────────────────────────
    
    public MemoryMatchGame() {
        super("Memory Match");

        String enteredName = JOptionPane.showInputDialog(
                null,
                "Enter your player name:",
                "Memory Match - Player",
                JOptionPane.QUESTION_MESSAGE
        );

        if (enteredName == null || enteredName.isBlank()) {
            dispose();
            return;
        }

        playerName = enteredName.trim();

        
        try {
            String response = apiClient.registerPlayer(playerName);

            // Extract the player ID from the JSON response.
            java.util.regex.Matcher matcher =
                    java.util.regex.Pattern
                            .compile("\"id\"\\s*:\\s*(\\d+)")
                            .matcher(response);

            if (!matcher.find()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Could not read the player ID from the backend.",
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE
                );
                dispose();
                return;
            }

            playerId = Integer.parseInt(matcher.group(1));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Could not register with the backend.\n"
                            + "Please check that the backend is running.\n\n"
                            + ex.getMessage(),
                    "Backend Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
            dispose();
            return;
        }


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);

        gameBoard = new GameBoard(gridSize, this);
        add(gameBoard, BorderLayout.CENTER);

        add(buildFooter(), BorderLayout.SOUTH);

        startTimer();
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(15, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0,0,new Color(30,10,60),getWidth(),0,new Color(10,30,60)));
                g2.fillRect(0,0,getWidth(),getHeight());
                // bottom separator line
                g2.setColor(ACCENT_PURPLE);
                g2.fillRect(0,getHeight()-2,getWidth(),2);
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Title
        titleLabel = new JLabel("MEMORY MATCH");
        titleLabel.setFont(loadFont(22f, Font.BOLD));
        titleLabel.setForeground(ACCENT_CYAN);
        header.add(titleLabel, BorderLayout.WEST);

        // Stats panel
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        stats.setOpaque(false);

        movesLabel = makeStatLabel("Moves: 0");
        timerLabel = makeStatLabel("Time: 0s");
        stats.add(timerLabel);
        stats.add(movesLabel);

        // Size toggle button
        sizeBtn = makeHeaderBtn("Grid: 4×4");
        sizeBtn.addActionListener(e -> toggleGridSize());
        stats.add(sizeBtn);

        // New Game button
        newGameBtn = makeHeaderBtn("New Game");
        newGameBtn.setForeground(ACCENT_GOLD);
        newGameBtn.addActionListener(e -> resetGame());
        stats.add(newGameBtn);

        JButton historyBtn = makeHeaderBtn("My Game History");
        historyBtn.addActionListener(e -> showGameHistory());
        stats.add(historyBtn);

        header.add(stats, BorderLayout.EAST);
        return header;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(BG_PANEL);
        footer.setBorder(new EmptyBorder(6, 0, 8, 0));
        JLabel hint = new JLabel("Click cards to reveal — find all matching pairs to win!");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(TEXT_DIM);
        footer.add(hint);
        return footer;
    }

    private JLabel makeStatLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(loadFont(13f, Font.BOLD));
        l.setForeground(TEXT_LIGHT);
        return l;
    }

    private JButton makeHeaderBtn(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isRollover() ? new Color(55,65,110) : new Color(35,45,85);
                g2.setColor(base);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(ACCENT_PURPLE);
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(loadFont(12f, Font.BOLD));
        b.setForeground(TEXT_LIGHT);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(5, 14, 5, 14));
        b.setOpaque(false);
        return b;
    }

    // ── Game control ──────────────────────────────────────────────────────────
    void incrementMoves() {
        moves++;
        movesLabel.setText("Moves: " + moves);
    }

    private void startTimer() {
        if (countTimer != null) countTimer.stop();
        elapsedSeconds = 0;
        timerLabel.setText("Time: 0s");
        countTimer = new javax.swing.Timer(1000, e -> {
            elapsedSeconds++;
            timerLabel.setText("Time: " + elapsedSeconds + "s");
        });
        countTimer.start();
    }

    void onWin() {
        countTimer.stop();
        int totalPairs = (gridSize * gridSize) / 2;
        score = Math.max(0, (totalPairs * 100) - (moves * 2));
        new Thread(() -> {
            try {
                String response = apiClient.saveGameResult(
                        playerId,
                        score,
                        moves,
                        true
                );

                System.out.println("Game result saved: " + response);

            } catch (Exception ex) {
                System.err.println(
                        "Could not save game result: " + ex.getMessage()
                );
            }
        }).start();

        // Slight delay so last card flip animation finishes
        javax.swing.Timer delay = new javax.swing.Timer(400, e -> showWinDialog());
        delay.setRepeats(false);
        delay.start();
    }

    private void showWinDialog() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel icon = new JLabel("🏆", SwingConstants.CENTER);
        icon.setFont(new Font("Dialog", Font.PLAIN, 48));
        panel.add(icon, BorderLayout.NORTH);

        JLabel msg = new JLabel(
            "<html><center><b>You Win!</b><br>Moves: " + moves +
            "&nbsp;&nbsp;Time: " + elapsedSeconds + "s</center></html>",
            SwingConstants.CENTER);
        msg.setFont(loadFont(16f, Font.BOLD));
        msg.setForeground(ACCENT_CYAN);
        panel.add(msg, BorderLayout.CENTER);

        String[] options = {"Play Again", "Change Grid", "Quit"};
        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("Panel.background", BG_PANEL);
        int choice = JOptionPane.showOptionDialog(this, panel, "🎉 Congratulations!",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) resetGame();
        else if (choice == 1) { toggleGridSize(); resetGame(); }
        else System.exit(0);
    }

    private void toggleGridSize() {
        gridSize = (gridSize == 4) ? 6 : 4;
        sizeBtn.setText("Grid: " + gridSize + "×" + gridSize);
        resetGame();
    }

    void resetGame() {
        moves = 0;
        score = 0;
        movesLabel.setText("Moves: 0");
        remove(gameBoard);
        gameBoard = new GameBoard(gridSize, this);
        add(gameBoard, BorderLayout.CENTER);
        startTimer();
        pack();
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    // ── Font helper ───────────────────────────────────────────────────────────
    private static Font loadFont(float size, int style) {
        return new Font("SansSerif", style, (int) size);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner class: GameBoard
    // ─────────────────────────────────────────────────────────────────────────
    static class GameBoard extends JPanel {
        private final int gridSize;
        private final MemoryMatchGame game;
        private CardButton[] cards;
        private CardButton firstCard = null, secondCard = null;
        private boolean locked = false;
        private int matchedPairs = 0;
        private final int totalPairs;

        GameBoard(int gridSize, MemoryMatchGame game) {
            this.gridSize = gridSize;
            this.game = game;
            this.totalPairs = (gridSize * gridSize) / 2;
            setBackground(BG_DARK);
            setBorder(new EmptyBorder(16, 18, 16, 18));
            setLayout(new GridLayout(gridSize, gridSize, 10, 10));
            initCards();
        }

        private void initCards() {
            String[] symbols = (gridSize == 4) ? SYMBOLS_4x4 : SYMBOLS_6x6;
            List<String> deck = new ArrayList<>();
            for (int i = 0; i < totalPairs; i++) {
                String s = symbols[i % symbols.length];
                deck.add(s); deck.add(s);
            }
            Collections.shuffle(deck);

            cards = new CardButton[gridSize * gridSize];
            for (int i = 0; i < cards.length; i++) {
                cards[i] = new CardButton(deck.get(i), i, this);
                add(cards[i]);
            }
        }

        void onCardClicked(CardButton card) {
            if (locked || card.isMatched() || card.isFlipped()) return;
            if (firstCard == null) {
                firstCard = card;
                card.flip(true);
            } else if (secondCard == null && card != firstCard) {
                secondCard = card;
                card.flip(true);
                game.incrementMoves();
                locked = true;
                checkMatch();
            }
        }

        private void checkMatch() {
            if (firstCard.getSymbol().equals(secondCard.getSymbol())) {
                // Match!
                javax.swing.Timer matchTimer = new javax.swing.Timer(400, e -> {
                    firstCard.setMatched();
                    secondCard.setMatched();
                    matchedPairs++;
                    resetSelection();
                    if (matchedPairs == totalPairs) game.onWin();
                });
                matchTimer.setRepeats(false);
                matchTimer.start();
            } else {
                // No match – flip back after delay
                javax.swing.Timer flipBack = new javax.swing.Timer(1200, e -> {
                    firstCard.flip(false);
                    secondCard.flip(false);
                    resetSelection();
                });
                flipBack.setRepeats(false);
                flipBack.start();
            }
        }

        private void resetSelection() {
            firstCard = null;
            secondCard = null;
            locked = false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner class: CardButton
    // ─────────────────────────────────────────────────────────────────────────
    static class CardButton extends JButton {
        private final String symbol;
        private boolean flipped = false;
        private boolean matched = false;
        private float flipProgress = 0f;   // 0 = back, 1 = front (for animation)
        private javax.swing.Timer flipTimer;
        private boolean targetFlipped = false;

        // Card size per grid
        private static final int CARD_SIZE_4 = 88;
        private static final int CARD_SIZE_6 = 72;
        private final int cardSize;

        CardButton(String symbol, int index, GameBoard board) {
            this.symbol = symbol;
            this.cardSize = (board.gridSize == 4) ? CARD_SIZE_4 : CARD_SIZE_6;
            setPreferredSize(new Dimension(cardSize, cardSize));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> board.onCardClicked(this));

            // Hover glow effect
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { repaint(); }
                @Override public void mouseExited(MouseEvent e) { repaint(); }
            });
        }

        String getSymbol() { return symbol; }
        boolean isFlipped() { return flipped; }
        boolean isMatched() { return matched; }

        void flip(boolean show) {
            targetFlipped = show;
            if (flipTimer != null) flipTimer.stop();
            // Animate flip
            float step = show ? 0.12f : -0.12f;
            flipTimer = new javax.swing.Timer(16, null);
            flipTimer.addActionListener(e -> {
                flipProgress += step;
                if (flipProgress >= 1f) { flipProgress = 1f; flipped = true; flipTimer.stop(); }
                if (flipProgress <= 0f) { flipProgress = 0f; flipped = false; flipTimer.stop(); }
                repaint();
            });
            flipTimer.start();
        }

        void setMatched() {
            matched = true;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            // Perspective squish for flip effect
            float scaleX = Math.abs(flipProgress * 2 - 1); // 1→0→1
            boolean showFront = flipProgress >= 0.5f;

            // Translate to center for horizontal scale
            g2.translate(w / 2.0, h / 2.0);
            g2.scale(Math.max(0.02, scaleX), 1.0);
            g2.translate(-w / 2.0, -h / 2.0);

            int arc = 14;
            if (showFront || flipped) {
                // Front face
                Color bg = matched ? MATCHED_BG : CARD_FRONT;
                // Shadow
                g2.setColor(new Color(0,0,0,60));
                g2.fillRoundRect(3, 4, w-4, h-4, arc, arc);
                // Card body
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w-2, h-2, arc, arc);
                // Border
                Color border = matched ? MATCHED_BORDER : ACCENT_CYAN;
                g2.setColor(border);
                g2.setStroke(new BasicStroke(matched ? 2.5f : 1.8f));
                g2.drawRoundRect(1, 1, w-4, h-4, arc-2, arc-2);
                // Symbol
                g2.setColor(matched ? ACCENT_CYAN : ACCENT_GOLD);
                g2.setFont(new Font("Dialog", Font.BOLD, cardSize / 2));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(symbol)) / 2;
                int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(symbol, tx, ty);
                // Matched sparkle overlay
                if (matched) {
                    g2.setColor(new Color(0, 200, 160, 25));
                    g2.fillRoundRect(0, 0, w-2, h-2, arc, arc);
                }
            } else {
                // Back face
                boolean hover = getModel().isRollover();
                // Shadow
                g2.setColor(new Color(0,0,0,70));
                g2.fillRoundRect(3, 4, w-4, h-4, arc, arc);
                // Gradient body
                GradientPaint gp = new GradientPaint(0,0,
                    hover ? new Color(55,65,120) : CARD_BACK,
                    w, h,
                    hover ? new Color(70,50,140) : new Color(45,55,100));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w-2, h-2, arc, arc);
                // Border
                g2.setColor(hover ? ACCENT_PURPLE : new Color(70, 90, 150));
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(1, 1, w-4, h-4, arc-2, arc-2);
                // Back pattern – small diamond grid
                g2.setColor(new Color(255,255,255,18));
                g2.setStroke(new BasicStroke(1f));
                int step = 14;
                for (int x = step; x < w-4; x += step)
                    for (int y = step; y < h-4; y += step) {
                        int s = 4;
                        int[] px = {x, x+s, x, x-s};
                        int[] py = {y-s, y, y+s, y};
                        g2.drawPolygon(px, py, 4);
                    }
                // Center "?" mark
                g2.setColor(new Color(255,255,255,40));
                g2.setFont(new Font("SansSerif", Font.BOLD, cardSize / 3));
                FontMetrics fm = g2.getFontMetrics();
                String q = "?";
                g2.drawString(q, (w-fm.stringWidth(q))/2, (h-fm.getHeight())/2+fm.getAscent());
            }
            g2.dispose();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Use system look for dialogs, but our custom painting handles the rest
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(MemoryMatchGame::new);
    }  
    
    private void showGameHistory() {
        try {
            String history =
                    apiClient.getPlayerResultsForName(playerName);

            java.util.List<Object[]> rows = new java.util.ArrayList<>();

            java.util.regex.Matcher objects =
                    java.util.regex.Pattern.compile("\\{[^{}]*\\}")
                            .matcher(history);

            while (objects.find()) {
                String item = objects.group();

                String id = extractJsonValue(item, "id");
                String scoreValue = extractJsonValue(item, "score");
                String movesValue = extractJsonValue(item, "moves");
                String completedValue = extractJsonValue(item, "completed");
                String playedAt = extractJsonValue(item, "playedAt");

                rows.add(new Object[] {
                        id,
                        scoreValue,
                        movesValue,
                        Boolean.parseBoolean(completedValue)
                                ? "Completed" : "Incomplete",
                        playedAt
                });
            }

            String[] columns = {
                    "Game ID", "Score", "Moves", "Status", "Played At"
            };

            javax.swing.table.DefaultTableModel model =
                    new javax.swing.table.DefaultTableModel(
                            rows.toArray(new Object[0][]),
                            columns
                    ) {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };

            JTable table = new JTable(model);
            table.setRowHeight(28);
            table.setAutoCreateRowSorter(true);
            table.setFillsViewportHeight(true);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(650, 250));

            if (rows.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No previous games found for " + playerName + ".",
                        "My Game History",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    scrollPane,
                    playerName + " - My Game History",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load game history.\n" + ex.getMessage(),
                    "History Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String extractJsonValue(String json, String key) {
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile(
                        "\"" + key + "\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}]+))"
                ).matcher(json);

        if (matcher.find()) {
            return matcher.group(1) != null
                    ? matcher.group(1)
                    : matcher.group(2).trim();
        }

        return "";
    }

}
