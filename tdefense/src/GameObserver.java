
interface GameObserver {

    default void onGameUpdate() {}

    default void onGameEvent(GameEvent event) {}
}