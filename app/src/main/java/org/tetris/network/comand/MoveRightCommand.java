package org.tetris.network.comand;

public class MoveRightCommand implements GameCommand {
    @Override
    public void execute(GameCommandExecutor executor) {
        executor.moveRight();
    }
}
