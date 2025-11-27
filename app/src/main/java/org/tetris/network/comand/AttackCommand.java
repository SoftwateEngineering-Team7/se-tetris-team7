package org.tetris.network.comand;

public class AttackCommand implements GameCommand {
    private final int lines;

    public AttackCommand(int lines) {
        this.lines = lines;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        executor.attack(lines);
    }
}
