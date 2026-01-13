import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

class EnemyCache {
    private static Map<String, Enemy> cache = new HashMap<>();

    public static void loadCache() {
        //PIERWSZA MAPA
        cache.put("NORMAL", new Enemy("NORMAL", 80, 2.0, 10, new Color(220, 20, 60), 12));
        cache.put("FAST", new Enemy("FAST", 50, 3.5, 8, new Color(50, 205, 50), 10));
        cache.put("TANK", new Enemy("TANK", 350, 0.8, 25, new Color(139, 0, 0), 18));

        //ZIMA
        cache.put("ICE", new Enemy("ICE", 120, 1.8, 15, new Color(135, 206, 250), 13));
        cache.put("FROST_GIANT", new Enemy("FROST_GIANT", 500, 0.7, 35, new Color(70, 130, 180), 20));
        cache.put("BLIZZARD", new Enemy("BLIZZARD", 60, 4.2, 12, new Color(176, 224, 230), 11));
    }

    public static Enemy getEnemy(String type) {
        Enemy prototype = cache.get(type);
        if (prototype == null) {
            prototype = cache.get("NORMAL");
        }
        return (Enemy) prototype.clone();
    }
}