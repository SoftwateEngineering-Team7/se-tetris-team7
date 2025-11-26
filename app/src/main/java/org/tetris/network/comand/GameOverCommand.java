package org.tetris.network.comand;

import org.tetris.network.game.GameEngine;

public class GameOverCommand implements GameCommand {
    private final int score;

    public GameOverCommand(int score) {
        this.score = score;
    }

    @Override
    public void execute(GameEngine game) {
        // 서버에서 수신 시: 상대방이 게임 오버됨 -> 나는 승리
        // 클라이언트에서 수신 시: 상대방이 게임 오버됨 -> 나는 패배
        // 여기서는 단순히 로그만 남기거나, GameEngine에 처리를 위임.
        System.out.println("[COMMAND] Opponent Game Over. Score: " + score);
    }
    
    public int getScore() {
        return score;
    }
}
