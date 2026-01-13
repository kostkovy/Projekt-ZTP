import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

class Enemy implements Prototype {
    public double x, y;
    public int hp, maxHp, reward, size;
    public double speed;
    public Color color;
    public boolean alive = true, finished = false;
    private int pathIndex = 0;

    public Enemy(String typeId, int hp, double speed, int reward, Color color, int size) {
        this.hp = hp; this.maxHp = hp; this.speed = speed;
        this.reward = reward; this.color = color; this.size = size;
        this.x = GameManager.getInstance().pathPoints[0].x;
        this.y = GameManager.getInstance().pathPoints[0].y;
    }

    @Override
    public Prototype clone() {
        try { return (Enemy) super.clone(); }
        catch (CloneNotSupportedException e) { return null; }
    }

    public void buffHealth(int amount) { this.maxHp += amount; this.hp = this.maxHp; }

    public void update() {
        if (!alive) return;
        Point[] path = GameManager.getInstance().pathPoints;
        if (pathIndex < path.length) {
            Point target = path[pathIndex];
            double dx = target.x - x, dy = target.y - y;
            double dist = Math.sqrt(dx*dx + dy*dy);
            if (dist < speed) pathIndex++;
            else { x += (dx/dist)*speed; y += (dy/dist)*speed; }
        } else finished = true;
    }

    public void draw(Graphics2D g) {
        GameManager gm = GameManager.getInstance();
        boolean isWinter = gm.wave >= 11;

        if (isWinter) {
            // mapa 2
            if (color.equals(new Color(135, 206, 250))) {
                int[] xPoints = {(int)x, (int)x + size, (int)x, (int)x - size};
                int[] yPoints = {(int)y - size, (int)y, (int)y + size, (int)y};
                g.setColor(color);
                g.fillPolygon(xPoints, yPoints, 4);
                g.setColor(Color.WHITE);
                g.drawPolygon(xPoints, yPoints, 4);
            } else if (color.equals(new Color(70, 130, 180))) {
                int[] xPoints = new int[6];
                int[] yPoints = new int[6];
                for (int i = 0; i < 6; i++) {
                    double angle = Math.PI / 3 * i;
                    xPoints[i] = (int)(x + size * Math.cos(angle));
                    yPoints[i] = (int)(y + size * Math.sin(angle));
                }
                g.setColor(color);
                g.fillPolygon(xPoints, yPoints, 6);
                g.setColor(new Color(200, 220, 255));
                g.drawPolygon(xPoints, yPoints, 6);
            } else if (color.equals(new Color(176, 224, 230))) {
                int[] xPoints = new int[8];
                int[] yPoints = new int[8];
                for (int i = 0; i < 8; i++) {
                    double angle = Math.PI / 4 * i;
                    int r = (i % 2 == 0) ? size : size / 2;
                    xPoints[i] = (int)(x + r * Math.cos(angle));
                    yPoints[i] = (int)(y + r * Math.sin(angle));
                }
                g.setColor(color);
                g.fillPolygon(xPoints, yPoints, 8);
                g.setColor(Color.WHITE);
                g.drawPolygon(xPoints, yPoints, 8);
            } else {
                //
                g.setColor(color);
                g.fillOval((int)x - size, (int)y - size, size*2, size*2);
            }
        } else {
            // mapa 1 enemy
            g.setColor(color);
            g.fillOval((int)x - size, (int)y - size, size*2, size*2);
        }

        // Pasek zdrowia
        g.setColor(Color.RED);
        g.fillRect((int)x - 10, (int)y - size - 8, 20, 4);
        g.setColor(Color.GREEN);
        g.fillRect((int)x - 10, (int)y - size - 8, (int)(20 * ((double)hp/maxHp)), 4);
    }
}