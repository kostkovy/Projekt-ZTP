package towerdefense.patterns.prototype;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class EnemyCache {
    private static Map<String, Enemy> cache = new HashMap<>();
    
    public static void loadCache() {
        cache.put("NORMAL", new Enemy("NORMAL", 80, 2.0, 10, new Color(220, 20, 60), 12));
        cache.put("FAST", new Enemy("FAST", 50, 3.5, 8, new Color(50, 205, 50), 10));
        cache.put("TANK", new Enemy("TANK", 350, 0.8, 25, new Color(139, 0, 0), 18));
    }
    
    public static Enemy getEnemy(String type) { 
        return (Enemy) cache.get(type).clone(); 
    }
}