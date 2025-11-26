package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class RestartGameCommand {
    public void execute(GameEngine<?, ?> game) {
        game.restartGame();
    }
}
