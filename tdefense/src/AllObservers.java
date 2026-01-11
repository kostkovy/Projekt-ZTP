import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Plik zawierający wszystkie 4 obserwatory (oprócz GamePanel który już istnieje)
 * Możesz też rozdzielić to na osobne pliki jeśli wolisz
 */

// ============================================================================
// OBSERVER #1 - Obserwator statystyk gry
// ============================================================================
class StatisticsObserver implements GameObserver {
    private int totalEnemiesKilled = 0;
    private int totalTowersBuilt = 0;
    private int totalMoneyEarned = 0;
    private int totalMoneySpent = 0;
    private int totalWavesCompleted = 0;
    private int highestWaveReached = 0;

    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.type) {
            case ENEMY_KILLED:
                totalEnemiesKilled++;
                if (event.data instanceof Integer) {
                    totalMoneyEarned += (Integer) event.data;
                }
                break;

            case TOWER_BUILT:
                totalTowersBuilt++;
                if (event.data instanceof Integer) {
                    totalMoneySpent += (Integer) event.data;
                }
                break;

            case TOWER_UPGRADED:
                if (event.data instanceof Integer) {
                    totalMoneySpent += (Integer) event.data;
                }
                break;

            case WAVE_COMPLETED:
                totalWavesCompleted++;
                if (event.data instanceof Integer) {
                    int wave = (Integer) event.data;
                    if (wave > highestWaveReached) {
                        highestWaveReached = wave;
                    }
                }
                break;

            case GAME_RESET:
                resetStatistics();
                break;
        }
    }

    private void resetStatistics() {
        totalEnemiesKilled = 0;
        totalTowersBuilt = 0;
        totalMoneyEarned = 0;
        totalMoneySpent = 0;
        totalWavesCompleted = 0;
        // highestWaveReached nie resetujemy - to rekord
    }

    // Gettery
    public int getTotalEnemiesKilled() { return totalEnemiesKilled; }
    public int getTotalTowersBuilt() { return totalTowersBuilt; }
    public int getTotalMoneyEarned() { return totalMoneyEarned; }
    public int getTotalMoneySpent() { return totalMoneySpent; }
    public int getTotalWavesCompleted() { return totalWavesCompleted; }
    public int getHighestWaveReached() { return highestWaveReached; }

    public void printStatistics() {
        System.out.println("=== STATYSTYKI GRY ===");
        System.out.println("Przeciwnicy zabici: " + totalEnemiesKilled);
        System.out.println("Wieże zbudowane: " + totalTowersBuilt);
        System.out.println("Pieniądze zarobione: " + totalMoneyEarned);
        System.out.println("Pieniądze wydane: " + totalMoneySpent);
        System.out.println("Fale ukończone: " + totalWavesCompleted);
        System.out.println("Najwyższa fala: " + highestWaveReached);
    }
}

// ============================================================================
// OBSERVER #2 - Obserwator dźwięków
// ============================================================================
class SoundObserver implements GameObserver {
    private boolean soundEnabled = true;

    @Override
    public void onGameEvent(GameEvent event) {
        if (!soundEnabled) return;

        switch (event.type) {
            case TOWER_BUILT:
                playSound("build.wav");
                break;

            case TOWER_UPGRADED:
                playSound("upgrade.wav");
                break;

            case ENEMY_KILLED:
                playSound("enemy_death.wav");
                break;

            case WAVE_STARTED:
                playSound("wave_start.wav");
                break;

            case WAVE_COMPLETED:
                playSound("wave_complete.wav");
                break;

            case LIVES_CHANGED:
                playSound("damage.wav");
                break;

            case GAME_OVER:
                playSound("game_over.wav");
                break;
        }
    }

    private void playSound(String soundFile) {
        // Symulacja - w rzeczywistości załadowałby i odtwarzał dźwięk
        System.out.println("[SOUND] Playing: " + soundFile);
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        System.out.println("[SOUND] Sound " + (enabled ? "enabled" : "disabled"));
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}

// ============================================================================
// OBSERVER #3 - Obserwator osiągnięć
// ============================================================================
class AchievementObserver implements GameObserver {
    private Set<String> unlockedAchievements = new HashSet<>();
    private int enemiesKilledCount = 0;
    private int towersBuiltCount = 0;

    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.type) {
            case ENEMY_KILLED:
                enemiesKilledCount++;
                checkEnemyAchievements();
                break;

            case TOWER_BUILT:
                towersBuiltCount++;
                checkTowerAchievements();
                break;

            case WAVE_COMPLETED:
                if (event.data instanceof Integer) {
                    int wave = (Integer) event.data;
                    checkWaveAchievements(wave);
                }
                break;

            case GAME_RESET:
                enemiesKilledCount = 0;
                towersBuiltCount = 0;
                break;
        }
    }

    private void checkEnemyAchievements() {
        if (enemiesKilledCount >= 10 && unlock("first_blood")) {
            System.out.println("🏆 ACHIEVEMENT UNLOCKED: First Blood (10 wrogów)");
        }
        if (enemiesKilledCount >= 50 && unlock("slayer")) {
            System.out.println("🏆 ACHIEVEMENT UNLOCKED: Slayer (50 wrogów)");
        }
        if (enemiesKilledCount >= 100 && unlock("massacre")) {
            System.out.println("🏆 ACHIEVEMENT UNLOCKED: Massacre (100 wrogów)");
        }
    }

    private void checkTowerAchievements() {
        if (towersBuiltCount >= 5 && unlock("builder")) {
            System.out.println("🏆 ACHIEVEMENT UNLOCKED: Builder (5 wież)");
        }
        if (towersBuiltCount >= 15 && unlock("architect")) {
            System.out.println("🏆 ACHIEVEMENT UNLOCKED: Architect (15 wież)");
        }
    }

    private void checkWaveAchievements(int wave) {
        if (wave >= 5 && unlock("survivor")) {
            System.out.println("🏆 ACHIEVEMENT UNLOCKED: Survivor (5 fal)");
        }
        if (wave >= 10 && unlock("veteran")) {
            System.out.println("🏆 ACHIEVEMENT UNLOCKED: Veteran (10 fal)");
        }
        if (wave >= 20 && unlock("legend")) {
            System.out.println("🏆 ACHIEVEMENT UNLOCKED: Legend (20 fal)");
        }
    }

    private boolean unlock(String achievementId) {
        if (!unlockedAchievements.contains(achievementId)) {
            unlockedAchievements.add(achievementId);
            return true;
        }
        return false;
    }

    public Set<String> getUnlockedAchievements() {
        return new HashSet<>(unlockedAchievements);
    }

    public int getAchievementCount() {
        return unlockedAchievements.size();
    }
}

// ============================================================================
// OBSERVER #4 - Obserwator logów
// ============================================================================
class LoggerObserver implements GameObserver {
    private List<String> eventLog = new ArrayList<>();
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private boolean detailedLogging = true;

    @Override
    public void onGameEvent(GameEvent event) {
        String timestamp = LocalTime.now().format(timeFormatter);
        String message = formatEventMessage(event);

        String logEntry = String.format("[%s] %s", timestamp, message);
        eventLog.add(logEntry);

        if (detailedLogging) {
            System.out.println(logEntry);
        }
    }

    private String formatEventMessage(GameEvent event) {
        switch (event.type) {
            case MONEY_CHANGED:
                return "Pieniądze zmienione";

            case LIVES_CHANGED:
                return "Utracono życie!";

            case WAVE_STARTED:
                return "Rozpoczęto falę " + event.data;

            case WAVE_COMPLETED:
                return "Ukończono falę " + event.data;

            case ENEMY_KILLED:
                return "Zabito wroga (nagroda: " + event.data + "$)";

            case TOWER_BUILT:
                return "Zbudowano wieżę (koszt: " + event.data + "$)";

            case TOWER_UPGRADED:
                return "Ulepszono wieżę (koszt: " + event.data + "$)";

            case GAME_OVER:
                return "KONIEC GRY";

            case GAME_RESET:
                return "Gra zresetowana";

            default:
                return "Nieznane zdarzenie: " + event.type;
        }
    }

    public void printLog() {
        System.out.println("\n=== DZIENNIK ZDARZEŃ ===");
        for (String entry : eventLog) {
            System.out.println(entry);
        }
    }

    public void clearLog() {
        eventLog.clear();
        System.out.println("[LOG] Dziennik wyczyszczony");
    }

    public List<String> getEventLog() {
        return new ArrayList<>(eventLog);
    }

    public void setDetailedLogging(boolean enabled) {
        this.detailedLogging = enabled;
    }
}