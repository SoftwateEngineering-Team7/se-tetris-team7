package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

/**
 * Ping에 대한 응답 커맨드.
 * 서버가 클라이언트의 PingCommand를 받으면 이 커맨드로 응답합니다.
 */
public class PongCommand implements GameCommand {
    private final long originalTimestamp;

    public PongCommand(long originalTimestamp) {
        this.originalTimestamp = originalTimestamp;
    }

    public long getOriginalTimestamp() {
        return originalTimestamp;
    }

    @Override
    public void execute(GameEngine game) {
        long currentTime = System.currentTimeMillis();
        long ping = currentTime - originalTimestamp;
        game.updatePing(ping);
    }
}
