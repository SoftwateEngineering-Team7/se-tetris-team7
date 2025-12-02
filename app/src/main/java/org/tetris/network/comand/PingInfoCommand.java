package org.tetris.network.comand;

/**
 * 자신의 Ping 정보를 상대방에게 공유하기 위한 커맨드.
 * 클라이언트가 자신의 ping 값을 측정한 후 서버를 통해 상대방에게 전달합니다.
 */
public class PingInfoCommand implements GameCommand {
    private final long ping;

    public PingInfoCommand(long ping) {
        this.ping = ping;
    }

    public long getPing() {
        return ping;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        executor.updateOpponentPing(ping);
    }
}
