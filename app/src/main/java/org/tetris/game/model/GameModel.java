package org.tetris.game.model;

import org.tetris.shared.BaseModel;
import org.util.Difficulty;
import org.tetris.game.model.blocks.Block;

public class GameModel extends BaseModel {
    private final NextBlockModel nextBlockModel;
    private final Board board;
    private ScoreModel scoreModel;
    private int totalLinesCleared;
    private int level;

    // 게임 상태
    private boolean isGameOver;
    private boolean isPaused;

    public GameModel() {
        this.board = new Board();
        this.nextBlockModel = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 5);
        this.scoreModel = new ScoreModel();
        this.totalLinesCleared = 0;
        this.level = 1;
        this.isGameOver = false;
        this.isPaused = false;

        // 첫 블럭 설정
        spawnNewBlock();
    }

    public Board getBoardModel() {
        return board;
    }

    public NextBlockModel getNextBlockModel() {
        return nextBlockModel;
    }

    public ScoreModel getScoreModel() {
        return scoreModel;
    }

    public int getTotalLinesCleared() {
        return totalLinesCleared;
    }

    public int getLevel() {
        return level;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    // 새 블럭 생성 (게임 오버 판단은 Model에서)
    public void spawnNewBlock() {
        Block newBlock = nextBlockModel.getBlock();
        boolean spawned = board.setActiveBlock(newBlock);
        if (!spawned) {
            isGameOver = true; // Model이 게임 오버 상태 관리
        }
    }

    public void updateModels(int linesCleared) {
        if (linesCleared > 0) {
            totalLinesCleared += linesCleared;
            board.collapse();
            scoreModel.lineCleared(linesCleared);
            updateLevel();
        }
    }

    // 레벨 업데이트 (10줄마다 레벨 증가)
    private void updateLevel() {
        level = (totalLinesCleared / 10) + 1;
    }

    // 게임 리셋
    public void reset() {
        board.reset();
        scoreModel.reset();
        totalLinesCleared = 0;
        level = 1;
        isGameOver = false;
        isPaused = false;
        spawnNewBlock();
    }

    // 레벨에 따른 낙하 속도 계산
    public int getDropInterval() {
        float difficultyMul = Difficulty.getSpeedMultiplier();
        return Math.max(10, 60 - Math.round(level * 5 * difficultyMul));
    }
}