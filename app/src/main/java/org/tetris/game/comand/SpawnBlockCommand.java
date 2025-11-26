package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class SpawnBlockCommand implements GameCommand {
    @Override
    public void execute(GameEngine<?, ?> engine) {
        // engine.spawnNewBlock(); // GameEngine에 메서드 추가 필요
    }
}
