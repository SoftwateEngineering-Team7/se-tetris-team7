package org.tetris.game.view;

import org.tetris.game.model.items.ItemActivation;

public interface GameViewCallback extends ItemActivation {
    void updateGameBoard();

    void updateScoreDisplay();

    void updateLevelDisplay();

    void updateLinesDisplay();

    void updateNextBlockPreview();

    void showGameOver();

    void updatePauseUI(boolean isPaused);
}
