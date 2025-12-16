import java.awt.Color;

class TowerFactory {
    public static ITower createTower(String type, int x, int y) {
        switch (type) {
            case "ARCHER": return new Tower(x, y, "Łucznik", 120, 600, 15, new Color(30, 144, 255));
            case "CANNON": return new Tower(x, y, "Armata", 150, 1500, 60, new Color(50, 50, 50));
            case "SNIPER": return new Tower(x, y, "Snajper", 300, 2500, 150, new Color(148, 0, 211));
            default: throw new IllegalArgumentException("Nieznany typ: " + type);
        }
    }
}