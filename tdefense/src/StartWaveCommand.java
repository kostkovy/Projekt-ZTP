class StartWaveCommand implements IGameCommand {
    private WaveManager wm;
    public StartWaveCommand(WaveManager wm) { this.wm = wm; }
    @Override
    public void execute() { wm.startWave(); }
}