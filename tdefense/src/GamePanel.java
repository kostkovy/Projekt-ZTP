import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

class GamePanel extends JPanel implements Runnable, GameObserver {
    private GameManager gm = GameManager.getInstance();
    private WaveManager waveManager = new WaveManager();
    private TowerFactoryManager factoryManager = TowerFactoryManager.getInstance();

    private String selectedTowerType = "ARCHER";
    private int selectedTowerCost = 50, selectedTowerRange = 120;
    private int mouseX, mouseY;

    // Scaling variables
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;

    // Achievement notifications
    private ArrayList<AchievementNotification> achievementNotifications = new ArrayList<>();

    // UI Elements (will be scaled)
    private Rectangle btnStartGame;
    private Rectangle btnRetry;
    private Rectangle btnStartWave;
    private Rectangle[] shopButtons = new Rectangle[4];

    // Mapa Komend (COMMAND PATTERN)
    private Map<Rectangle, IGameCommand> commands = new HashMap<>();

    public GamePanel() {
        this.setFocusable(true);
        this.setBackground(new Color(34, 139, 34));
        gm.addObserver(this);

        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setPreferredSize(screenSize);

        // Calculate scaling
        calculateScaling(screenSize.width, screenSize.height);

        // Initialize UI elements
        initializeUIElements();

        // Setup Commands
        commands.put(shopButtons[0], new BuyTowerCommand(this, "ARCHER",
                factoryManager.getTowerCost("ARCHER"), factoryManager.getTowerRange("ARCHER")));
        commands.put(shopButtons[1], new BuyTowerCommand(this, "CANNON",
                factoryManager.getTowerCost("CANNON"), factoryManager.getTowerRange("CANNON")));
        commands.put(shopButtons[2], new BuyTowerCommand(this, "SNIPER",
                factoryManager.getTowerCost("SNIPER"), factoryManager.getTowerRange("SNIPER")));
        commands.put(shopButtons[3], new BuyTowerCommand(this, "LASER",
                factoryManager.getTowerCost("LASER"), factoryManager.getTowerRange("LASER")));
        commands.put(btnStartWave, new StartWaveCommand(waveManager));

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = (int) ((e.getX() - offsetX) / scaleX);
                mouseY = (int) ((e.getY() - offsetY) / scaleY);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e);
            }
        };
        this.addMouseListener(ma);
        this.addMouseMotionListener(ma);
    }

    private void calculateScaling(int screenWidth, int screenHeight) {
        int gameWidth = gm.MAP_WIDTH;
        int gameHeight = gm.MAP_HEIGHT + gm.UI_HEIGHT;

        // Calculate scale to fit screen while maintaining aspect ratio
        double scaleXTemp = (double) screenWidth / gameWidth;
        double scaleYTemp = (double) screenHeight / gameHeight;

        // Use the smaller scale to ensure everything fits
        scaleX = scaleY = Math.min(scaleXTemp, scaleYTemp);

        // Calculate offsets to center the game
        offsetX = (int) ((screenWidth - (gameWidth * scaleX)) / 2);
        offsetY = (int) ((screenHeight - (gameHeight * scaleY)) / 2);
    }

    private void initializeUIElements() {
        // Initialize buttons with original coordinates (will be scaled during drawing)
        btnStartGame = new Rectangle(gm.MAP_WIDTH / 2 - 100, gm.MAP_HEIGHT / 2 + 50, 200, 60);
        btnRetry = new Rectangle(gm.MAP_WIDTH / 2 - 100, gm.MAP_HEIGHT / 2 + 50, 200, 60);
        btnStartWave = new Rectangle(gm.MAP_WIDTH - 220, gm.MAP_HEIGHT + 30, 200, 60);

        for (int i = 0; i < 4; i++) {
            shopButtons[i] = new Rectangle(20 + (i * 130), gm.MAP_HEIGHT + 15, 110, 90);
        }
    }

    private Rectangle scaleRect(Rectangle r) {
        return new Rectangle(
                (int) (r.x * scaleX) + offsetX,
                (int) (r.y * scaleY) + offsetY,
                (int) (r.width * scaleX),
                (int) (r.height * scaleY)
        );
    }

    public void setSelectedTower(String type, int cost, int range) {
        this.selectedTowerType = type;
        this.selectedTowerCost = cost;
        this.selectedTowerRange = range;
    }

    @Override
    public void onGameUpdate() {
        repaint();
    }

    @Override
    public void onGameEvent(GameEvent event) {
        repaint();
    }

    public void showAchievement(String title, String description) {
        achievementNotifications.add(new AchievementNotification(title, description));
    }

    private void handleClick(MouseEvent e) {
        int x = (int) ((e.getX() - offsetX) / scaleX);
        int y = (int) ((e.getY() - offsetY) / scaleY);

        if (gm.state == GameState.MENU && btnStartGame.contains(x, y)) {
            gm.resetGame();
        } else if (gm.state == GameState.GAME_OVER && btnRetry.contains(x, y)) {
            gm.resetGame();
        } else if (gm.state == GameState.PREP_PHASE || gm.state == GameState.WAVE_IN_PROGRESS) {

            // Obsługa UI przez COMMAND PATTERN
            if (y > gm.MAP_HEIGHT) {
                for (Map.Entry<Rectangle, IGameCommand> entry : commands.entrySet()) {
                    if (entry.getKey().contains(x, y)) {
                        entry.getValue().execute();
                        return;
                    }
                }
                return;
            }

            // Obsługa Mapy
            int c = x / gm.TILE_SIZE;
            int r = y / gm.TILE_SIZE;
            if (c >= 0 && c < gm.COLS && r >= 0 && r < gm.ROWS) {

                // Prawy Przycisk Myszy -> ULEPSZENIE
                if (SwingUtilities.isRightMouseButton(e) && gm.occupiedMap[c][r]) {
                    for (int i = 0; i < gm.towers.size(); i++) {
                        ITower t = gm.towers.get(i);
                        if (Math.abs(t.getX() - (c * gm.TILE_SIZE + gm.TILE_SIZE / 2)) < 20 &&
                                Math.abs(t.getY() - (r * gm.TILE_SIZE + gm.TILE_SIZE / 2)) < 20) {

                            if (!(t instanceof UpgradeDecorator) && gm.money >= 100) {
                                gm.spendMoney(100);
                                gm.towers.set(i, new UpgradeDecorator(t));
                                gm.towerUpgraded(100);
                            }
                        }
                    }
                }
                // Lewy Przycisk Myszy -> BUDOWANIE
                else if (SwingUtilities.isLeftMouseButton(e) && !gm.occupiedMap[c][r]) {
                    if (gm.money >= selectedTowerCost) {
                        gm.spendMoney(selectedTowerCost);
                        int tx = c * gm.TILE_SIZE + gm.TILE_SIZE / 2;
                        int ty = r * gm.TILE_SIZE + gm.TILE_SIZE / 2;

                        ITower t = factoryManager.createTower(selectedTowerType, tx, ty);
                        gm.towers.add(t);
                        gm.occupiedMap[c][r] = true;
                        gm.towerBuilt(selectedTowerCost);
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000.0 / 60.0;
        while (true) {
            long now = System.nanoTime();
            if (now - lastTime >= nsPerTick) {
                update();
                repaint();
                lastTime = now;
            }
            try {
                Thread.sleep(2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        // Update achievement notifications
        Iterator<AchievementNotification> iter = achievementNotifications.iterator();
        while (iter.hasNext()) {
            AchievementNotification notif = iter.next();
            notif.update();
            if (notif.isExpired()) {
                iter.remove();
            }
        }

        if (gm.state == GameState.WAVE_IN_PROGRESS) {
            waveManager.update();
        }
        if (gm.state == GameState.PREP_PHASE || gm.state == GameState.WAVE_IN_PROGRESS) {
            for (Enemy enemy : gm.enemies) {
                enemy.update();
                if (enemy.finished) {
                    gm.takeDamage();
                    gm.enemies.remove(enemy);
                } else if (!enemy.alive) {
                    gm.addMoney(enemy.reward);
                    gm.enemyKilled(enemy.reward);
                    gm.enemies.remove(enemy);
                }
            }
            for (ITower t : gm.towers) {
                t.update();
            }
            for (Projectile p : gm.projectiles) {
                p.update();
                if (!p.active) {
                    gm.projectiles.remove(p);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Fill background (black bars)
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Apply scaling transformation
        g2.translate(offsetX, offsetY);
        g2.scale(scaleX, scaleY);

        if (gm.state == GameState.MENU) {
            drawMenu(g2);
        } else if (gm.state == GameState.GAME_OVER) {
            drawGameOver(g2);
        } else {
            drawGame(g2);
            drawUI(g2);
        }

        // Reset transformation for achievement notifications
        g2.scale(1.0 / scaleX, 1.0 / scaleY);
        g2.translate(-offsetX, -offsetY);

        // Draw achievement notifications on top (in screen space)
        drawAchievementNotifications(g2);
    }

    private void drawMenu(Graphics2D g) {
        // Gradient background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(20, 30, 48), 0, gm.MAP_HEIGHT + gm.UI_HEIGHT, new Color(36, 59, 85));
        g.setPaint(gradient);
        g.fillRect(0, 0, gm.MAP_WIDTH, gm.MAP_HEIGHT + gm.UI_HEIGHT);

        // Title with shadow
        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.setColor(new Color(0, 0, 0, 100));
        g.drawString("TOWER DEFENSE", gm.MAP_WIDTH / 2 - 245, gm.MAP_HEIGHT / 2 - 48);
        g.setColor(new Color(255, 215, 0));
        g.drawString("TOWER DEFENSE", gm.MAP_WIDTH / 2 - 250, gm.MAP_HEIGHT / 2 - 50);

        // Start button with glow
        g.setColor(new Color(46, 204, 113));
        g.fillRoundRect((int) btnStartGame.getX(), (int) btnStartGame.getY(),
                (int) btnStartGame.getWidth(), (int) btnStartGame.getHeight(), 15, 15);
        g.setColor(new Color(39, 174, 96));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect((int) btnStartGame.getX(), (int) btnStartGame.getY(),
                (int) btnStartGame.getWidth(), (int) btnStartGame.getHeight(), 15, 15);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("START GAME", (int) btnStartGame.getX() + 30, (int) btnStartGame.getY() + 40);

        // Instructions
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(200, 200, 200));
        String[] instructions = {
                "Skróty klawiszowe:",
                "S - Pokaż statystyki | L - Pokaż logi",
                "M - Włącz/Wyłącz dźwięk | A - Pokaż osiągnięcia",
                "ESC - Wyjdź z gry"
        };
        int yPos = gm.MAP_HEIGHT + gm.UI_HEIGHT - 100;
        for (String line : instructions) {
            int width = g.getFontMetrics().stringWidth(line);
            g.drawString(line, gm.MAP_WIDTH / 2 - width / 2, yPos);
            yPos += 20;
        }
    }

    private void drawGameOver(Graphics2D g) {
        // Dark overlay
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, gm.MAP_WIDTH, gm.MAP_HEIGHT + gm.UI_HEIGHT);

        // Game Over text with glow
        g.setFont(new Font("Arial", Font.BOLD, 70));
        g.setColor(new Color(231, 76, 60, 150));
        g.drawString("GAME OVER", gm.MAP_WIDTH / 2 - 215, gm.MAP_HEIGHT / 2 - 48);
        g.setColor(new Color(231, 76, 60));
        g.drawString("GAME OVER", gm.MAP_WIDTH / 2 - 220, gm.MAP_HEIGHT / 2 - 50);

        // Retry button
        g.setColor(new Color(230, 126, 34));
        g.fillRoundRect((int) btnRetry.getX(), (int) btnRetry.getY(),
                (int) btnRetry.getWidth(), (int) btnRetry.getHeight(), 15, 15);
        g.setColor(new Color(211, 84, 0));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect((int) btnRetry.getX(), (int) btnRetry.getY(),
                (int) btnRetry.getWidth(), (int) btnRetry.getHeight(), 15, 15);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.drawString("RETRY", (int) btnRetry.getX() + 65, (int) btnRetry.getY() + 40);
    }

    private void drawGame(Graphics2D g) {
        // Draw grass background with pattern
        drawGrassBackground(g);

        // Draw path with stones
        g.setColor(new Color(139, 119, 101));
        g.setStroke(new BasicStroke(gm.TILE_SIZE - 5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Point[] p = gm.pathPoints;
        for (int i = 0; i < p.length - 1; i++) {
            g.drawLine(p[i].x, p[i].y, p[i + 1].x, p[i + 1].y);
        }

        // Draw path border
        g.setColor(new Color(101, 84, 69));
        g.setStroke(new BasicStroke(gm.TILE_SIZE - 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < p.length - 1; i++) {
            g.drawLine(p[i].x, p[i].y, p[i + 1].x, p[i + 1].y);
        }

        g.setStroke(new BasicStroke(1));

        // Draw entities
        for (Enemy e : gm.enemies) {
            e.draw(g);
        }
        for (ITower t : gm.towers) {
            t.draw(g);
        }
        for (Projectile pr : gm.projectiles) {
            pr.draw(g);
        }

        // Ghost Tower with glow
        if (mouseY < gm.MAP_HEIGHT && gm.state != GameState.GAME_OVER) {
            int c = mouseX / gm.TILE_SIZE;
            int r = mouseY / gm.TILE_SIZE;
            int x = c * gm.TILE_SIZE;
            int y = r * gm.TILE_SIZE;
            boolean can = (c >= 0 && c < gm.COLS && r >= 0 && r < gm.ROWS && !gm.occupiedMap[c][r]);

            // Draw placement preview
            g.setColor(can ? new Color(46, 204, 113, 80) : new Color(231, 76, 60, 80));
            g.fillRoundRect(x + 2, y + 2, gm.TILE_SIZE - 4, gm.TILE_SIZE - 4, 10, 10);

            g.setColor(can ? new Color(46, 204, 113, 150) : new Color(231, 76, 60, 150));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(x + 2, y + 2, gm.TILE_SIZE - 4, gm.TILE_SIZE - 4, 10, 10);

            // Range indicator
            g.setColor(can ? new Color(52, 152, 219, 100) : new Color(231, 76, 60, 100));
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            g.drawOval(
                    x + gm.TILE_SIZE / 2 - selectedTowerRange,
                    y + gm.TILE_SIZE / 2 - selectedTowerRange,
                    selectedTowerRange * 2,
                    selectedTowerRange * 2
            );
            g.setStroke(new BasicStroke(1));
        }
    }

    private void drawGrassBackground(Graphics2D g) {
        // Base grass color
        g.setColor(new Color(60, 179, 113));
        g.fillRect(0, 0, gm.MAP_WIDTH, gm.MAP_HEIGHT);

        // Draw grass pattern
        for (int x = 0; x < gm.MAP_WIDTH; x += 20) {
            for (int y = 0; y < gm.MAP_HEIGHT; y += 20) {
                // Darker grass stripes
                if ((x + y) % 40 == 0) {
                    g.setColor(new Color(55, 165, 105, 30));
                    g.fillRect(x, y, 20, 20);
                }

                // Add small grass details
                if (Math.random() > 0.7) {
                    g.setColor(new Color(50, 155, 100, 50));
                    g.fillOval(x + (int) (Math.random() * 15), y + (int) (Math.random() * 15), 3, 3);
                }
            }
        }
    }

    private void drawUI(Graphics2D g) {
        // Modern dark UI background with gradient
        GradientPaint uiGradient = new GradientPaint(0, gm.MAP_HEIGHT, new Color(30, 39, 46),
                0, gm.MAP_HEIGHT + gm.UI_HEIGHT, new Color(45, 52, 54));
        g.setPaint(uiGradient);
        g.fillRect(0, gm.MAP_HEIGHT, gm.MAP_WIDTH, gm.UI_HEIGHT);

        // Top border line
        g.setColor(new Color(99, 110, 114));
        g.setStroke(new BasicStroke(2));
        g.drawLine(0, gm.MAP_HEIGHT, gm.MAP_WIDTH, gm.MAP_HEIGHT);
        g.setStroke(new BasicStroke(1));

        // Stats panel with icons
        int statsX = 560;
        int statsY = gm.MAP_HEIGHT + 20;

        // Money
        drawStatBox(g, statsX, statsY, "💰 PIENIĄDZE", String.valueOf(gm.money), new Color(241, 196, 15));

        // Lives
        drawStatBox(g, statsX, statsY + 35, "❤ ŻYCIA", String.valueOf(gm.lives), new Color(231, 76, 60));

        // Wave
        drawStatBox(g, statsX, statsY + 70, "🌊 FALA", String.valueOf(gm.wave), new Color(52, 152, 219));

        // Help text
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.setColor(new Color(149, 165, 166));
        g.drawString("PPM = Ulepsz (100$)", statsX + 180, statsY + 20);

        // Shop buttons with enhanced design
        String[] names = {"🏹 ŁUCZNIK", "💣 ARMATA", "🎯 SNAJPER", "⚡ LASER"};
        int[] costs = {50, 120, 250, 80};
        Color[] colors = {
                new Color(52, 152, 219),
                new Color(127, 140, 141),
                new Color(155, 89, 182),
                new Color(26, 188, 156)
        };

        for (int i = 0; i < 4; i++) {
            boolean sel = selectedTowerType.equals(new String[]{"ARCHER", "CANNON", "SNIPER", "LASER"}[i]);
            boolean canAfford = gm.money >= costs[i];

            // Button background
            if (sel) {
                g.setColor(colors[i]);
                g.fillRoundRect(shopButtons[i].x - 2, shopButtons[i].y - 2,
                        shopButtons[i].width + 4, shopButtons[i].height + 4, 12, 12);
            }

            g.setColor(canAfford ? new Color(52, 73, 94) : new Color(40, 40, 40));
            g.fillRoundRect(shopButtons[i].x, shopButtons[i].y,
                    shopButtons[i].width, shopButtons[i].height, 10, 10);

            // Border
            g.setColor(sel ? colors[i] : (canAfford ? new Color(99, 110, 114) : new Color(60, 60, 60)));
            g.setStroke(new BasicStroke(sel ? 3 : 2));
            g.drawRoundRect(shopButtons[i].x, shopButtons[i].y,
                    shopButtons[i].width, shopButtons[i].height, 10, 10);
            g.setStroke(new BasicStroke(1));

            // Tower icon color indicator
            g.setColor(colors[i]);
            g.fillOval(shopButtons[i].x + 5, shopButtons[i].y + 5, 20, 20);

            // Text
            g.setColor(canAfford ? Color.WHITE : new Color(100, 100, 100));
            g.setFont(new Font("Arial", Font.BOLD, 11));
            g.drawString(names[i], shopButtons[i].x + 8, shopButtons[i].y + 45);

            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(canAfford ? new Color(241, 196, 15) : new Color(150, 150, 100));
            g.drawString(costs[i] + "$", shopButtons[i].x + 35, shopButtons[i].y + 70);
        }

        // Start Wave button with glow effect
        boolean canStart = gm.state == GameState.PREP_PHASE;

        if (canStart) {
            // Glow effect
            g.setColor(new Color(46, 204, 113, 50));
            g.fillRoundRect(btnStartWave.x - 3, btnStartWave.y - 3,
                    btnStartWave.width + 6, btnStartWave.height + 6, 18, 18);
        }

        g.setColor(canStart ? new Color(46, 204, 113) : new Color(70, 70, 70));
        g.fillRoundRect(btnStartWave.x, btnStartWave.y,
                btnStartWave.width, btnStartWave.height, 15, 15);

        g.setColor(canStart ? new Color(39, 174, 96) : new Color(50, 50, 50));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(btnStartWave.x, btnStartWave.y,
                btnStartWave.width, btnStartWave.height, 15, 15);
        g.setStroke(new BasicStroke(1));

        g.setColor(canStart ? Color.WHITE : new Color(120, 120, 120));
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("▶ START WAVE", btnStartWave.x + 30, btnStartWave.y + 38);
    }

    private void drawStatBox(Graphics2D g, int x, int y, String label, String value, Color accentColor) {
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.setColor(new Color(149, 165, 166));
        g.drawString(label, x, y);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.setColor(accentColor);
        g.drawString(value, x + 120, y);
    }

    private void drawAchievementNotifications(Graphics2D g) {
        int yOffset = 80;
        int screenWidth = getWidth();
        for (AchievementNotification notif : achievementNotifications) {
            notif.draw(g, screenWidth - 320, yOffset);
            yOffset += 90;
        }
    }

    // Inner class for achievement notifications
    private class AchievementNotification {
        private String title;
        private String description;
        private long startTime;
        private static final long DISPLAY_TIME = 4000; // 4 seconds
        private static final long FADE_TIME = 500;

        public AchievementNotification(String title, String description) {
            this.title = title;
            this.description = description;
            this.startTime = System.currentTimeMillis();
        }

        public void update() {
            // Nothing to update for now
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - startTime > DISPLAY_TIME;
        }

        public void draw(Graphics2D g, int x, int y) {
            long elapsed = System.currentTimeMillis() - startTime;
            float alpha = 1.0f;

            // Fade in
            if (elapsed < 300) {
                alpha = elapsed / 300f;
            }
            // Fade out
            else if (elapsed > DISPLAY_TIME - FADE_TIME) {
                alpha = (DISPLAY_TIME - elapsed) / (float) FADE_TIME;
            }

            alpha = Math.max(0, Math.min(1, alpha));

            // Draw notification box
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            // Background with glow
            g.setColor(new Color(241, 196, 15, (int) (200 * alpha)));
            g.fillRoundRect(x - 3, y - 3, 306, 76, 15, 15);

            g.setColor(new Color(46, 52, 54, (int) (230 * alpha)));
            g.fillRoundRect(x, y, 300, 70, 12, 12);

            // Border
            g.setColor(new Color(241, 196, 15, (int) (255 * alpha)));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(x, y, 300, 70, 12, 12);

            // Trophy icon
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.setColor(new Color(241, 196, 15, (int) (255 * alpha)));
            g.drawString("🏆", x + 10, y + 42);

            // Text
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.setColor(new Color(255, 255, 255, (int) (255 * alpha)));
            g.drawString(title, x + 50, y + 45);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
}