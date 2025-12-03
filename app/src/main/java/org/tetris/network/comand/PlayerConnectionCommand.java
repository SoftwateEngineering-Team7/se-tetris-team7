package org.tetris.network.comand;

public class PlayerConnectionCommand implements GameMenuCommand {
    private static final long serialVersionUID = 1L;
    private final boolean opponentConnected;

    public PlayerConnectionCommand(boolean opponentConnected) {
        this.opponentConnected = opponentConnected;
    }

    @Override
    public void execute(GameMenuCommandExecutor executor) {
        executor.onPlayerConnectionChanged(opponentConnected);
    }

    public boolean isOpponentConnected() {
        return opponentConnected;
    }
}
