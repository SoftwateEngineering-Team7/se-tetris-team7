package org.tetris.game.engine;

import org.util.Point;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.PlayerSlot;

import javafx.animation.AnimationTimer;

import org.tetris.game.view.GameViewCallback;

public class SingleGameEngine extends GameEngine<GameViewCallback, GameModel> {

    private AnimationTimer gameLoop;
    private long lastUpdate = 0;
    private long lastDropTime = 0; // 마지막 블록 낙하 시간

    protected SingleGameEngine(PlayerSlot player, GameModel gameModel, GameViewCallback controller) {
        super(player, gameModel, controller);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GameEngine.Builder<Builder, GameViewCallback, GameModel> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public SingleGameEngine build() {
            return new SingleGameEngine(player, gameModel, controller);
        }
    }

    public void startGame(long seed) {

        startGameLoop();
    }

    @Override
    public void restartGame() {
        resetGameLoop();
        gameModel.reset();
        resetPlayerSlot();
        startGameLoop();
    }

    public void stopGame() {
        resetGameLoop();
    }

    public void gameOver(int score) {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        controller.showGameOver();

    }

    public void onGameResult(boolean isWinner, int score) {

    }

    public void updateState(String state) {
        super.updateState(state);
    }

    public String getCurrentState() {
        return super.getCurrentState();
    }

    // ===== 메인 게임 루프 =====

    protected void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    lastDropTime = now;
                    return;
                }

                long elapsed = now - lastUpdate;
                if (elapsed >= FRAME_TIME) {
                    updateGameLoop(now);
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }

    protected void updateGameLoop(long now) {
        if (gameModel.isPaused() || gameModel.isGameOver()) {
            return;
        }

        if (player != null && player.isFlashing) {
            tickFlash(player, gameModel, now);
            controller.updateGameBoard();
            return;
        }

        // 레벨에 따른 블록 낙하 간격 (프레임 기준)
        int dropIntervalFrames = gameModel.getDropInterval();
        long dropIntervalNanos = dropIntervalFrames * FRAME_TIME;

        long timeSinceLastDrop = now - lastDropTime;
        if (timeSinceLastDrop >= dropIntervalNanos) {
            boolean moved = player.boardModel.autoDown();

            if (player != null) {
                if (moved) {
                    player.scoreModel.blockDropped();
                } else {
                    lockBlock(player, gameModel);
                }
            }

            lastDropTime = now;
        }

        controller.updateGameBoard();
        controller.updateScoreDisplay();
        controller.updateLevelDisplay();
        controller.updateLinesDisplay();
        controller.updateNextBlockPreview();
    }

    protected void resetPlayerSlot() {
        if (player != null) {
            player.isFlashing = false;
            player.flashOn = false;
            player.flashToggleCount = 0;
            player.flashMask = null;

            player.clearingRows.clear();
            player.clearingCols.clear();
            player.clearingCells.clear();

            player.renderer.boardReset();
        }
    }

    protected void resetGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        lastUpdate = 0;
        lastDropTime = 0;
    }

    @Override
    public void togglePause() {
        super.togglePause();
        controller.updatePauseUI(gameModel.isPaused());
    }

}
