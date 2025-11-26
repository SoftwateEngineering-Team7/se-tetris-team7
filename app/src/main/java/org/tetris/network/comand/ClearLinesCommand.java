package org.tetris.network.comand;

import org.tetris.network.game.GameEngine;

public class ClearLinesCommand implements GameCommand {

    public ClearLinesCommand() {
    }

    @Override
    public void execute(GameEngine<?, ?> game) {
        // engine.onLinesCleared(lines); // GameEngine에 메서드 추가 필요
    }
}
