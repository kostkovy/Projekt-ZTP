package towerdefense.patterns.command;

import towerdefense.ui.GamePanel;

public class BuyTowerCommand implements IGameCommand {
    private GamePanel panel;
    private String type;
    private int cost, range;

    public BuyTowerCommand(GamePanel panel, String type, int cost, int range) {
        this.panel = panel; 
        this.type = type; 
        this.cost = cost; 
        this.range = range;
    }
    
    @Override
    public void execute() {
        panel.setSelectedTower(type, cost, range);
    }
}