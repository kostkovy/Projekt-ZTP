/**
 * OBSERVER PATTERN - Ulepszona wersja interfejsu obserwatora
 * Pozwala obserwatorom reagować na konkretne zdarzenia
 */
interface GameObserver {
    /**
     * Metoda wywoływana przy każdej zmianie w grze (kompatybilność wsteczna)
     */
    default void onGameUpdate() {}

    /**
     * Metoda wywoływana przy konkretnych zdarzeniach
     */
    default void onGameEvent(GameEvent event) {}
}