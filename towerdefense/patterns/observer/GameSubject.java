package towerdefense.patterns.observer;

import java.util.ArrayList;
import java.util.List;

public abstract class GameSubject {
    protected List<GameObserver> observers = new ArrayList<>();
    
    public void addObserver(GameObserver o) { 
        observers.add(o); 
    }
    
    public void removeObserver(GameObserver o) { 
        observers.remove(o); 
    }
    
    protected void notifyObservers() { 
        for(GameObserver o : observers) o.onGameUpdate(); 
    }
}