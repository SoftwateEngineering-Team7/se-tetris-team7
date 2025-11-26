package org.tetris.network.comand;

import org.tetris.network.game.GameEngine;

/**
 * Ping 측정을 위한 커맨드.
 * 클라이언트가 서버에 Ping을 보내면, 서버는 PongCommand로 응답합니다.
 */
public class PingCommand implements GameCommand {
    private final long timestamp;

    public PingCommand() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void execute(GameEngine game) {
        // 클라이언트 측에서는 실행되지 않음 (서버에서 PongCommand로 응답)
    }
}
