package org.tetris.network.comand;

import org.tetris.network.game.GameEngine;

public class ResumeGameCommand implements GameCommand {

    public void execute(GameEngine<?, ?> game) {
        // game.resumeGame(); // GameEngine에 메서드 추가 필요
    }
}
