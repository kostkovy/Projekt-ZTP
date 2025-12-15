package towerdefense.patterns.decorator;

import java.awt.Graphics2D;

public interface ITower {
    void update();
    void draw(Graphics2D g);
    int getDamage();
    int getRange();
    int getX();
    int getY();
}