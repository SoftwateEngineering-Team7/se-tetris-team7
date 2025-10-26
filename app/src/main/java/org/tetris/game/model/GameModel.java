package org.tetris.game.model;

import org.tetris.shared.BaseModel;
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
    private int frameCounter;

    public GameModel() {
        this.board = new Board();
        this.nextBlockModel = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 5);
        this.scoreModel = new ScoreModel();
        this.totalLinesCleared = 0;
        this.level = 1;
        this.isGameOver = false;
        this.isPaused = false;
        this.frameCounter = 0;
        
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
    
    public int getFrameCounter() {
        return frameCounter;
    }
    
    public void incrementFrameCounter() {
        this.frameCounter++;
    }
    
    public void resetFrameCounter() {
        this.frameCounter = 0;
    }
    
    // 새 블럭 생성 (성공 여부 반환)
    public boolean spawnNewBlock() {
        Block newBlock = nextBlockModel.getBlock();
        boolean spawned = board.setActiveBlock(newBlock);
        if (!spawned) {
            isGameOver = true;
        }
        return spawned;
    }
    
    // 블럭 고정 및 라인 클리어 처리
    public int lockBlockAndClearLines() {
        int linesCleared = board.clearLines();
        if (linesCleared > 0) {
            totalLinesCleared += linesCleared;
            scoreModel.lineCleared(linesCleared);
            updateLevel();
        }
        return linesCleared;
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
        frameCounter = 0;
        spawnNewBlock();
    }
    
    // 레벨에 따른 낙하 속도 계산
    public int getDropInterval() {
        return Math.max(10, 60 - (level * 5));
    }
} 