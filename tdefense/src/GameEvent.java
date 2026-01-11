/**
 * Enum reprezentujący różne zdarzenia w grze
 * Pozwala obserwatorom reagować tylko na konkretne zdarzenia
 */
enum GameEventType {
    MONEY_CHANGED,
    LIVES_CHANGED,
    WAVE_STARTED,
    WAVE_COMPLETED,
    ENEMY_KILLED,
    TOWER_BUILT,
    TOWER_UPGRADED,
    GAME_OVER,
    GAME_RESET
}

/**
 * Klasa zdarzenia - zawiera typ i dodatkowe dane
 */
class GameEvent {
    public final GameEventType type;
    public final Object data;

    public GameEvent(GameEventType type) {
        this(type, null);
    }

    public GameEvent(GameEventType type, Object data) {
        this.type = type;
        this.data = data;
    }
}