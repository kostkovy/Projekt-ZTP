import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

enum GameState { MENU, PREP_PHASE, WAVE_IN_PROGRESS, GAME_OVER }

class GameManager {
    private static GameManager instance;
    public final int TILE_SIZE = 48, COLS = 20, ROWS = 12;
    public final int MAP_WIDTH = COLS * TILE_SIZE, MAP_HEIGHT = ROWS * TILE_SIZE;
    public final int UI_HEIGHT = 120;

    public GameState state = GameState.MENU;
    public int money = 10000000, lives = 100, wave = 1;

    public boolean[][] occupiedMap;
    public List<Enemy> enemies = new CopyOnWriteArrayList<>();
    public List<ITower> towers = new CopyOnWriteArrayList<>();
    public List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    public Point[] pathPoints;

    private List<GameObserver> observers = new ArrayList<>();

    private GameManager() {
        occupiedMap = new boolean[COLS][ROWS];
        initPath();
        markPathAsOccupied();
    }

    public static GameManager getInstance() {
        if (instance == null) instance = new GameManager();
        return instance;
    }

    public void addObserver(GameObserver o) { observers.add(o); }

    // NOWA METODA: Powiadamianie o konkretnych zdarzeniach
    public void notifyObservers(GameEvent event) {
        for(GameObserver o : observers) {
            o.onGameEvent(event);
        }
    }

    // Stara metoda dla kompatybilności wstecznej
    public void notifyObservers() {
        for(GameObserver o : observers) {
            o.onGameUpdate();
        }
    }

    public void addMoney(int amount) {
        this.money += amount;
        notifyObservers();
        notifyObservers(new GameEvent(GameEventType.MONEY_CHANGED, amount));
    }

    public void spendMoney(int amount) {
        this.money -= amount;
        notifyObservers();
        notifyObservers(new GameEvent(GameEventType.MONEY_CHANGED, -amount));
    }

    public void takeDamage() {
        this.lives--;
        notifyObservers(new GameEvent(GameEventType.LIVES_CHANGED, lives));
        notifyObservers();

        if(lives <= 0) {
            state = GameState.GAME_OVER;
            notifyObservers(new GameEvent(GameEventType.GAME_OVER));
        }
    }

    public void enemyKilled(int reward) {
        notifyObservers(new GameEvent(GameEventType.ENEMY_KILLED, reward));
    }

    public void towerBuilt(int cost) {
        notifyObservers(new GameEvent(GameEventType.TOWER_BUILT, cost));
    }

    public void towerUpgraded(int cost) {
        notifyObservers(new GameEvent(GameEventType.TOWER_UPGRADED, cost));
    }

    public void waveStarted(int waveNumber) {
        notifyObservers(new GameEvent(GameEventType.WAVE_STARTED, waveNumber));
    }

    public void waveCompleted(int waveNumber) {
        notifyObservers(new GameEvent(GameEventType.WAVE_COMPLETED, waveNumber));
    }

    private void initPath() {
        pathPoints = new Point[] {
                new Point(0, 2 * TILE_SIZE + TILE_SIZE/2),
                new Point(5 * TILE_SIZE + TILE_SIZE/2, 2 * TILE_SIZE + TILE_SIZE/2),
                new Point(5 * TILE_SIZE + TILE_SIZE/2, 8 * TILE_SIZE + TILE_SIZE/2),
                new Point(14 * TILE_SIZE + TILE_SIZE/2, 8 * TILE_SIZE + TILE_SIZE/2),
                new Point(14 * TILE_SIZE + TILE_SIZE/2, 4 * TILE_SIZE + TILE_SIZE/2),
                new Point(MAP_WIDTH, 4 * TILE_SIZE + TILE_SIZE/2)
        };
    }

    private void markPathAsOccupied() {
        for (int i = 0; i < pathPoints.length - 1; i++) {
            Line2D line = new Line2D.Float(pathPoints[i], pathPoints[i+1]);
            for (int c = 0; c < COLS; c++) {
                for (int r = 0; r < ROWS; r++) {
                    Rectangle rect = new Rectangle(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    if (line.intersects(rect)) occupiedMap[c][r] = true;
                }
            }
        }
    }

    public void resetGame() {
        money = 10000000; lives = 100; wave = 1; state = GameState.PREP_PHASE;
        enemies.clear(); towers.clear(); projectiles.clear();
        occupiedMap = new boolean[COLS][ROWS];
        markPathAsOccupied();
        notifyObservers();
        notifyObservers(new GameEvent(GameEventType.GAME_RESET));
    }
}