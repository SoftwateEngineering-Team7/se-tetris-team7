package org.tetris.network.comand;

public class GameResultCommand implements GameCommand {
    private final boolean isWinner;
    private final int score;

    public GameResultCommand(boolean isWinner, int score) {
        this.isWinner = isWinner;
        this.score = score;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        executor.onGameResult(isWinner, score);
    }
}
