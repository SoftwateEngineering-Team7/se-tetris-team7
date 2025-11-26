package org.tetris.game.engine;

import org.tetris.game.model.DualGameModel;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.view.GameViewCallback;
import javafx.animation.AnimationTimer;

public class LocalMultiGameEngine extends GameEngine<GameViewCallback, DualGameModel> {

    protected PlayerSlot player2;
    private AnimationTimer gameLoop;
    private long lastUpdate = 0;

    // Player 1 state
    private long lastDropTime1 = 0;

    // Player 2 state
    private long lastDropTime2 = 0;

    public LocalMultiGameEngine(PlayerSlot player1, PlayerSlot player2, DualGameModel gameModel,
            GameViewCallback controller) {
        super(player1, gameModel, controller);
        this.player2 = player2;
    }

    @Override
    public void startGame(long seed) {
        startGameLoop();
    }

    @Override
    public void restartGame() {
        resetGameLoop();
        gameModel.getPlayer1GameModel().reset();
        gameModel.getPlayer2GameModel().reset();
        resetPlayerSlot(player);
        resetPlayerSlot(player2);
        startGameLoop();

    }

    @Override
    public void stopGame() {
        resetGameLoop();
    }

    @Override
    public void togglePause() {
        super.togglePause();
        controller.updatePauseUI(gameModel.isPaused());
    }

    @Override
    public void gameOver(int score) {
        if (gameLoop != null)
            gameLoop.stop();
        controller.showGameOver();
    }

    @Override
    public void onGameResult(boolean isWinner, int score) {
        // Handled by controller usually
    }

    // Player 1 Controls (Inherited moveLeft calls doMoveLeft(player))
    @Override
    public void hardDrop() {
        doHardDrop(player, gameModel.getPlayer1GameModel());
    }

    // Player 2 Controls
    public void moveLeftP2() {
        doMoveLeft(player2);
    }

    public void moveRightP2() {
        doMoveRight(player2);
    }

    public void rotateP2() {
        doRotate(player2);
    }

    public void softDropP2() {
        doSoftDrop(player2);
    }

    public void hardDropP2() {
        doHardDrop(player2, gameModel.getPlayer2GameModel());
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    lastDropTime1 = now;
                    lastDropTime2 = now;
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

    private void updateGameLoop(long now) {
        if (gameModel.isPaused())
            return;
        if (gameModel.getPlayer1GameModel().isGameOver() && gameModel.getPlayer2GameModel().isGameOver())
            return;

        updatePlayer(player, gameModel.getPlayer1GameModel(), now, 1);
        updatePlayer(player2, gameModel.getPlayer2GameModel(), now, 2);

        controller.updateScoreDisplay();
        controller.updateLevelDisplay();
        controller.updateLinesDisplay();
        controller.updateNextBlockPreview();
    }

    private void updatePlayer(PlayerSlot p, GameModel model, long now, int playerIdx) {
        if (p == null || model.isGameOver())
            return;

        if (p.isFlashing) {
            tickFlash(p, model, now);
            controller.updateGameBoard(); // This might need to be specific to player?
            // GameViewCallback.updateGameBoard() updates ALL?
            // In DualGameController, updateGameBoard() calls updateGameBoard(slot) for
            // both?
            // No, DualGameViewAdapter calls updateGameBoard(slot).
            // But here controller is a single callback.
            // Wait, GameEngine has ONE controller.
            // In DualGameController, we used TWO engines, each with an adapter.
            // NOW we have ONE engine with ONE controller.
            // The controller passed to LocalMultiGameEngine must handle updates for BOTH.
            // Or we need separate callbacks?
            // DualGameController implements GameViewCallback? No, it has inner class.
            // We need to pass a callback that can handle both.
            // For now, let's assume controller.updateGameBoard() updates everything or we
            // don't care about efficiency yet.
            return;
        }

        long lastDropTime = (playerIdx == 1) ? lastDropTime1 : lastDropTime2;
        int dropIntervalFrames = model.getDropInterval();
        long dropIntervalNanos = dropIntervalFrames * FRAME_TIME;

        if (now - lastDropTime >= dropIntervalNanos) {
            boolean moved = p.boardModel.autoDown();
            if (moved) {
                p.scoreModel.blockDropped();
            } else {
                lockBlock(p, model);
            }
            if (playerIdx == 1)
                lastDropTime1 = now;
            else
                lastDropTime2 = now;
        }

        // We should call updateGameBoard periodically?
        // SingleGameEngine calls it every frame.
        controller.updateGameBoard();
    }

    private void resetGameLoop() {
        if (gameLoop != null)
            gameLoop.stop();
        lastUpdate = 0;
        lastDropTime1 = 0;
        lastDropTime2 = 0;
    }

    private void resetPlayerSlot(PlayerSlot slot) {
        if (slot != null) {
            slot.isFlashing = false;
            slot.flashOn = false;
            slot.flashToggleCount = 0;
            slot.flashMask = null;
            slot.clearingRows.clear();
            slot.clearingCols.clear();
            slot.clearingCells.clear();
            slot.renderer.boardReset();
        }
    }
}
