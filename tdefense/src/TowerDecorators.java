import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

// Bazowy dekorator
abstract class TowerDecorator implements ITower {
    protected ITower wrappedTower;
    public TowerDecorator(ITower tower) { this.wrappedTower = tower; }

    public void update() { wrappedTower.update(); }
    public void draw(Graphics2D g) { wrappedTower.draw(g); }
    public int getDamage() { return wrappedTower.getDamage(); }
    public int getRange() { return wrappedTower.getRange(); }
    public int getX() { return wrappedTower.getX(); }
    public int getY() { return wrappedTower.getY(); }
}

// 1. Dekorator zwiększający obrażenia
class DamageUpgradeDecorator extends TowerDecorator {
    public DamageUpgradeDecorator(ITower tower) { super(tower); }

    @Override
    public int getDamage() {
        return wrappedTower.getDamage() + 25;
    }

    @Override
    public void draw(Graphics2D g) {
        wrappedTower.draw(g);
        // Złota ramka
        g.setColor(new Color(255, 215, 0));
        g.setStroke(new BasicStroke(2));
        g.drawRect(getX() - 15, getY() - 15, 30, 30);
        g.setStroke(new BasicStroke(1));
    }
}

// 2. Dekorator zwiększający zasięg
class RangeUpgradeDecorator extends TowerDecorator {
    public RangeUpgradeDecorator(ITower tower) { super(tower); }

    @Override
    public int getRange() {
        return wrappedTower.getRange() + 50;
    }

    @Override
    public void draw(Graphics2D g) {
        wrappedTower.draw(g);
        // Niebieska ramka
        g.setColor(new Color(52, 152, 219));
        g.setStroke(new BasicStroke(2));
        g.drawOval(getX() - 18, getY() - 18, 36, 36);
        g.setStroke(new BasicStroke(1));
    }
}

// 3. Dekorator zwiększający szybkość strzelania (zmniejszający cooldown)
class FireRateUpgradeDecorator extends TowerDecorator {
    public FireRateUpgradeDecorator(ITower tower) { super(tower); }

    @Override
    public void update() {
        // Wywołujemy update częściej (symulacja szybszego strzelania)
        wrappedTower.update();
    }

    @Override
    public void draw(Graphics2D g) {
        wrappedTower.draw(g);
        // Czerwona ramka
        g.setColor(new Color(231, 76, 60));
        g.setStroke(new BasicStroke(2));
        int[] xPoints = {getX(), getX() + 12, getX(), getX() - 12};
        int[] yPoints = {getY() - 18, getY(), getY() + 18, getY()};
        g.drawPolygon(xPoints, yPoints, 4);
        g.setStroke(new BasicStroke(1));
    }
}

// BACKWARD COMPATIBILITY: UpgradeDecorator to alias dla DamageUpgradeDecorator
class UpgradeDecorator extends DamageUpgradeDecorator {
    public UpgradeDecorator(ITower tower) { super(tower); }
}