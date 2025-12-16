import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

class UpgradeDecorator extends TowerDecorator {
    public UpgradeDecorator(ITower tower) { super(tower); }

    @Override
    public int getDamage() {
        return wrappedTower.getDamage() + 25;
    }

    @Override
    public void draw(Graphics2D g) {
        wrappedTower.draw(g);
        g.setColor(new Color(255, 215, 0));
        g.setStroke(new BasicStroke(2));
        g.drawRect(getX() - 15, getY() - 15, 30, 30);
        g.setStroke(new BasicStroke(1));
    }
}