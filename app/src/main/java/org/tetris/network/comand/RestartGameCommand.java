package org.tetris.network.comand;

import org.tetris.network.game.GameEngine;

public class RestartGameCommand {
    public void execute(GameEngine<?, ?> game) {
        game.restartGame();
    }
}
