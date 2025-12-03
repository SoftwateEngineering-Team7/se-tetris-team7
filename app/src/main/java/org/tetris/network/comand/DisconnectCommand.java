package org.tetris.network.comand;

/**
 * 상대방이 연결을 끊었음을 알리는 커맨드.
 * 이 커맨드를 받은 플레이어는 자동으로 승리 처리됩니다.
 */
public class DisconnectCommand implements GameCommand {
    private static final long serialVersionUID = 1L;
    
    private final String reason;
    
    /**
     * DisconnectCommand 생성자
     * @param reason 연결 끊김 사유 (예: "상대방이 게임을 종료했습니다", "네트워크 오류")
     */
    public DisconnectCommand(String reason) {
        this.reason = reason;
    }
    
    /**
     * 기본 생성자 - 일반적인 연결 끊김
     */
    public DisconnectCommand() {
        this("상대방이 게임을 종료했습니다.");
    }
    
    public String getReason() {
        return reason;
    }
    
    @Override
    public void execute(GameCommandExecutor executor) {
        executor.onOpponentDisconnect(reason);
    }
}
