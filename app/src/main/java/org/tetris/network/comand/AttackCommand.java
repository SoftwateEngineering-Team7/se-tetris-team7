package org.tetris.network.comand;

import org.tetris.network.game.GameEngine;

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
