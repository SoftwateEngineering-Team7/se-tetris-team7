package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class TogglePauseCommand implements GameCommand {
    @Override
    public void execute(GameEngine<?, ?> gameEngine) {
        gameEngine.togglePause();
    }
}
