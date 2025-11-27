package org.tetris.network.comand;

public class GameOverCommand implements GameCommand {
    private final int score;

    public GameOverCommand(int score) {
        this.score = score;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        executor.gameOver(score);
    }

    public int getScore() {
        return score;
    }
}
