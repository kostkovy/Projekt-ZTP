import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class GamePanel extends JPanel implements Runnable, GameObserver {
    private GameManager gm = GameManager.getInstance();
    private WaveManager waveManager = new WaveManager();
    private TowerFactoryManager factoryManager = TowerFactoryManager.getInstance();

    private String selectedTowerType = "ARCHER";
    private int selectedTowerCost = 50, selectedTowerRange = 120;
    private int mouseX, mouseY;

    // Scaling variables
    private double scale = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;

    // Achievement notifications
    private ArrayList<AchievementNotification> achievementNotifications = new ArrayList<>();

    // UI Panels
    private boolean showStatistics = false;
    private boolean showLogs = false;
    private boolean showAchievements = false;
    private StatisticsObserver statsObserver;
    private LoggerObserver loggerObserver;
    private AchievementObserver achievementObserver;

    // Map transition
    private boolean isTransitioning = false;
    private float transitionAlpha = 0f;
    private static final int TRANSITION_DURATION = 120; // 2 seconds at 60fps
    private int transitionFrame = 0;

    // UI Elements
    private Rectangle btnStartGame;
    private Rectangle btnRetry;
    private Rectangle btnStartWave;
    private Rectangle[] shopButtons = new Rectangle[4];

    // Command Pattern
    private Map<Rectangle, IGameCommand> commands = new HashMap<>();

    // --- UPGRADE MENU VARIABLES (NOWE) ---
    private boolean isUpgradeMenuOpen = false;
    private ITower selectedTowerForUpgrade = null;
    private int selectedTowerIndex = -1;
    private int menuX = 0;
    private int menuY = 0;

    // Wymiary menu ulepszeń
    private final int MENU_WIDTH = 160;
    private final int MENU_HEIGHT = 100; // 3 opcje
    private final int BUTTON_HEIGHT = 33;

    public GamePanel() {
        this.setFocusable(true);
        this.setBackground(new Color(34, 139, 34));
        gm.addObserver(this);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setPreferredSize(screenSize);

        calculateScaling(screenSize.width, screenSize.height);
        initializeUIElements();

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
                // Przeliczamy pozycję myszy uwzględniając skalę
                mouseX = (int) ((e.getX() - offsetX) / scale);
                mouseY = (int) ((e.getY() - offsetY) / scale);
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

        double scaleX = (double) screenWidth / gameWidth;
        double scaleY = (double) screenHeight / gameHeight;

        scale = Math.min(scaleX, scaleY);
        scale = Math.min(scale, 1.5);

        offsetX = (int) ((screenWidth - (gameWidth * scale)) / 2);
        offsetY = (int) ((screenHeight - (gameHeight * scale)) / 2);
    }

    private void initializeUIElements() {
        btnStartGame = new Rectangle(gm.MAP_WIDTH / 2 - 100, gm.MAP_HEIGHT / 2 + 50, 200, 60);
        btnRetry = new Rectangle(gm.MAP_WIDTH / 2 - 100, gm.MAP_HEIGHT / 2 + 50, 200, 60);
        btnStartWave = new Rectangle(gm.MAP_WIDTH - 220, gm.MAP_HEIGHT + 30, 200, 60);

        for (int i = 0; i < 4; i++) {
            shopButtons[i] = new Rectangle(20 + (i * 130), gm.MAP_HEIGHT + 15, 110, 90);
        }
    }

    public void setObservers(StatisticsObserver stats, LoggerObserver logger, AchievementObserver achievement) {
        this.statsObserver = stats;
        this.loggerObserver = logger;
        this.achievementObserver = achievement;
    }

    public void toggleStatistics() { showStatistics = !showStatistics; repaint(); }
    public void toggleLogs() { showLogs = !showLogs; repaint(); }
    public void toggleAchievements() { showAchievements = !showAchievements; repaint(); }

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
        if (event.type == GameEventType.WAVE_STARTED && event.data instanceof Integer) {
            int wave = (Integer) event.data;
            if (wave == 11) {
                triggerMapTransition();
            }
        }
        repaint();
    }

    private void triggerMapTransition() {
        isTransitioning = true;
        transitionFrame = 0;
        transitionAlpha = 0f;
    }

    public void showAchievement(String title, String description) {
        achievementNotifications.add(new AchievementNotification(title, description));
    }

    // ---
    private void handleClick(MouseEvent e) {
        // Przeliczenie współrzędnych ekranu na współrzędne gry
        int x = (int) ((e.getX() - offsetX) / scale);
        int y = (int) ((e.getY() - offsetY) / scale);


        if (isUpgradeMenuOpen) {
            // Sprawdź, czy kliknięto w obszar menu
            if (x >= menuX && x <= menuX + MENU_WIDTH &&
                    y >= menuY && y <= menuY + MENU_HEIGHT) {

                int localY = y - menuY;

                if (localY < BUTTON_HEIGHT) {
                    applyUpgrade(0); // Damage
                } else if (localY < BUTTON_HEIGHT * 2) {
                    applyUpgrade(1); // Range
                } else {
                    applyUpgrade(2); // Fire Rate
                }
            }

            // Każde kliknięcie poza menu zamyka menu
            isUpgradeMenuOpen = false;
            selectedTowerForUpgrade = null;
            return;
        }


        if (gm.state == GameState.MENU && btnStartGame.contains(x, y)) {
            gm.resetGame();
        } else if (gm.state == GameState.GAME_OVER && btnRetry.contains(x, y)) {
            gm.resetGame();
        } else if (gm.state == GameState.PREP_PHASE || gm.state == GameState.WAVE_IN_PROGRESS) {

            if (y > gm.MAP_HEIGHT) {
                for (Map.Entry<Rectangle, IGameCommand> entry : commands.entrySet()) {
                    if (entry.getKey().contains(x, y)) {
                        entry.getValue().execute();
                        return;
                    }
                }
                return;
            }

            int c = x / gm.TILE_SIZE;
            int r = y / gm.TILE_SIZE;

            if (c >= 0 && c < gm.COLS && r >= 0 && r < gm.ROWS) {
                // PRAWY PRZYCISK MYSZY - OTWIERANIE MENU
                if (SwingUtilities.isRightMouseButton(e) && gm.occupiedMap[c][r]) {
                    for (int i = 0; i < gm.towers.size(); i++) {
                        ITower t = gm.towers.get(i);
                        if (Math.abs(t.getX() - (c * gm.TILE_SIZE + gm.TILE_SIZE / 2)) < 20 &&
                                Math.abs(t.getY() - (r * gm.TILE_SIZE + gm.TILE_SIZE / 2)) < 20) {

                            // Ustawiamy menu
                            selectedTowerForUpgrade = t;
                            selectedTowerIndex = i;
                            menuX = x;
                            menuY = y;

                            // Zabezpieczenie przed wyjściem poza ekran
                            if (menuX + MENU_WIDTH > gm.MAP_WIDTH) menuX = gm.MAP_WIDTH - MENU_WIDTH;
                            if (menuY + MENU_HEIGHT > gm.MAP_HEIGHT) menuY = gm.MAP_HEIGHT - MENU_HEIGHT;

                            isUpgradeMenuOpen = true;
                            break;
                        }
                    }
                }
                // LEWY PRZYCISK MYSZY - STAWIANIE WIEŻY
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

    // ulepszenia ---
    private void applyUpgrade(int type) {
        if (selectedTowerForUpgrade == null) return;

        ITower upgradedTower = null;
        int cost = 0;

        switch (type) {
            case 0: // Damage
                if (gm.money >= 100) {
                    upgradedTower = new DamageUpgradeDecorator(selectedTowerForUpgrade);
                    cost = 100;
                }
                break;
            case 1: // Range
                if (gm.money >= 80) {
                    upgradedTower = new RangeUpgradeDecorator(selectedTowerForUpgrade);
                    cost = 80;
                }
                break;
            case 2: // Fire Rate
                if (gm.money >= 120) {
                    upgradedTower = new FireRateUpgradeDecorator(selectedTowerForUpgrade);
                    cost = 120;
                }
                break;
        }

        if (upgradedTower != null) {
            gm.spendMoney(cost);
            gm.towers.set(selectedTowerIndex, upgradedTower);
            gm.towerUpgraded(cost);
        } else {
            System.out.println("Niewystarczające środki na ulepszenie.");
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
        if (isTransitioning) {
            transitionFrame++;
            if (transitionFrame < TRANSITION_DURATION / 2) {
                transitionAlpha = transitionFrame / (float)(TRANSITION_DURATION / 2);
            } else if (transitionFrame < TRANSITION_DURATION) {
                transitionAlpha = 1f - (transitionFrame - TRANSITION_DURATION / 2) / (float)(TRANSITION_DURATION / 2);
            } else {
                isTransitioning = false;
                transitionAlpha = 0f;
            }
        }

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

        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        if (gm.state == GameState.MENU) {
            drawMenu(g2);
        } else if (gm.state == GameState.GAME_OVER) {
            drawGameOver(g2);
        } else {
            drawGame(g2);
            drawUI(g2);

            // --- RYSOWANIE MENU ULEPSZEŃ (NA WIERZCHU) ---
            if (isUpgradeMenuOpen) {
                drawUpgradeMenu(g2);
            }
        }

        // Map transition overlay
        if (isTransitioning) {
            g2.setColor(new Color(255, 255, 255, (int)(transitionAlpha * 255)));
            g2.fillRect(0, 0, gm.MAP_WIDTH, gm.MAP_HEIGHT + gm.UI_HEIGHT);

            if (transitionFrame == TRANSITION_DURATION / 2) {
                g2.setColor(new Color(52, 152, 219));
                g2.setFont(new Font("Arial", Font.BOLD, 40));
                String msg = "WINTER MAP UNLOCKED!";
                int w = g2.getFontMetrics().stringWidth(msg);
                g2.drawString(msg, gm.MAP_WIDTH / 2 - w / 2, gm.MAP_HEIGHT / 2);
            }
        }

        g2.scale(1.0 / scale, 1.0 / scale);
        g2.translate(-offsetX, -offsetY);

        drawAchievementNotifications(g2);

        if (showStatistics && statsObserver != null) drawStatisticsPanel(g2);
        if (showLogs && loggerObserver != null) drawLogsPanel(g2);
        if (showAchievements && achievementObserver != null) drawAchievementsPanel(g2);
    }

    // --- METODA RYSOWANIA MENU ULEPSZEŃ (NOWE) ---
    private void drawUpgradeMenu(Graphics2D g) {
        // Tło menu (półprzezroczyste czarne)
        g.setColor(new Color(20, 20, 20, 230));
        g.fillRoundRect(menuX, menuY, MENU_WIDTH, MENU_HEIGHT, 10, 10);

        // Ramka
        g.setColor(new Color(241, 196, 15));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(menuX, menuY, MENU_WIDTH, MENU_HEIGHT, 10, 10);
        g.setStroke(new BasicStroke(1));

        // Linie oddzielające przyciski
        g.setColor(new Color(100, 100, 100));
        g.drawLine(menuX, menuY + BUTTON_HEIGHT, menuX + MENU_WIDTH, menuY + BUTTON_HEIGHT);
        g.drawLine(menuX, menuY + BUTTON_HEIGHT * 2, menuX + MENU_WIDTH, menuY + BUTTON_HEIGHT * 2);

        g.setFont(new Font("Arial", Font.BOLD, 12));

        // Opcja 1: Damage
        boolean canAffordDmg = gm.money >= 100;
        g.setColor(canAffordDmg ? Color.WHITE : Color.GRAY);
        g.drawString("💪 Atak (+25)", menuX + 10, menuY + 20);
        g.setColor(canAffordDmg ? new Color(241, 196, 15) : Color.DARK_GRAY);
        g.drawString("100$", menuX + 110, menuY + 20);

        // Opcja 2: Range
        boolean canAffordRng = gm.money >= 80;
        g.setColor(canAffordRng ? Color.WHITE : Color.GRAY);
        g.drawString("🎯 Zasięg (+50)", menuX + 10, menuY + 20 + BUTTON_HEIGHT);
        g.setColor(canAffordRng ? new Color(241, 196, 15) : Color.DARK_GRAY);
        g.drawString("80$", menuX + 110, menuY + 20 + BUTTON_HEIGHT);

        // Opcja 3: Fire Rate
        boolean canAffordSpd = gm.money >= 120;
        g.setColor(canAffordSpd ? Color.WHITE : Color.GRAY);
        g.drawString("⚡ Szybkość", menuX + 10, menuY + 20 + BUTTON_HEIGHT * 2);
        g.setColor(canAffordSpd ? new Color(241, 196, 15) : Color.DARK_GRAY);
        g.drawString("120$", menuX + 110, menuY + 20 + BUTTON_HEIGHT * 2);
    }

    private void drawMenu(Graphics2D g) {
        GradientPaint gradient = new GradientPaint(0, 0, new Color(20, 30, 48), 0, gm.MAP_HEIGHT + gm.UI_HEIGHT, new Color(36, 59, 85));
        g.setPaint(gradient);
        g.fillRect(0, 0, gm.MAP_WIDTH, gm.MAP_HEIGHT + gm.UI_HEIGHT);

        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.setColor(new Color(0, 0, 0, 100));
        g.drawString("TOWER DEFENSE", gm.MAP_WIDTH / 2 - 245, gm.MAP_HEIGHT / 2 - 48);
        g.setColor(new Color(255, 215, 0));
        g.drawString("TOWER DEFENSE", gm.MAP_WIDTH / 2 - 250, gm.MAP_HEIGHT / 2 - 50);

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

        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(200, 200, 200));
        String[] instructions = {
                "Skróty klawiszowe:",
                "S - Pokaż statystyki | L - Pokaż logi",
                "M - Włącz/Wyłącz dźwięk | A - Pokaż osiągnięcia",
                "ESC - Wyjdź z gry",
                "",
                "Fala 11+ odblokowuje ZIMOWĄ MAPĘ!"
        };
        int yPos = gm.MAP_HEIGHT + gm.UI_HEIGHT - 120;
        for (String line : instructions) {
            int width = g.getFontMetrics().stringWidth(line);
            g.drawString(line, gm.MAP_WIDTH / 2 - width / 2, yPos);
            yPos += 20;
        }
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, gm.MAP_WIDTH, gm.MAP_HEIGHT + gm.UI_HEIGHT);

        boolean isVictory = gm.wave > 20;

        if (isVictory) {
            g.setFont(new Font("Arial", Font.BOLD, 70));
            g.setColor(new Color(46, 204, 113, 150));
            g.drawString("WYGRANA!", gm.MAP_WIDTH / 2 - 185, gm.MAP_HEIGHT / 2 - 98);
            g.setColor(new Color(46, 204, 113));
            g.drawString("WYGRANA!", gm.MAP_WIDTH / 2 - 190, gm.MAP_HEIGHT / 2 - 100);

            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.setColor(new Color(241, 196, 15));
            g.drawString("Gratulacje! Ukończyłeś wszystkie 20 fal!",
                    gm.MAP_WIDTH / 2 - 300, gm.MAP_HEIGHT / 2);
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 70));
            g.setColor(new Color(231, 76, 60, 150));
            g.drawString("GAME OVER", gm.MAP_WIDTH / 2 - 215, gm.MAP_HEIGHT / 2 - 48);
            g.setColor(new Color(231, 76, 60));
            g.drawString("GAME OVER", gm.MAP_WIDTH / 2 - 220, gm.MAP_HEIGHT / 2 - 50);
        }

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
        boolean isWinter = gm.wave >= 11;

        if (isWinter) {
            drawWinterBackground(g);
        } else {
            drawGrassBackground(g);
        }

        // Draw path
        Color pathColor = isWinter ? new Color(200, 220, 240) : new Color(139, 119, 101);
        Color borderColor = isWinter ? new Color(180, 200, 220) : new Color(101, 84, 69);

        g.setColor(pathColor);
        g.setStroke(new BasicStroke(gm.TILE_SIZE - 5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Point[] p = gm.pathPoints;
        for (int i = 0; i < p.length - 1; i++) {
            g.drawLine(p[i].x, p[i].y, p[i + 1].x, p[i + 1].y);
        }

        g.setColor(borderColor);
        g.setStroke(new BasicStroke(gm.TILE_SIZE - 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < p.length - 1; i++) {
            g.drawLine(p[i].x, p[i].y, p[i + 1].x, p[i + 1].y);
        }

        g.setStroke(new BasicStroke(1));

        for (Enemy e : gm.enemies) {
            e.draw(g);
        }
        for (ITower t : gm.towers) {
            t.draw(g);
        }
        for (Projectile pr : gm.projectiles) {
            pr.draw(g);
        }

        // Ghost Tower
        if (mouseY < gm.MAP_HEIGHT && gm.state != GameState.GAME_OVER && !isUpgradeMenuOpen) {
            int c = mouseX / gm.TILE_SIZE;
            int r = mouseY / gm.TILE_SIZE;
            int x = c * gm.TILE_SIZE;
            int y = r * gm.TILE_SIZE;
            boolean can = (c >= 0 && c < gm.COLS && r >= 0 && r < gm.ROWS && !gm.occupiedMap[c][r]);

            g.setColor(can ? new Color(46, 204, 113, 80) : new Color(231, 76, 60, 80));
            g.fillRoundRect(x + 2, y + 2, gm.TILE_SIZE - 4, gm.TILE_SIZE - 4, 10, 10);

            g.setColor(can ? new Color(46, 204, 113, 150) : new Color(231, 76, 60, 150));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(x + 2, y + 2, gm.TILE_SIZE - 4, gm.TILE_SIZE - 4, 10, 10);
            g.setColor(can ? new Color(52, 152, 219, 100) : new Color(231, 76, 60, 100));

            // Range indicator
            g.setColor(can ? new Color(255, 255, 255, 181) : new Color(231, 76, 60, 100));
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
        g.setColor(new Color(60, 179, 113));
        g.fillRect(0, 0, gm.MAP_WIDTH, gm.MAP_HEIGHT);

        for (int x = 0; x < gm.MAP_WIDTH; x += 20) {
            for (int y = 0; y < gm.MAP_HEIGHT; y += 20) {
                if ((x + y) % 40 == 0) {
                    g.setColor(new Color(55, 165, 105, 30));
                    g.fillRect(x, y, 20, 20);
                }
            }
        }
    }

    private void drawWinterBackground(Graphics2D g) {
        g.setColor(new Color(240, 248, 255));
        g.fillRect(0, 0, gm.MAP_WIDTH, gm.MAP_HEIGHT);

        for (int x = 0; x < gm.MAP_WIDTH; x += 30) {
            for (int y = 0; y < gm.MAP_HEIGHT; y += 30) {
                if (Math.random() > 0.8) {
                    g.setColor(new Color(200, 220, 240, 100));
                    g.fillOval(x, y, 8, 8);
                }
                if ((x + y) % 60 == 0) {
                    g.setColor(new Color(220, 235, 250, 50));
                    g.fillRect(x, y, 25, 25);
                }
            }
        }
    }

    private void drawUI(Graphics2D g) {
        boolean isWinter = gm.wave >= 11;
        Color uiColor1 = isWinter ? new Color(40, 50, 70) : new Color(30, 39, 46);
        Color uiColor2 = isWinter ? new Color(50, 60, 80) : new Color(45, 52, 54);

        GradientPaint uiGradient = new GradientPaint(0, gm.MAP_HEIGHT, uiColor1,
                0, gm.MAP_HEIGHT + gm.UI_HEIGHT, uiColor2);
        g.setPaint(uiGradient);
        g.fillRect(0, gm.MAP_HEIGHT, gm.MAP_WIDTH, gm.UI_HEIGHT);

        g.setColor(new Color(99, 110, 114));
        g.setStroke(new BasicStroke(2));
        g.drawLine(0, gm.MAP_HEIGHT, gm.MAP_WIDTH, gm.MAP_HEIGHT);
        g.setStroke(new BasicStroke(1));

        int statsX = 560;
        int statsY = gm.MAP_HEIGHT + 20;

        drawStatBox(g, statsX, statsY, "💰 PIENIĄDZE", String.valueOf(gm.money), new Color(241, 196, 15));
        drawStatBox(g, statsX, statsY + 35, "❤ ŻYCIA", String.valueOf(gm.lives), new Color(231, 76, 60));
        drawStatBox(g, statsX, statsY + 70, isWinter ? "❄ FALA" : "🌊 FALA", String.valueOf(gm.wave),
                isWinter ? new Color(100, 200, 255) : new Color(52, 152, 219));

        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.setColor(new Color(149, 165, 166));
        g.drawString("PPM = Ulepsz (100-120$)", statsX + 180, statsY + 20);

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

            if (sel) {
                g.setColor(colors[i]);
                g.fillRoundRect(shopButtons[i].x - 2, shopButtons[i].y - 2,
                        shopButtons[i].width + 4, shopButtons[i].height + 4, 12, 12);
            }

            g.setColor(canAfford ? new Color(52, 73, 94) : new Color(40, 40, 40));
            g.fillRoundRect(shopButtons[i].x, shopButtons[i].y,
                    shopButtons[i].width, shopButtons[i].height, 10, 10);

            g.setColor(sel ? colors[i] : (canAfford ? new Color(99, 110, 114) : new Color(60, 60, 60)));
            g.setStroke(new BasicStroke(sel ? 3 : 2));
            g.drawRoundRect(shopButtons[i].x, shopButtons[i].y,
                    shopButtons[i].width, shopButtons[i].height, 10, 10);
            g.setStroke(new BasicStroke(1));

            g.setColor(colors[i]);
            g.fillOval(shopButtons[i].x + 5, shopButtons[i].y + 5, 20, 20);

            g.setColor(canAfford ? Color.WHITE : new Color(100, 100, 100));
            g.setFont(new Font("Arial", Font.BOLD, 11));
            g.drawString(names[i], shopButtons[i].x + 8, shopButtons[i].y + 45);

            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(canAfford ? new Color(241, 196, 15) : new Color(150, 150, 100));
            g.drawString(costs[i] + "$", shopButtons[i].x + 35, shopButtons[i].y + 70);
        }

        boolean canStart = gm.state == GameState.PREP_PHASE;

        if (canStart) {
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

    private void drawStatisticsPanel(Graphics2D g) {
        int panelWidth = 500;
        int panelHeight = 400;
        int x = (getWidth() - panelWidth) / 2;
        int y = (getHeight() - panelHeight) / 2;

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, panelWidth, panelHeight, 20, 20);

        g.setColor(new Color(52, 152, 219));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(x, y, panelWidth, panelHeight, 20, 20);

        g.setColor(new Color(52, 152, 219));
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("📊 STATYSTYKI", x + 20, y + 40);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        int yPos = y + 80;
        int lineHeight = 35;

        String[] labels = {
                "Przeciwnicy zabici:",
                "Wieże zbudowane:",
                "Pieniądze zarobione:",
                "Pieniądze wydane:",
                "Fale ukończone:",
                "Najwyższa fala:"
        };

        String[] values = {
                String.valueOf(statsObserver.getTotalEnemiesKilled()),
                String.valueOf(statsObserver.getTotalTowersBuilt()),
                statsObserver.getTotalMoneyEarned() + "$",
                statsObserver.getTotalMoneySpent() + "$",
                String.valueOf(statsObserver.getTotalWavesCompleted()),
                String.valueOf(statsObserver.getHighestWaveReached())
        };

        for (int i = 0; i < labels.length; i++) {
            g.setColor(new Color(200, 200, 200));
            g.drawString(labels[i], x + 30, yPos);

            g.setColor(new Color(241, 196, 15));
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString(values[i], x + 320, yPos);
            g.setFont(new Font("Arial", Font.PLAIN, 18));

            yPos += lineHeight;
        }

        g.setFont(new Font("Arial", Font.ITALIC, 14));
        g.setColor(new Color(150, 150, 150));
        g.drawString("Naciśnij 'S' aby zamknąć", x + 150, y + panelHeight - 20);
    }

    private void drawLogsPanel(Graphics2D g) {
        int panelWidth = 600;
        int panelHeight = 500;
        int x = (getWidth() - panelWidth) / 2;
        int y = (getHeight() - panelHeight) / 2;

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, panelWidth, panelHeight, 20, 20);

        g.setColor(new Color(155, 89, 182));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(x, y, panelWidth, panelHeight, 20, 20);

        g.setColor(new Color(155, 89, 182));
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("📝 DZIENNIK ZDARZEŃ", x + 20, y + 40);

        List<String> logs = loggerObserver.getEventLog();
        int displayCount = Math.min(12, logs.size());
        int startIndex = Math.max(0, logs.size() - displayCount);

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        int yPos = y + 80;
        int lineHeight = 30;

        for (int i = startIndex; i < logs.size(); i++) {
            String log = logs.get(i);
            if (log.length() > 65) {
                log = log.substring(0, 62) + "...";
            }

            g.setColor(new Color(220, 220, 220));
            g.drawString(log, x + 20, yPos);
            yPos += lineHeight;
        }

        g.setFont(new Font("Arial", Font.ITALIC, 14));
        g.setColor(new Color(150, 150, 150));
        g.drawString("Naciśnij 'L' aby zamknąć", x + 200, y + panelHeight - 20);
    }

    private void drawAchievementsPanel(Graphics2D g) {
        int panelWidth = 550;
        int panelHeight = 450;
        int x = (getWidth() - panelWidth) / 2;
        int y = (getHeight() - panelHeight) / 2;

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, panelWidth, panelHeight, 20, 20);

        g.setColor(new Color(241, 196, 15));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(x, y, panelWidth, panelHeight, 20, 20);

        g.setColor(new Color(241, 196, 15));
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("🏆 OSIĄGNIĘCIA", x + 20, y + 40);

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(new Color(200, 200, 200));
        g.drawString("Odblokowano: " + achievementObserver.getAchievementCount() + "/9", x + 20, y + 70);

        String[][] achievements = {
                {"first_blood", "First Blood", "Zabij 10 wrogów"},
                {"slayer", "Slayer", "Zabij 50 wrogów"},
                {"massacre", "Massacre", "Zabij 100 wrogów"},
                {"builder", "Builder", "Zbuduj 5 wież"},
                {"architect", "Architect", "Zbuduj 15 wież"},
                {"survivor", "Survivor", "Przetrwaj 5 fal"},
                {"veteran", "Veteran", "Przetrwaj 10 fal"},
                {"legend", "Legend", "Przetrwaj 20 fal"},
                {"winter_warrior", "Winter Warrior", "Osiągnij falę zimową"}
        };

        int yPos = y + 110;
        int lineHeight = 35;

        for (String[] achievement : achievements) {
            boolean unlocked = achievementObserver.getUnlockedAchievements().contains(achievement[0]);

            if (unlocked) {
                g.setColor(new Color(46, 204, 113));
                g.fillOval(x + 20, yPos - 15, 20, 20);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.drawString("✓", x + 25, yPos);
            } else {
                g.setColor(new Color(60, 60, 60));
                g.fillOval(x + 20, yPos - 15, 20, 20);
            }

            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(unlocked ? new Color(241, 196, 15) : new Color(120, 120, 120));
            g.drawString(achievement[1], x + 50, yPos);

            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.setColor(unlocked ? new Color(200, 200, 200) : new Color(100, 100, 100));
            g.drawString(achievement[2], x + 50, yPos + 18);

            yPos += lineHeight;
        }

        g.setFont(new Font("Arial", Font.ITALIC, 14));
        g.setColor(new Color(150, 150, 150));
        g.drawString("Naciśnij 'A' aby zamknąć", x + 180, y + panelHeight - 20);
    }

    private class AchievementNotification {
        private String title;
        private String description;
        private long startTime;
        private static final long DISPLAY_TIME = 4000;
        private static final long FADE_TIME = 500;

        public AchievementNotification(String title, String description) {
            this.title = title;
            this.description = description;
            this.startTime = System.currentTimeMillis();
        }

        public void update() {
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - startTime > DISPLAY_TIME;
        }

        public void draw(Graphics2D g, int x, int y) {
            long elapsed = System.currentTimeMillis() - startTime;
            float alpha = 1.0f;

            if (elapsed < 300) {
                alpha = elapsed / 300f;
            } else if (elapsed > DISPLAY_TIME - FADE_TIME) {
                alpha = (DISPLAY_TIME - elapsed) / (float) FADE_TIME;
            }

            alpha = Math.max(0, Math.min(1, alpha));

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            g.setColor(new Color(241, 196, 15, (int) (200 * alpha)));
            g.fillRoundRect(x - 3, y - 3, 306, 76, 15, 15);

            g.setColor(new Color(46, 52, 54, (int) (230 * alpha)));
            g.fillRoundRect(x, y, 300, 70, 12, 12);

            g.setColor(new Color(241, 196, 15, (int) (255 * alpha)));
            g.setStroke(new BasicStroke(2));
            g.drawRoundRect(x, y, 300, 70, 12, 12);

            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.setColor(new Color(241, 196, 15, (int) (255 * alpha)));
            g.drawString("🏆", x + 10, y + 42);

            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(new Color(255, 255, 255, (int) (255 * alpha)));
            g.drawString(title, x + 50, y + 30);

            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.setColor(new Color(200, 200, 200, (int) (255 * alpha)));
            g.drawString(description, x + 50, y + 50);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
    }
}