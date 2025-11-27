package org.tetris.network.comand;

/**
 * Ping 측정을 위한 커맨드.
 * 클라이언트가 서버에 Ping을 보내면, 서버는 PongCommand로 응답합니다.
 */
public class PingCommand implements GameCommand, GameMenuCommand {
    private final long timestamp;

    public PingCommand() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        // Ping 명령은 클라이언트 엔진에서 특별히 처리할 내용이 없을 수 있음
        // 필요하다면 디버그 로그 출력
        // System.out.println("Ping received");
    }

    @Override
    public void execute(GameMenuCommandExecutor executor) {
        // Ping 명령은 클라이언트 엔진에서 특별히 처리할 내용이 없을 수 있음
        // 필요하다면 디버그 로그 출력
        // System.out.println("Ping received");
    }
}
