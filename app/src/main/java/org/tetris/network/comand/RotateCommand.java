package org.tetris.network.comand;

import org.tetris.network.game.GameEngine;

public class RotateCommand implements GameCommand {
    @Override
    public void execute(GameEngine game) {
        game.rotate();
    }
}
