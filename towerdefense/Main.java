package towerdefense;

import towerdefense.ui.GamePanel;
import towerdefense.patterns.prototype.EnemyCache;
import javax.swing.*;

public class Main extends JFrame {
    public Main() {
        EnemyCache.loadCache();
        this.setTitle("TD v5.0 - 6 Design Patterns");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        GamePanel panel = new GamePanel();
        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        new Thread(panel).start();
    }
    
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings","on");
        SwingUtilities.invokeLater(Main::new);
    }
}