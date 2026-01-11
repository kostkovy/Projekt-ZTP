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
    private boolean isFullscreen = true;

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

        // Get graphics device for fullscreen
        device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        this.setTitle("Tower Defense");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setUndecorated(true);
        this.setResizable(false);

        panel = new GamePanel();
        gm.addObserver(panel);

        this.add(panel);

        // Set fullscreen mode
        if (device.isFullScreenSupported()) {
            device.setFullScreenWindow(this);
        } else {
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        this.setVisible(true);
        new Thread(panel).start();

        setupKeyBindings();

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║    🎮 TOWER DEFENSE - STEROWANIE 🎮    ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ ESC - Wyjdź z gry                      ║");
        System.out.println("║ S   - Pokaż statystyki                 ║");
        System.out.println("║ L   - Pokaż logi                       ║");
        System.out.println("║ M   - Włącz/Wyłącz dźwięk             ║");
        System.out.println("║ A   - Pokaż osiągnięcia                ║");
        System.out.println("╚════════════════════════════════════════╝\n");
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
                        statsObserver.printStatistics();
                        break;
                    case KeyEvent.VK_L:
                        loggerObserver.printLog();
                        break;
                    case KeyEvent.VK_M:
                        soundObserver.setSoundEnabled(!soundObserver.isSoundEnabled());
                        break;
                    case KeyEvent.VK_A:
                        System.out.println("\n🏆 ════════════════════════════════════");
                        System.out.println("   OSIĄGNIĘCIA ODBLOKOWANE: " +
                                achievementObserver.getAchievementCount());
                        System.out.println("   " + achievementObserver.getUnlockedAchievements());
                        System.out.println("🏆 ════════════════════════════════════\n");
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

    // Extended AchievementObserver that shows on-screen notifications
    private class AchievementObserverWithUI extends AchievementObserver {
        @Override
        public void onGameEvent(GameEvent event) {
            super.onGameEvent(event);

            // Check for achievement unlocks and notify UI
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
                        } else if (wave == 20) {
                            panel.showAchievement("Legend", "Przetrwaj 20 fal");
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