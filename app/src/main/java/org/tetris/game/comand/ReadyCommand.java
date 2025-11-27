package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;
import org.tetris.game.engine.P2PGameEngine;

public class ReadyCommand implements GameCommand {
    private boolean isReady;

    public ReadyCommand(boolean isReady) {
        this.isReady = isReady;
    }

    @Override
    public void execute(GameEngine<?, ?> game) {
        if (game instanceof P2PGameEngine) {
            ((P2PGameEngine) game).onReadyCommand(isReady);
        }
    }

    public boolean getIsReady() {
        return isReady;
    }
}
