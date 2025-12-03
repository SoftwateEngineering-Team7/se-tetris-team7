package org.tetris.network.comand;

public class HardDropCommand implements GameCommand {
    @Override
    public void execute(GameCommandExecutor executor) {
        executor.hardDrop();
    }
}
