package org.tetris.game.model;

public class TimeAttack {

    public final int TIME_LIMIT_SECONDS;

    private boolean isTimeAttackMode;

    private int remainingSeconds;

    public TimeAttack() {
        this(180);
    }

    public TimeAttack(int timeLimit) {
        TIME_LIMIT_SECONDS = timeLimit;
    }

    public int getRemainingSeconds(int elapsedSeconds) {
        if (isTimeAttackMode) {
            int remaining = TIME_LIMIT_SECONDS - elapsedSeconds;
            remainingSeconds = Math.max(remaining, 0);
            return remainingSeconds;
        }

        return -1;
    }

    public void setTimeAttackMode(boolean timeMode) {
        this.isTimeAttackMode = timeMode;
    }
}
