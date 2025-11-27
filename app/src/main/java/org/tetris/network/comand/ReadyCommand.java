package org.tetris.network.comand;

public class ReadyCommand implements GameMenuCommand {
    private boolean isReady;

    public ReadyCommand(boolean isReady) {
        this.isReady = isReady;
    }

    @Override
    public void execute(GameMenuCommandExecutor executor) {
        executor.onReady(isReady);
    }

    public boolean getIsReady() {
        return isReady;
    }
}
