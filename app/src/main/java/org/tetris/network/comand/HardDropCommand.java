package org.tetris.network.comand;

import org.tetris.network.game.GameEngine;

public class HardDropCommand implements GameCommand {
    @Override
    public void execute(GameEngine<?, ?> game) {
        game.hardDrop();
    }
}
