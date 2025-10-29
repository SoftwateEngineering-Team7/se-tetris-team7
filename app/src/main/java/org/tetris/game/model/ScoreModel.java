package org.tetris.game.model;

import org.util.Difficulty;

public class ScoreModel {
    private int score;
    private final int[] lineClearScores = {0, 1000, 3000, 5000, 8000}; // 0, 1, 2, 3, 4줄 클리어 시 점수
    private final int itemActivationScore = 1000; // 아이템 활성화 시 점수
    
    // 블록 떨어질 때마다 획득하는 기본 점수 단위
    private final int easyBlockDropScore = 1;
    private final int mediumBlockDropScore = 2;
    private final int hardBlockDropScore = 3;
    
    // 자동 낙하와 수동 낙하의 점수 배율
    private final int softDropMultiplier = 1;  // 자동 낙하 (기본)
    private int gravityMultiplier = 1;

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
    
    /**
     * 블록이 1칸 떨어질 때마다 호출 (자동 낙하)
     * 기본적으로 1점 획득
     */

    public void setGravityMultiplier(int dropInterval) {
        if (dropInterval >= 50) {
            gravityMultiplier = 1;
        } else if (dropInterval >= 30) {
            gravityMultiplier = 2;
        } else {
            gravityMultiplier = 3;
        }
    }


    public void blockDropped() {
        if (Difficulty.getCurrentDifficulty().equals(Difficulty.EASY_STRING))
            score += easyBlockDropScore * softDropMultiplier * gravityMultiplier;
        else if (Difficulty.getCurrentDifficulty().equals(Difficulty.NORMAL_STRING))
            score += mediumBlockDropScore * softDropMultiplier * gravityMultiplier;
        else if (Difficulty.getCurrentDifficulty().equals(Difficulty.HARD_STRING))
            score += hardBlockDropScore * softDropMultiplier * gravityMultiplier;
    }
    
    /**
     * 소프트 드롭 (수동으로 아래 키 누를 때)
     * @param distance 떨어진 칸 수
     */
    public void softDrop(int distance) {
        if (distance > 0) {
            if (Difficulty.getCurrentDifficulty().equals(Difficulty.EASY_STRING))
                score += easyBlockDropScore * softDropMultiplier * distance * gravityMultiplier;
            else if (Difficulty.getCurrentDifficulty().equals(Difficulty.NORMAL_STRING))
                score += mediumBlockDropScore * softDropMultiplier * distance * gravityMultiplier;
            else if (Difficulty.getCurrentDifficulty().equals(Difficulty.HARD_STRING))
                score += hardBlockDropScore * softDropMultiplier * distance * gravityMultiplier;
        }
    }
    

    public void reset() {
        score = 0;
    }

    @Override
    public String toString() {
        return String.format("%08d", score);
    }
}   
