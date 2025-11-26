package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class GameStartCommand implements GameCommand {
    private final long seed;

    public GameStartCommand(long seed) {
        this.seed = seed;
    }

    @Override
    public void execute(GameEngine<?, ?> game) {
        game.startGame(seed);
    }
}
