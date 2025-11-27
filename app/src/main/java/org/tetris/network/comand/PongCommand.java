package org.tetris.network.comand;

/**
 * Ping에 대한 응답 커맨드.
 * 서버가 클라이언트의 PingCommand를 받으면 이 커맨드로 응답합니다.
 */
public class PongCommand implements GameCommand, GameMenuCommand {
    private final long originalTimestamp;

    public PongCommand(long originalTimestamp) {
        this.originalTimestamp = originalTimestamp;
    }

    public long getOriginalTimestamp() {
        return originalTimestamp;
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        long current = System.currentTimeMillis();
        long ping = current - originalTimestamp;
        executor.updatePing(ping);
    }

    @Override
    public void execute(GameMenuCommandExecutor executor) {
        long current = System.currentTimeMillis();
        long ping = current - originalTimestamp;
        executor.updatePing(ping);
    }
}
