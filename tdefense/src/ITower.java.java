import java.awt.Graphics2D;

interface ITower {
    void update();
    void draw(Graphics2D g);
    int getDamage();
    int getRange();
    int getX();
    int getY();
}