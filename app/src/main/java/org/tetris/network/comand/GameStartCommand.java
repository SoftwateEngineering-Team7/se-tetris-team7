package org.tetris.network.comand;

import org.tetris.network.game.GameEngine;

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
