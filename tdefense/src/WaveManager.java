class WaveManager {
    private GameManager gm = GameManager.getInstance();
    private int enemiesLeftToSpawn = 0;
    private long lastSpawnTime = 0;

    public void startWave() {
        if(gm.state == GameState.PREP_PHASE) {
            gm.state = GameState.WAVE_IN_PROGRESS;
            enemiesLeftToSpawn = 6 + (gm.wave * 2);
            gm.waveStarted(gm.wave); // Powiadomienie obserwatorów
        }
    }

    public void update() {
        if (gm.state != GameState.WAVE_IN_PROGRESS) return;

        if (enemiesLeftToSpawn > 0 && System.currentTimeMillis() - lastSpawnTime > 800) {
            String type = "NORMAL";
            double r = Math.random();
            if (gm.wave >= 2 && r < 0.3) type = "FAST";
            if (gm.wave >= 4 && r < 0.2) type = "TANK";

            Enemy e = EnemyCache.getEnemy(type);
            e.buffHealth((gm.wave / 3) * 50);
            gm.enemies.add(e);
            enemiesLeftToSpawn--;
            lastSpawnTime = System.currentTimeMillis();
        }

        if (enemiesLeftToSpawn == 0 && gm.enemies.isEmpty()) {
            gm.state = GameState.PREP_PHASE;
            gm.waveCompleted(gm.wave); // Powiadomienie obserwatorów
            gm.wave++;
            gm.addMoney(40 + (gm.wave * 10));
        }
    }
}