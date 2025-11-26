package org.tetris.game.comand;

import org.tetris.game.engine.GameEngine;

public class GameOverCommand implements GameCommand {
    private final int score;

    public GameOverCommand(int score) {
        this.score = score;
    }

    @Override
    public void execute(GameEngine<?, ?> game) {
        System.out.println("[COMMAND] Opponent Game Over. Score: " + score);
        // 상대방이 게임 오버되었으므로, 나는 승리함
        game.onGameResult(true, score);
    }

    public int getScore() {
        return score;
    }
}
