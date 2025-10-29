package org.tetris.game.model;

public class ScoreModel {
    private int score;
    private final int[] lineClearScores = {0, 1000, 3000, 5000, 8000}; // 0, 1, 2, 3, 4줄 클리어 시 점수
    private final int itemActivationScore = 1000; // 아이템 활성화 시 점수

    public ScoreModel() {
        this.score = 0;
    }

    public int getScore() {
        return score;
    }

    public void add(int amount)
    {
        if (amount > 0) {
            score += amount;
        }
    }

    public void lineCleared(int lines)
    {
        int length = lineClearScores.length;

        if (lines >= 0 && lines < length) {
            score += lineClearScores[lines];
        }
        else if (lines >= length) {
            score += lineClearScores[length - 1] * (lines - (length - 1));
        }
    }

    public void itemActivated()
    {
        score += itemActivationScore;
    }

    public void reset() {
        score = 0;
    }

    @Override
    public String toString() {
        return String.format("%08d", score);
    }
}   
