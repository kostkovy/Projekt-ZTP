import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.Map;
import java.util.HashMap;

class GamePanel extends JPanel implements Runnable, GameObserver {
    private GameManager gm = GameManager.getInstance();
    private WaveManager waveManager = new WaveManager();
    private TowerFactoryManager factoryManager = TowerFactoryManager.getInstance();

    private String selectedTowerType = "ARCHER";
    private int selectedTowerCost = 50, selectedTowerRange = 120;
    private int mouseX, mouseY;

    // UI Elements
    private Rectangle btnStartGame = new Rectangle(gm.MAP_WIDTH / 2 - 100, gm.MAP_HEIGHT / 2 + 50, 200, 60);
    private Rectangle btnRetry = new Rectangle(gm.MAP_WIDTH / 2 - 100, gm.MAP_HEIGHT / 2 + 50, 200, 60);
    private Rectangle btnStartWave = new Rectangle(gm.MAP_WIDTH - 220, gm.MAP_HEIGHT + 30, 200, 60);
    private Rectangle[] shopButtons = new Rectangle[4]; // 4 wieże teraz

    // Mapa Komend (COMMAND PATTERN)
    private Map<Rectangle, IGameCommand> commands = new HashMap<>();

    public GamePanel() {
        this.setPreferredSize(new Dimension(gm.MAP_WIDTH, gm.MAP_HEIGHT + gm.UI_HEIGHT));
        this.setFocusable(true);
        gm.addObserver(this);

        // Setup Shop Buttons (4 wieże)
        for (int i = 0; i < 4; i++) {
            shopButtons[i] = new Rectangle(20 + (i * 120), gm.MAP_HEIGHT + 20, 100, 80);
        }

        // Konfiguracja Komend (COMMAND PATTERN) z użyciem Factory Manager
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
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e);
            }
        };
        this.addMouseListener(ma);
        this.addMouseMotionListener(ma);
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

    private void handleClick(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

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

                // Prawy Przycisk Myszy -> ULEPSZENIE (DECORATOR PATTERN)
                if (SwingUtilities.isRightMouseButton(e) && gm.occupiedMap[c][r]) {
                    for (int i = 0; i < gm.towers.size(); i++) {
                        ITower t = gm.towers.get(i);
                        if (Math.abs(t.getX() - (c * gm.TILE_SIZE + gm.TILE_SIZE / 2)) < 20 &&
                                Math.abs(t.getY() - (r * gm.TILE_SIZE + gm.TILE_SIZE / 2)) < 20) {

                            if (!(t instanceof UpgradeDecorator) && gm.money >= 100) {
                                gm.spendMoney(100);
                                gm.towers.set(i, new UpgradeDecorator(t));
                                gm.towerUpgraded(100); // Powiadomienie
                                System.out.println("Wieża ulepszona (Decorator Pattern)!");
                            }
                        }
                    }
                }
                // Lewy Przycisk Myszy -> BUDOWANIE (FACTORY METHOD PATTERN)
                else if (SwingUtilities.isLeftMouseButton(e) && !gm.occupiedMap[c][r]) {
                    if (gm.money >= selectedTowerCost) {
                        gm.spendMoney(selectedTowerCost);
                        int tx = c * gm.TILE_SIZE + gm.TILE_SIZE / 2;
                        int ty = r * gm.TILE_SIZE + gm.TILE_SIZE / 2;

                        // UŻYCIE FACTORY METHOD PATTERN
                        ITower t = factoryManager.createTower(selectedTowerType, tx, ty);
                        gm.towers.add(t);
                        gm.occupiedMap[c][r] = true;
                        gm.towerBuilt(selectedTowerCost); // Powiadomienie
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
        if (gm.state == GameState.WAVE_IN_PROGRESS) {
            waveManager.update();
        }
        if (gm.state == GameState.PREP_PHASE || gm.state == GameState.WAVE_IN_PROGRESS) {
            for (Enemy e : gm.enemies) {
                e.update();
                if (e.finished) {
                    gm.takeDamage();
                    gm.enemies.remove(e);
                } else if (!e.alive) {
                    gm.addMoney(e.reward);
                    gm.enemyKilled(e.reward); // Powiadomienie
                    gm.enemies.remove(e);
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
        if (gm.state == GameState.MENU) {
            drawMenu(g2);
        } else if (gm.state == GameState.GAME_OVER) {
            drawGameOver(g2);
        } else {
            drawGame(g2);
            drawUI(g2);
        }
    }

    private void drawMenu(Graphics2D g) {
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("TOWER DEFENSE", getWidth() / 2 - 200, getHeight() / 2 - 50);
        g.setColor(new Color(34, 139, 34));
        g.fill(btnStartGame);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("START", (int) btnStartGame.getX() + 60, (int) btnStartGame.getY() + 38);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.drawString("GAME OVER", getWidth() / 2 - 180, getHeight() / 2 - 50);
        g.setColor(Color.ORANGE);
        g.fill(btnRetry);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.drawString("RETRY", (int) btnRetry.getX() + 60, (int) btnRetry.getY() + 38);
    }

    private void drawGame(Graphics2D g) {
        g.setColor(new Color(60, 179, 113));
        g.fillRect(0, 0, gm.MAP_WIDTH, gm.MAP_HEIGHT);
        g.setColor(new Color(210, 180, 140));
        g.setStroke(new BasicStroke(gm.TILE_SIZE - 10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Point[] p = gm.pathPoints;
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
        if (mouseY < gm.MAP_HEIGHT && gm.state != GameState.GAME_OVER) {
            int c = mouseX / gm.TILE_SIZE;
            int r = mouseY / gm.TILE_SIZE;
            int x = c * gm.TILE_SIZE;
            int y = r * gm.TILE_SIZE;
            boolean can = (c >= 0 && c < gm.COLS && r >= 0 && r < gm.ROWS && !gm.occupiedMap[c][r]);
            g.setColor(can ? new Color(0, 255, 0, 100) : new Color(255, 0, 0, 100));
            g.fillRect(x, y, gm.TILE_SIZE, gm.TILE_SIZE);
            g.setColor(Color.WHITE);
            g.drawOval(
                    x + gm.TILE_SIZE / 2 - selectedTowerRange,
                    y + gm.TILE_SIZE / 2 - selectedTowerRange,
                    selectedTowerRange * 2,
                    selectedTowerRange * 2
            );
        }
    }

    private void drawUI(Graphics2D g) {
        g.setColor(new Color(45, 45, 45));
        g.fillRect(0, gm.MAP_HEIGHT, getWidth(), gm.UI_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("MONEY: " + gm.money, 500, gm.MAP_HEIGHT + 30);
        g.drawString("LIVES: " + gm.lives, 500, gm.MAP_HEIGHT + 60);
        g.drawString("WAVE: " + gm.wave, 500, gm.MAP_HEIGHT + 90);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("(RMB = Upgrade 100$)", 650, gm.MAP_HEIGHT + 30);

        String[] names = {"ARCHER", "CANNON", "SNIPER", "LASER"};
        int[] costs = {50, 120, 250, 80};
        for (int i = 0; i < 4; i++) {
            boolean sel = selectedTowerType.equals(names[i]);
            g.setColor(sel ? Color.YELLOW : Color.GRAY);
            g.draw(shopButtons[i]);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            g.drawString(names[i], shopButtons[i].x + 5, shopButtons[i].y + 40);
            g.drawString(costs[i] + "$", shopButtons[i].x + 5, shopButtons[i].y + 60);
        }

        g.setColor(gm.state == GameState.PREP_PHASE ? new Color(34, 139, 34) : Color.GRAY);
        g.fill(btnStartWave);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("START WAVE", btnStartWave.x + 40, btnStartWave.y + 35);
    }
}