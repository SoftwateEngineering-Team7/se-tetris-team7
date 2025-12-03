package org.tetris.network.comand;

public class SoftDropCommand implements GameCommand {
    @Override
    public void execute(GameCommandExecutor executor) {
        executor.softDrop();
    }
}
