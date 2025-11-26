package org.tetris.network.game;

import java.util.List;
import org.util.Point;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.PlayerSlot;
import org.tetris.shared.BaseModel;
import org.tetris.game.view.GameViewCallback;

/**
 * 가상의 게임 엔진 (커맨드 실행 대상)
 * 실제 게임의 로직을 처리하는 역할을 가정합니다.
 */
public abstract class GameEngine<C extends GameViewCallback, M extends BaseModel> {
    private String currentState = "Initial State";
    protected PlayerSlot player;
    protected M gameModel;
    protected C controller;

    // 플래시 애니메이션 파라미터
    protected static final int FLASH_TIMES = 2;
    protected static final int FLASH_TOGGLES = FLASH_TIMES * 2; // on/off 합계
    protected static final long FLASH_INTERVAL_NANOS = 100_000_000L; // 100ms
    protected static final long FRAME_TIME = 16_666_667; // ~60 FPS (나노초)

    public GameEngine() {
        this.player = null;
        this.gameModel = (M) null;
        this.controller = (C) null;
    }

    public GameEngine(M gameModel) {
        this.gameModel = gameModel;
        this.controller = (C) null;
    }

    public GameEngine(M gameModel, C controller) {
        this.gameModel = gameModel;
        this.controller = controller;
    }

    public GameEngine(PlayerSlot player, M gameModel) {
        this.player = player;
        this.gameModel = gameModel;
        this.controller = (C) null;
    }

    public GameEngine(PlayerSlot player, M gameModel, C controller) {
        this.player = player;
        this.gameModel = gameModel;
        this.controller = controller;
    }

    public abstract void startGame(long seed);

    public abstract void restartGame();

    public abstract void stopGame();

    public abstract void gameOver(int score);

    public abstract void onGameResult(boolean isWinner, int score);

    // Default implementations for Single Player (operating on 'player')
    // Assumes M is GameModel or we can cast it.
    // Actually, for SingleGameEngine, M is GameModel.
    // We need a way to get the GameModel associated with 'player'.
    // Since GameEngine is generic, we can cast gameModel if M is GameModel.

    private GameModel getSingleGameModel() {
        if (gameModel instanceof GameModel)
            return (GameModel) gameModel;
        return null;
    }

    public void moveLeft() {
        doMoveLeft(player);
    }

    public void moveRight() {
        doMoveRight(player);
    }

    public void rotate() {
        doRotate(player);
    }

    public void softDrop() {
        doSoftDrop(player);
    }

    public void hardDrop() {
        doHardDrop(player, getSingleGameModel());
    }

    // Core Logic Methods (Protected)

    protected void doMoveLeft(PlayerSlot target) {
        if (target != null)
            target.boardModel.moveLeft();
    }

    protected void doMoveRight(PlayerSlot target) {
        if (target != null)
            target.boardModel.moveRight();
    }

    protected void doRotate(PlayerSlot target) {
        if (target != null)
            target.boardModel.rotate();
    }

    protected void doSoftDrop(PlayerSlot target) {
        if (target != null) {
            boolean moved = target.boardModel.moveDown();
            if (moved) {
                target.scoreModel.softDrop(1);
            }
        }
    }

    protected void doHardDrop(PlayerSlot target, GameModel targetModel) {
        if (target != null) {
            int dropDistance = target.boardModel.hardDrop();
            target.scoreModel.add(dropDistance * 2);
            lockBlock(target, targetModel);
        }
    }

    protected void lockBlock(PlayerSlot target, GameModel targetModel) {
        List<Integer> fullRows = target.boardModel.findFullRows();
        target.clearingRows.addAll(fullRows);

        // 아이템 활성화
        if (targetModel != null && controller instanceof org.tetris.game.model.items.ItemActivation) {
            targetModel.tryActivateItem((org.tetris.game.model.items.ItemActivation) controller);
        }

        if (target.boardModel.getIsForceDown()) {
            boolean moved = target.boardModel.moveDown(true);
            if (!moved) {
                target.boardModel.removeCurrentBlock();
                if (targetModel != null) {
                    targetModel.updateModels(0);
                    targetModel.spawnNewBlock();
                }
                if (controller != null)
                    controller.updateGameBoard();
                checkGameOver(target, targetModel);
            }
            return;
        }

        if (!target.clearingRows.isEmpty() || !target.clearingCols.isEmpty() || !target.clearingCells.isEmpty()) {
            beginFlash(target, targetModel, System.nanoTime());
            return;
        }

        if (controller != null)
            controller.updateGameBoard();
        if (targetModel != null)
            targetModel.spawnNewBlock();
        checkGameOver(target, targetModel);
    }

    protected void beginFlash(PlayerSlot target, GameModel targetModel, long now) {
        target.isFlashing = true;
        target.flashMask = target.renderer.buildFlashMask(
                target.clearingRows, target.clearingCols, target.clearingCells);
        target.flashOn = false;
        target.flashToggleCount = 0;
        target.nextFlashAt = now;
        // Store targetModel in PlayerSlot? No, PlayerSlot doesn't have it.
        // We need to pass it to tickFlash. But tickFlash is called from loop.
        // We can't easily store it in GameEngine if we have multiple.
        // Wait, SingleGameEngine has 1 model. LocalMulti has 2.
        // We can pass it when calling tickFlash.
    }

    protected void tickFlash(PlayerSlot target, GameModel targetModel, long now) {
        if (target == null || !target.isFlashing || target.flashMask == null)
            return;
        if (now < target.nextFlashAt)
            return;

        target.flashOn = !target.flashOn;
        target.flashToggleCount++;
        target.nextFlashAt = now + FLASH_INTERVAL_NANOS;

        if (target.flashToggleCount >= FLASH_TOGGLES) {
            target.isFlashing = false;
            target.flashOn = false;
            target.flashMask = null;
            processClears(target, targetModel);
        }
    }

    protected void processClears(PlayerSlot target, GameModel targetModel) {
        int linesCleared = deleteCompletedRows(target);
        int colsCleared = deleteCompletedCols(target);
        deleteCompletedCells(target);

        if (targetModel != null) {
            targetModel.updateModels(linesCleared + colsCleared);
            targetModel.spawnNewBlock();
        }
        checkGameOver(target, targetModel);
    }

    protected int deleteCompletedRows(PlayerSlot target) {
        for (int r : target.clearingRows) {
            target.boardModel.clearRow(r);
        }
        int count = target.clearingRows.size();
        target.clearingRows.clear();
        return count;
    }

    protected int deleteCompletedCols(PlayerSlot target) {
        for (int c : target.clearingCols) {
            target.boardModel.clearColumn(c);
        }
        int count = target.clearingCols.size();
        target.clearingCols.clear();
        return count;
    }

    protected void deleteCompletedCells(PlayerSlot target) {
        int boardHeight = target.boardModel.getSize().r;
        int boardWidth = target.boardModel.getSize().c;
        for (Point p : target.clearingCells) {
            if (p.r >= 0 && p.r < boardHeight && p.c >= 0 && p.c < boardWidth) {
                target.boardModel.getBoard()[p.r][p.c] = 0;
            }
        }
        target.clearingCells.clear();
    }

    protected void checkGameOver(PlayerSlot target, GameModel targetModel) {
        if (targetModel != null) {
            if (targetModel.isGameOver()) {
                gameOver(target.scoreModel.getScore());
            }
        }
    }

    public void attack(int lines) {
        System.out.println("[CLIENT-ENGINE] Attacked! Adding " + lines + " garbage lines.");
    }

    public void updateState(String state) {
        this.currentState = state;
        System.out.println("[CLIENT-ENGINE] State updated to: " + state);
    }

    public void togglePause() {
        if (gameModel instanceof org.tetris.shared.Pausable) {
            org.tetris.shared.Pausable pausable = (org.tetris.shared.Pausable) gameModel;
            pausable.setPaused(!pausable.isPaused());
        }
    }

    public String getCurrentState() {
        return currentState;
    }

    public boolean isPaused() {
        if (gameModel instanceof org.tetris.shared.Pausable) {
            return ((org.tetris.shared.Pausable) gameModel).isPaused();
        }
        return false;
    }
}
