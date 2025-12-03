package org.tetris.network.comand;

import java.util.ArrayList;
import java.util.List;

public class GameOverCommand implements GameCommand {
    private static final long serialVersionUID = 2L;
    
    private final int score;
    private final List<int[]> pendingAttacks;

    public GameOverCommand(int score) {
        this.score = score;
        this.pendingAttacks = new ArrayList<>();
    }

    public GameOverCommand(int score, List<int[]> pendingAttacks) {
        this.score = score;
        this.pendingAttacks = pendingAttacks != null ? new ArrayList<>(pendingAttacks) : new ArrayList<>();
    }

    @Override
    public void execute(GameCommandExecutor executor) {
        executor.gameOver(score, pendingAttacks);
    }

    public int getScore() {
        return score;
    }

    public List<int[]> getPendingAttacks() {
        return pendingAttacks;
    }
}
