import java.awt.Color;
import java.awt.Graphics2D;

class Tower implements ITower {
    public int x, y, range, damage, cooldown;
    public String name;
    public Color color;
    private long lastShotTime = 0;

    public Tower(int x, int y, String name, int range, int cooldown, int damage, Color color) {
        this.x = x; this.y = y; this.name = name;
        this.range = range; this.cooldown = cooldown; this.damage = damage; this.color = color;
    }

    @Override
    public int getDamage() { return damage; }
    @Override
    public int getRange() { return range; }
    @Override
    public int getX() { return x; }
    @Override
    public int getY() { return y; }

    @Override
    public void update() {
        if (System.currentTimeMillis() - lastShotTime < cooldown) return;
        Enemy target = null;
        double minDst = Double.MAX_VALUE;
        for (Enemy e : GameManager.getInstance().enemies) {
            double dst = (e.x - x)*(e.x - x) + (e.y - y)*(e.y - y);
            if (dst < range * range && dst < minDst && e.alive) { minDst = dst; target = e; }
        }
        if (target != null) {
            GameManager.getInstance().projectiles.add(new Projectile(x, y, target, getDamage()));
            lastShotTime = System.currentTimeMillis();
        }
    }

    @Override
    public void draw(Graphics2D g) {
        int ts = GameManager.getInstance().TILE_SIZE;
        g.setColor(Color.DARK_GRAY); g.fillRect(x - ts/2 + 4, y - ts/2 + 4, ts - 8, ts - 8);
        g.setColor(color); g.fillOval(x - 10, y - 10, 20, 20);
    }
}