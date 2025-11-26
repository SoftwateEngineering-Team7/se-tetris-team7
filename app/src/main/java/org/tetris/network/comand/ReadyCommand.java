package org.tetris.network.comand;

import org.tetris.network.game.GameEngine;

public class ReadyCommand implements GameCommand{
    private boolean isReady;

    public ReadyCommand(boolean isReady){
        this.isReady = isReady;
    }

    @Override
    public void execute(GameEngine game) {
        game.setOtherReady(isReady);
    }

    public boolean getIsReady(){
        return isReady;
    }
}
