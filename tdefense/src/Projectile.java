import java.awt.Color;
import java.awt.Graphics2D;

class Projectile {
    public double x, y;
    private Enemy target;
    private int damage;
    public boolean active = true;
    public Projectile(double x, double y, Enemy target, int damage) {
        this.x = x; this.y = y; this.target = target; this.damage = damage;
    }
    public void update() {
        if (!target.alive || target.finished) { active = false; return; }
        double dx = target.x - x, dy = target.y - y;
        double dist = Math.sqrt(dx*dx + dy*dy);
        if (dist < 10) { target.hp -= damage; if (target.hp <= 0) target.alive = false; active = false; }
        else { x += (dx/dist)*10; y += (dy/dist)*10; }
    }
    public void draw(Graphics2D g) { g.setColor(Color.YELLOW); g.fillOval((int)x - 3, (int)y - 3, 6, 6); }
}