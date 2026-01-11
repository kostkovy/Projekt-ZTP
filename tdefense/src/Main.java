import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Main extends JFrame {
    private StatisticsObserver statsObserver;
    private SoundObserver soundObserver;
    private AchievementObserver achievementObserver;
    private LoggerObserver loggerObserver;
    private GamePanel panel;
    private GraphicsDevice device;

    public Main() {
        EnemyCache.loadCache();

        GameManager gm = GameManager.getInstance();

        statsObserver = new StatisticsObserver();
        soundObserver = new SoundObserver();
        achievementObserver = new AchievementObserverWithUI();
        loggerObserver = new LoggerObserver();

        gm.addObserver(statsObserver);
        gm.addObserver(soundObserver);
        gm.addObserver(achievementObserver);
        gm.addObserver(loggerObserver);

        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        this.setTitle("Tower Defense");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setUndecorated(true);
        this.setResizable(false);

        panel = new GamePanel();
        panel.setObservers(statsObserver, loggerObserver, achievementObserver);
        gm.addObserver(panel);

        this.add(panel);

        if (device.isFullScreenSupported()) {
            device.setFullScreenWindow(this);
        } else {
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        this.setVisible(true);
        new Thread(panel).start();

        setupKeyBindings();

        System.out.println("\n╔═══════════════════════════════════════╗");
        System.out.println("║    🎮 TOWER DEFENSE - STEROWANIE 🎮    ║");
        System.out.println("╠═══════════════════════════════════════╣");
        System.out.println("║ ESC - Wyjdź z gry                      ║");
        System.out.println("║ S   - Pokaż statystyki (GRAFICZNIE)   ║");
        System.out.println("║ L   - Pokaż logi (GRAFICZNIE)         ║");
        System.out.println("║ M   - Włącz/Wyłącz dźwięk             ║");
        System.out.println("║ A   - Pokaż osiągnięcia (GRAFICZNIE)  ║");
        System.out.println("║                                        ║");
        System.out.println("║ 🏆 NOWOŚĆ: Fala 11 = ZIMOWA MAPA!     ║");
        System.out.println("╚═══════════════════════════════════════╝\n");
    }

    private void setupKeyBindings() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        exitFullscreen();
                        System.exit(0);
                        break;
                    case KeyEvent.VK_S:
                        panel.toggleStatistics();
                        break;
                    case KeyEvent.VK_L:
                        panel.toggleLogs();
                        break;
                    case KeyEvent.VK_M:
                        soundObserver.setSoundEnabled(!soundObserver.isSoundEnabled());
                        break;
                    case KeyEvent.VK_A:
                        panel.toggleAchievements();
                        break;
                }
            }
            return false;
        });
    }

    private void exitFullscreen() {
        if (device.isFullScreenSupported() && device.getFullScreenWindow() == this) {
            device.setFullScreenWindow(null);
        }
    }

    private class AchievementObserverWithUI extends AchievementObserver {
        @Override
        public void onGameEvent(GameEvent event) {
            super.onGameEvent(event);

            switch (event.type) {
                case ENEMY_KILLED:
                    int enemyCount = getEnemiesKilledCount();
                    if (enemyCount == 10) {
                        panel.showAchievement("First Blood", "Zabij 10 wrogów");
                    } else if (enemyCount == 50) {
                        panel.showAchievement("Slayer", "Zabij 50 wrogów");
                    } else if (enemyCount == 100) {
                        panel.showAchievement("Massacre", "Zabij 100 wrogów");
                    }
                    break;

                case TOWER_BUILT:
                    int towerCount = getTowersBuiltCount();
                    if (towerCount == 5) {
                        panel.showAchievement("Builder", "Zbuduj 5 wież");
                    } else if (towerCount == 15) {
                        panel.showAchievement("Architect", "Zbuduj 15 wież");
                    }
                    break;

                case WAVE_COMPLETED:
                    if (event.data instanceof Integer) {
                        int wave = (Integer) event.data;
                        if (wave == 5) {
                            panel.showAchievement("Survivor", "Przetrwaj 5 fal");
                        } else if (wave == 10) {
                            panel.showAchievement("Veteran", "Przetrwaj 10 fal");
                            panel.showAchievement("Winter Awaits", "Zimowa mapa w następnej fali!");
                        } else if (wave == 20) {
                            panel.showAchievement("Legend", "Przetrwaj 20 fal");
                        }
                    }
                    break;

                case WAVE_STARTED:
                    if (event.data instanceof Integer) {
                        int wave = (Integer) event.data;
                        if (wave == 11 && unlock("winter_warrior")) {
                            panel.showAchievement("Winter Warrior", "Osiągnij falę zimową!");
                        }
                    }
                    break;
            }
        }
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fallback to default
            }
            new Main();
        });
    }
}