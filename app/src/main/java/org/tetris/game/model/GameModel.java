package org.tetris.game.model;

import org.tetris.shared.BaseModel;
import org.util.Difficulty;
import org.tetris.game.controller.ItemController;
import org.tetris.game.model.blocks.*;
import org.tetris.game.model.items.ItemActivation;

import org.tetris.shared.Pausable;

public class GameModel extends BaseModel implements Pausable {

    public final static int MAX_DROP_INTERVAL = 60; // 최대 낙하 간격 (레벨 1)
    public final static int MIN_DROP_INTERVAL = 10; // 드롭 간격 감소량

    private final NextBlockModel nextBlockModel;
    private final Board board;
    private ScoreModel scoreModel;

    private int totalLinesCleared;
    private int level;

    private boolean isItemMode = false;
    private ItemController itemController = new ItemController();

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

    public boolean isItemMode() {
        return isItemMode;
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

    public void setItemMode(boolean itemMode) {
        this.isItemMode = itemMode;
        System.out.println("Item Mode: " + itemMode);
    }

    public void setDifficulty() {
        this.nextBlockModel.setBlockProbList(Difficulty.getBlockProbList());
    }

    // 새 블럭 생성 (게임 오버 판단은 Model에서)
    public void spawnNewBlock() {
        Block newBlock = nextBlockModel.getBlock();

        boolean spawned = board.setActiveBlock(newBlock);
        if (!spawned) {
            isGameOver = true; // Model이 게임 오버 상태 관리
        }

        // 아이템 모드이고 아이템 생성 가능하면 다음 블록에 아이템 적용
        if (isItemMode && itemController.canSpawnItem()) {
            Block targetBlock = nextBlockModel.peekNext();
            targetBlock = itemController.spawnItem(targetBlock);
            nextBlockModel.swapNext(targetBlock);
        }
    }

    public void updateModels(int linesCleared) {

        if (linesCleared > 0) {
            totalLinesCleared += linesCleared;
            board.collapse();

            scoreModel.lineCleared(linesCleared);

            updateLevel();

            if (!isItemMode)
                return; // ITEM_MODE
            itemController.onLineCleared(linesCleared);
        }

        return;
    }

    public boolean tryActivateItem(ItemActivation context) {
        if (!isItemMode)
            return false;
        if (!board.activeBlock.isItemBlock())
            return false;

        itemController.activateItem(board, context);
        return true;
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
        int dropInterval = MAX_DROP_INTERVAL - Math.round((level - 1) * 5 * difficultyMul);

        scoreModel.setGravityMultiplier(dropInterval);

        return Math.max(MIN_DROP_INTERVAL, dropInterval);
    }
}