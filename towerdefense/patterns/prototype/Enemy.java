package towerdefense.patterns.prototype;

import towerdefense.GameManager;
import towerdefense.game.GameState;
import java.awt.*;

public class Enemy implements Prototype {
    public double x, y;
    public int hp, maxHp, reward, size;
    public double speed;
    public Color color;
    public boolean alive = true, finished = false;
    private int pathIndex = 0;

    public Enemy(String typeId, int hp, double speed, int reward, Color color, int size) {
        this.hp = hp; 
        this.maxHp = hp; 
        this.speed = speed;
        this.reward = reward; 
        this.color = color; 
        this.size = size;
        this.x = GameManager.getInstance().pathPoints[0].x;
        this.y = GameManager.getInstance().pathPoints[0].y;
    }

    @Override
    public Prototype clone() {
        try { 
            return (Enemy) super.clone(); 
        } catch (CloneNotSupportedException e) { 
            return null; 
        }
    }

    public void buffHealth(int amount) { 
        this.maxHp += amount; 
        this.hp = this.maxHp; 
    }

    public void update() {
        if (!alive) return;
        Point[] path = GameManager.getInstance().pathPoints;
        if (pathIndex < path.length) {
            Point target = path[pathIndex];
            double dx = target.x - x, dy = target.y - y;
            double dist = Math.sqrt(dx*dx + dy*dy);
            if (dist < speed) pathIndex++;
            else { 
                x += (dx/dist)*speed; 
                y += (dy/dist)*speed; 
            }
        } else finished = true;
    }

    public void draw(Graphics2D g) {
        g.setColor(color); 
        g.fillOval((int)x - size, (int)y - size, size*2, size*2);
        g.setColor(Color.RED); 
        g.fillRect((int)x - 10, (int)y - size - 8, 20, 4);
        g.setColor(Color.GREEN); 
        g.fillRect((int)x - 10, (int)y - size - 8, (int)(20 * ((double)hp/maxHp)), 4);
    }
}