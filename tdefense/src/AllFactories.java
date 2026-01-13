import java.awt.Color;
import java.util.HashMap;
import java.util.Map;


abstract class TowerFactoryBase {

    // Factory
    public abstract ITower createTower(int x, int y);

    // wzor
    public ITower createTowerWithValidation(int x, int y, GameManager gm) {
     //walidacja
        int col = x / gm.TILE_SIZE;
        int row = y / gm.TILE_SIZE;

        if (col < 0 || col >= gm.COLS || row < 0 || row >= gm.ROWS) {
            throw new IllegalArgumentException("Pozycja poza mapą!");
        }

        if (gm.occupiedMap[col][row]) {
            throw new IllegalArgumentException("Pole już zajęte!");
        }


        return createTower(x, y);
    }


    protected abstract String getTowerName();
    protected abstract int getBaseDamage();
    protected abstract int getBaseRange();
    protected abstract int getCooldown();
    protected abstract Color getTowerColor();
}

// lucznik
class ArcherFactory extends TowerFactoryBase {
    @Override
    public ITower createTower(int x, int y) {
        return new Tower(x, y, getTowerName(), getBaseRange(), getCooldown(), getBaseDamage(), getTowerColor());
    }

    @Override
    protected String getTowerName() { return "Łucznik"; }

    @Override
    protected int getBaseDamage() { return 25; }

    @Override
    protected int getBaseRange() { return 120; }

    @Override
    protected int getCooldown() { return 600; }

    @Override
    protected Color getTowerColor() { return new Color(30, 144, 255); }
}

//armata
class CannonFactory extends TowerFactoryBase {
    @Override
    public ITower createTower(int x, int y) {
        return new Tower(x, y, getTowerName(), getBaseRange(), getCooldown(), getBaseDamage(), getTowerColor());
    }

    @Override
    protected String getTowerName() { return "Armata"; }

    @Override
    protected int getBaseDamage() { return 60; }

    @Override
    protected int getBaseRange() { return 150; }

    @Override
    protected int getCooldown() { return 1500; }

    @Override
    protected Color getTowerColor() { return new Color(50, 50, 50); }
}

//snajper
class SniperFactory extends TowerFactoryBase {
    @Override
    public ITower createTower(int x, int y) {
        return new Tower(x, y, getTowerName(), getBaseRange(), getCooldown(), getBaseDamage(), getTowerColor());
    }

    @Override
    protected String getTowerName() { return "Snajper"; }

    @Override
    protected int getBaseDamage() { return 150; }

    @Override
    protected int getBaseRange() { return 300; }

    @Override
    protected int getCooldown() { return 2500; }

    @Override
    protected Color getTowerColor() { return new Color(148, 0, 211); }
}

// laser
class LaserFactory extends TowerFactoryBase {
    @Override
    public ITower createTower(int x, int y) {
        return new Tower(x, y, getTowerName(), getBaseRange(), getCooldown(), getBaseDamage(), getTowerColor());
    }

    @Override
    protected String getTowerName() { return "Laser"; }

    @Override
    protected int getBaseDamage() { return 15; }

    @Override
    protected int getBaseRange() { return 100; }

    @Override
    protected int getCooldown() { return 300; }

    @Override
    protected Color getTowerColor() { return new Color(0, 255, 255); }
}

//zarzadanie
class TowerFactoryManager {
    private static TowerFactoryManager instance;
    private Map<String, TowerFactoryBase> factories = new HashMap<>();
    private Map<String, Integer> costs = new HashMap<>();

    private TowerFactoryManager() {
        registerDefaultFactories();
    }

    public static TowerFactoryManager getInstance() {
        if (instance == null) {
            instance = new TowerFactoryManager();
        }
        return instance;
    }

    private void registerDefaultFactories() {
        registerFactory("ARCHER", new ArcherFactory(), 50);
        registerFactory("CANNON", new CannonFactory(), 120);
        registerFactory("SNIPER", new SniperFactory(), 250);
        registerFactory("LASER", new LaserFactory(), 80);
    }

    public void registerFactory(String type, TowerFactoryBase factory, int cost) {
        factories.put(type, factory);
        costs.put(type, cost);
    }

    public ITower createTower(String type, int x, int y) {
        TowerFactoryBase factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("Nieznany typ wieży: " + type);
        }
        return factory.createTower(x, y);
    }

    public ITower createTowerWithValidation(String type, int x, int y, GameManager gm) {
        TowerFactoryBase factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("Nieznany typ wieży: " + type);
        }
        return factory.createTowerWithValidation(x, y, gm);
    }

    public int getTowerCost(String type) {
        return costs.getOrDefault(type, 0);
    }

    public int getTowerRange(String type) {
        TowerFactoryBase factory = factories.get(type);
        if (factory == null) return 0;
        return factory.getBaseRange();
    }

    public Map<String, TowerFactoryBase> getAllFactories() {
        return new HashMap<>(factories);
    }
}