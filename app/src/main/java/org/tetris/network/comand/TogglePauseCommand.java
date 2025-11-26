package org.tetris.network.comand;

import org.tetris.network.game.GameEngine;

public class TogglePauseCommand implements GameCommand {
    @Override
    public void execute(GameEngine<?, ?> gameEngine) {
        gameEngine.togglePause();
    }
}
