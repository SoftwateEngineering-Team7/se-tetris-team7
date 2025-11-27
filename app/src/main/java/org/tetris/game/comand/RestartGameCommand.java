package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class RestartGameCommand implements GameCommand {
    @Override
    public void execute(GameEngine<?, ?> game) {
        game.restartGame();
    }
