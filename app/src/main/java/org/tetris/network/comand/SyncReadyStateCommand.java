package org.tetris.network.comand;

/**
 * 상대방의 현재 Ready 상태를 동기화하는 커맨드.
 * 화면 전환 후 메뉴로 돌아왔을 때 상대방의 Ready 상태를 받기 위해 사용됩니다.
 */
public class SyncReadyStateCommand implements GameMenuCommand {
    private final boolean opponentReady;

    public SyncReadyStateCommand(boolean opponentReady) {
        this.opponentReady = opponentReady;
    }

    @Override
    public void execute(GameMenuCommandExecutor executor) {
        executor.onReady(opponentReady);
    }

    public boolean isOpponentReady() {
        return opponentReady;
    }
}
