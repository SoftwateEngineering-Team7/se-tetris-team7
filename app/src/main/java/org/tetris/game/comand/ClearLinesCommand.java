package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class ClearLinesCommand implements GameCommand {
    private final int linesCleared;

    public ClearLinesCommand(int linesCleared) {
        this.linesCleared = linesCleared;
    }

    @Override
    public void execute(GameEngine<?, ?> game) {
        game.onLinesCleared(linesCleared);
    }
}
