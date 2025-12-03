package org.tetris.network.comand;

public class MoveLeftCommand implements GameCommand {
    @Override
    public void execute(GameCommandExecutor executor) {
        executor.moveLeft();
    }
}
