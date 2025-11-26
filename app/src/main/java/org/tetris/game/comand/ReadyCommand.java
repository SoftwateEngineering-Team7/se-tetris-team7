package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class ReadyCommand implements GameCommand{
    private boolean isReady;

    public ReadyCommand(boolean isReady){
        this.isReady = isReady;
    }

    @Override
    public void execute(GameEngine game) {
        game.onReadyCommand(isReady);
    }

    public boolean getIsReady(){
        return isReady;
    }
}
