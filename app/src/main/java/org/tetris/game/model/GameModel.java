package org.tetris.game.model;

import org.tetris.shared.BaseModel;
import org.util.Difficulty;
import org.tetris.game.model.blocks.*;
import org.tetris.game.model.items.*;

public class GameModel extends BaseModel {

    private final static int ITEM_MODE_LINE_THRESHOLD = 10;

    private final NextBlockModel nextBlockModel;
    private final Board board;  
    private ScoreModel scoreModel;

    private int totalLinesCleared;
    private int localLineCleared = 0;
    private int level;

    private boolean isItemMode = true; // TODO: 기본값 false로 변경
    private boolean isItemUsed = false;
    private Item activeItem = Item.getRandomItem();

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
    
    // TODO: 난이도에 따른 블럭 출현 확률 설정 + 게임 시작 전에 호출
    public void setGameStart(Difficulty difficulty, boolean itemMode)
    {
        this.isItemMode = itemMode;
        //TODO: this.nextBlockModel.setBlockProbList(difficulty.getBlockProbList());
    }

    // 새 블럭 생성 (게임 오버 판단은 Model에서)
    public void spawnNewBlock() {
        Block newBlock = nextBlockModel.getBlock();

        if (isItemMode && !isItemUsed) {
            newBlock = activeItem.GetItemBlock(newBlock);
        }

        boolean spawned = board.setActiveBlock(newBlock);
        if (!spawned) {
            isGameOver = true;  // Model이 게임 오버 상태 관리
        }
    }
    
    // 블럭 고정 및 라인 클리어 처리
    public int lockBlockAndClearLines() {
        int linesCleared = board.clearLines();
        if (linesCleared > 0) {
            totalLinesCleared += linesCleared;
            localLineCleared += linesCleared;

            scoreModel.lineCleared(linesCleared);

            updateLevel();
            updateItemMode();
        }
        
        if (isItemMode && !isItemUsed) {
            isItemUsed = true;
            activateItem();
            scoreModel.itemActivated();
        }

        return linesCleared;
    }
    
    // 레벨 업데이트 (10줄마다 레벨 증가)
    private void updateLevel() {
        level = (totalLinesCleared / 10) + 1;
    }

    public void updateItemMode()
    {
        if (!isItemMode) return;
    
        if (localLineCleared >= ITEM_MODE_LINE_THRESHOLD) {
            localLineCleared = 0;
            activeItem = Item.getRandomItem();
            isItemUsed = false;
        }
    }

    public void activateItem()
    {
        if (!isItemMode) return;
        activeItem.Activate(board);
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
        return Math.max(10, 60 - (level * 5));
    }
} 