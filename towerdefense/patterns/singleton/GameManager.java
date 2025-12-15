package towerdefense;

import towerdefense.patterns.decorator.ITower;
import towerdefense.patterns.observer.GameSubject;
import towerdefense.game.GameState;
import towerdefense.patterns.prototype.Enemy;
import towerdefense.game.Projectile;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameManager extends GameSubject {
    private static GameManager instance;
    
    public final int TILE_SIZE = 48, COLS = 20, ROWS = 12;
    public final int MAP_WIDTH = COLS * TILE_SIZE, MAP_HEIGHT = ROWS * TILE_SIZE;
    public final int UI_HEIGHT = 120;

    public GameState state = GameState.MENU;
    public int money = 120, lives = 10, wave = 1;

    public boolean[][] occupiedMap;
    public List<Enemy> enemies = new CopyOnWriteArrayList<>();
    public List<ITower> towers = new CopyOnWriteArrayList<>();
    public List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    public Point[] pathPoints;

    private GameManager() {
        occupiedMap = new boolean[COLS][ROWS];
        initPath();
        markPathAsOccupied();
    }

    public static GameManager getInstance() {
        if (instance == null) instance = new GameManager();
        return instance;
    }

    public void addMoney(int amount) { 
        this.money += amount; 
        notifyObservers(); 
    }
    
    public void spendMoney(int amount) { 
        this.money -= amount; 
        notifyObservers(); 
    }
    
    public void takeDamage() { 
        this.lives--; 
        notifyObservers(); 
        if(lives <= 0) state = GameState.GAME_OVER; 
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
        money = 120; 
        lives = 10; 
        wave = 1; 
        state = GameState.PREP_PHASE;
        enemies.clear(); 
        towers.clear(); 
        projectiles.clear();
        occupiedMap = new boolean[COLS][ROWS];
        markPathAsOccupied();
        notifyObservers();
    }
}