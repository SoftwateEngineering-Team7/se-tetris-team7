package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class GameResultCommand implements GameCommand {
    private final boolean isWinner;
    private final int score;

    public GameResultCommand(boolean isWinner, int score) {
        this.isWinner = isWinner;
        this.score = score;
    }

    @Override
    public void execute(GameEngine<?, ?> game) {
        game.onGameResult(isWinner, score);
    }
}
