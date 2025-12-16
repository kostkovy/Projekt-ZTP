import java.awt.Graphics2D;

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