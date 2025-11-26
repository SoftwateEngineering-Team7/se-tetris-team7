package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class RotateCommand implements GameCommand {
    @Override
    public void execute(GameEngine<?, ?> game) {
        game.rotate();
    }
}
