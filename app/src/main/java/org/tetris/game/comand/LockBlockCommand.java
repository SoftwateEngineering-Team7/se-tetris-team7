package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class LockBlockCommand implements GameCommand {
    @Override
    public void execute(GameEngine<?, ?> game) {
        // 엔진 내부에서 lockCurrentBlock 호출
        // engine.lockCurrentBlock(); // GameEngine에 추상 메서드 추가 필요
    }
}
