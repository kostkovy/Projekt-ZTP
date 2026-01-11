import javax.swing.*;
import java.awt.KeyboardFocusManager;
public class Main extends JFrame {
    // Referencje do obserwatorów, żeby można było z nimi pracować
    private StatisticsObserver statsObserver;
    private SoundObserver soundObserver;
    private AchievementObserver achievementObserver;
    private LoggerObserver loggerObserver;

    public Main() {
        EnemyCache.loadCache();

        // Inicjalizacja GameManager
        GameManager gm = GameManager.getInstance();

        // REJESTRACJA WSZYSTKICH OBSERWATORÓW
        statsObserver = new StatisticsObserver();
        soundObserver = new SoundObserver();
        achievementObserver = new AchievementObserver();
        loggerObserver = new LoggerObserver();

        gm.addObserver(statsObserver);
        gm.addObserver(soundObserver);
        gm.addObserver(achievementObserver);
        gm.addObserver(loggerObserver);

        System.out.println("=== TOWER DEFENSE - Wzorce projektowe ===");
        System.out.println("1. FACTORY METHOD - Abstrakcyjne fabryki wież");
        System.out.println("2. OBSERVER - 5 obserwatorów (Panel, Stats, Sound, Achievement, Logger)");
        System.out.println("3. DECORATOR - Ulepszanie wież");
        System.out.println("4. COMMAND - Komendy UI");
        System.out.println("5. PROTOTYPE - Klonowanie wrogów");
        System.out.println("6. SINGLETON - GameManager");
        System.out.println("==========================================\n");

        this.setTitle("TowerDefense - Design Patterns Demo");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        GamePanel panel = new GamePanel();
        gm.addObserver(panel); // Panel też jest obserwatorem

        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        new Thread(panel).start();

        // Opcjonalnie: można dodać menu do wyświetlania statystyk
        setupKeyBindings();
    }

    private void setupKeyBindings() {
        // Klawisz S - pokaż statystyki
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == java.awt.event.KeyEvent.KEY_PRESSED) {
                switch (e.getKeyCode()) {
                    case java.awt.event.KeyEvent.VK_S:
                        statsObserver.printStatistics();
                        break;
                    case java.awt.event.KeyEvent.VK_L:
                        loggerObserver.printLog();
                        break;
                    case java.awt.event.KeyEvent.VK_M:
                        soundObserver.setSoundEnabled(!soundObserver.isSoundEnabled());
                        break;
                    case java.awt.event.KeyEvent.VK_A:
                        System.out.println("🏆 Osiągnięcia odblokowane: " +
                                achievementObserver.getAchievementCount() + " - " +
                                achievementObserver.getUnlockedAchievements());
                        break;
                }
            }
            return false;
        });

        System.out.println("Skróty klawiszowe:");
        System.out.println("  S - Pokaż statystyki");
        System.out.println("  L - Pokaż logi");
        System.out.println("  M - Włącz/Wyłącz dźwięk");
        System.out.println("  A - Pokaż osiągnięcia\n");
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings","on");
        SwingUtilities.invokeLater(Main::new);
    }
}