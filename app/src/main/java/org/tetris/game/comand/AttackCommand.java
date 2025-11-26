package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class AttackCommand implements GameCommand {
    private final int lines;

    public AttackCommand(int lines) {
        this.lines = lines;
    }

    @Override
    public void execute(GameEngine<?, ?> game) {
        game.attack(lines);
    }
}
