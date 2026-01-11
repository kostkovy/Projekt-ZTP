class WaveManager {
    private GameManager gm = GameManager.getInstance();
    private int enemiesLeftToSpawn = 0;
    private long lastSpawnTime = 0;
    private long spawnDelay = 800;

    public void startWave() {
        if(gm.state == GameState.PREP_PHASE) {
            gm.state = GameState.WAVE_IN_PROGRESS;

            if (gm.wave <= 5) {
                enemiesLeftToSpawn = 8 + (gm.wave * 2);
            } else if (gm.wave <= 10) {
                enemiesLeftToSpawn = 18 + (gm.wave * 3);
            } else if (gm.wave <= 20) {
                enemiesLeftToSpawn = 45 + (gm.wave * 2);
            } else {
                enemiesLeftToSpawn = 85 + (gm.wave * 3);
            }

            if (gm.wave >= 15) {
                spawnDelay = 500;
            } else if (gm.wave >= 10) {
                spawnDelay = 600;
            } else if (gm.wave >= 5) {
                spawnDelay = 700;
            } else {
                spawnDelay = 800;
            }

            gm.waveStarted(gm.wave);
        }
    }

    public void update() {
        if (gm.state != GameState.WAVE_IN_PROGRESS) return;

        if (enemiesLeftToSpawn > 0 && System.currentTimeMillis() - lastSpawnTime > spawnDelay) {
            String type = selectEnemyType();

            Enemy e = EnemyCache.getEnemy(type);


            int healthBonus = calculateHealthBonus();
            e.buffHealth(healthBonus);

            gm.enemies.add(e);
            enemiesLeftToSpawn--;
            lastSpawnTime = System.currentTimeMillis();
        }

        if (enemiesLeftToSpawn == 0 && gm.enemies.isEmpty()) {
            gm.state = GameState.PREP_PHASE;
            gm.waveCompleted(gm.wave);

            int reward = calculateWaveReward();
            gm.addMoney(reward);

            gm.wave++;
        }
    }

    private String selectEnemyType() {
        boolean isWinter = gm.wave >= 11;
        double r = Math.random();

        if (isWinter) {
            // Zimowi przeciwnicy
            if (gm.wave >= 15 && r < 0.25) {
                return "FROST_GIANT"; // 25% szansa na Lodowego Olbrzyma
            } else if (r < 0.40) {
                return "BLIZZARD"; // 40% szansa na Blizzard (szybki)
            } else {
                return "ICE"; // 35% szansa na Ice (normalny)
            }
        } else {
            // Letni przeciwnicy
            if (gm.wave >= 7 && r < 0.25) {
                return "TANK"; // 25% szansa na czołg od fali 7
            } else if (gm.wave >= 3 && r < 0.45) {
                return "FAST"; // 45% szansa na szybkiego od fali 3
            } else {
                return "NORMAL"; // 30% szansa na normalnego
            }
        }
    }

    private int calculateHealthBonus() {
        // Progresywne zwiększanie HP
        if (gm.wave <= 5) {
            return gm.wave * 30;
        } else if (gm.wave <= 10) {
            return 150 + (gm.wave - 5) * 50;
        } else if (gm.wave <= 20) {
            return 400 + (gm.wave - 10) * 80;
        } else {
            return 1200 + (gm.wave - 20) * 120;
        }
    }

    private int calculateWaveReward() {
        // Progresywne nagrody
        if (gm.wave <= 5) {
            return 50 + (gm.wave * 10);
        } else if (gm.wave <= 10) {
            return 100 + (gm.wave * 15);
        } else if (gm.wave <= 20) {
            return 250 + (gm.wave * 20);
        } else {
            return 650 + (gm.wave * 25);
        }
    }
}