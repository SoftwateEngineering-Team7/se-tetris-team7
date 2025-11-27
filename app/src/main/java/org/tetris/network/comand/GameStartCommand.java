package org.tetris.network.comand;

public class GameStartCommand implements GameCommand, GameMenuCommand {
    private final long mySeed;
    private final long otherSeed;

    public GameStartCommand(long mySeed, long otherSeed) {
        this.mySeed = mySeed;
        this.otherSeed = otherSeed;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        executor.gameStart(mySeed, otherSeed);
    }

    @Override
    public void execute(GameMenuCommandExecutor executor) {
        executor.gameStart();
    }

}
