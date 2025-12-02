package org.tetris.network.comand;

/**
 * 일시정지 상태를 동기화하기 위한 커맨드.
 * P2P 대전 모드에서 한 플레이어가 일시정지하면 상대방도 동시에 일시정지됩니다.
 */
public class PauseCommand implements GameCommand {
    private static final long serialVersionUID = 1L;
    
    private final boolean isPaused;
    
    /**
     * PauseCommand 생성자
     * @param isPaused true면 일시정지, false면 일시정지 해제
     */
    public PauseCommand(boolean isPaused) {
        this.isPaused = isPaused;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    @Override
    public void execute(GameCommandExecutor executor) {
        if (isPaused) {
            executor.pause();
        } else {
            executor.resume();
        }
    }
}
