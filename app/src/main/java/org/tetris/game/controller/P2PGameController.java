package org.tetris.game.controller;

import org.tetris.network.comand.GameCommandExecutor;
import org.tetris.network.comand.GameCommand;
import org.tetris.network.comand.*;
import org.tetris.network.GameClient;
import org.tetris.game.model.P2PGameModel;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;

import java.util.Arrays;
import org.tetris.network.dto.MatchSettings;
import java.util.Random;

public class P2PGameController extends DualGameController<P2PGameModel>
        implements GameCommandExecutor {

    GameClient client;

    public P2PGameController(P2PGameModel model) {
        super(model);
        this.client = GameClient.getInstance();
        client.setGameExecutor(this);
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        // 로컬 플레이어(Player 1)의 입력만 처리하고 서버로 커맨드 전송
        if (player1 == null || player1.gameModel.isPaused() || player1.gameModel.isGameOver()) {
            return;
        }

        KeyCode code = e.getCode();
        GameCommand command = null;
        boolean updateNeeded = false;

        if (code == KeyCode.LEFT) {
            command = new MoveLeftCommand();
            player1.boardModel.moveLeft();
            updateNeeded = true;
        } else if (code == KeyCode.RIGHT) {
            command = new MoveRightCommand();
            player1.boardModel.moveRight();
            updateNeeded = true;
        } else if (code == KeyCode.UP) {
            command = new RotateCommand();
            player1.boardModel.rotate();
            updateNeeded = true;
        } else if (code == KeyCode.DOWN) {
            command = new SoftDropCommand();
            if (player1.boardModel.moveDown()) {
                player1.scoreModel.softDrop(1);
            }
            updateNeeded = true;
        } else if (code == KeyCode.SPACE) {
            command = new HardDropCommand();
            handleHardDrop(player1);
            // handleHardDrop already calls lockCurrentBlock and updates score
            // No need to set updateNeeded as handleHardDrop logic usually triggers updates
            // or next block
            // But we should ensure UI is updated.
            // handleHardDrop calls lockCurrentBlock -> checkLines -> spawnNewBlock ->
            // renderNextBlock
            // So it should be fine.
        }

        if (updateNeeded) {
            updateGameBoard(player1);
            player1.renderer.renderNextBlock(player1.nextBlockModel.peekNext());
        }

        if (command != null) {
            client.sendCommand(command);
        }
    }

    // Override handlePlayerInput to do nothing for P2, because P2 is controlled by
    // network commands
    @Override
    protected void handlePlayerInput(KeyEvent e, org.tetris.game.model.PlayerSlot player,
            KeyCode left, KeyCode right, KeyCode rotate, KeyCode down, KeyCode hardDrop) {
        if (player == player1) {
            super.handlePlayerInput(e, player, left, right, rotate, down, hardDrop);
        }
    }

    // --- GameCommandExecutor Implementation (Remote Player P2 Updates) ---

    @Override
    public void moveLeft() {
        Platform.runLater(() -> {
            if (player2 != null) {
                player2.boardModel.moveLeft();
                updateGameBoard(player2);
            }
        });
    }

    @Override
    public void moveRight() {
        Platform.runLater(() -> {
            if (player2 != null) {
                player2.boardModel.moveRight();
                updateGameBoard(player2);
            }
        });
    }

    @Override
    public void rotate() {
        Platform.runLater(() -> {
            if (player2 != null) {
                player2.boardModel.rotate();
                updateGameBoard(player2);
            }
        });
    }

    @Override
    public void softDrop() {
        Platform.runLater(() -> {
            if (player2 != null) {
                if (player2.boardModel.moveDown()) {
                    player2.scoreModel.softDrop(1);
                }
                updateGameBoard(player2);
            }
        });
    }

    @Override
    public void hardDrop() {
        Platform.runLater(() -> {
            if (player2 != null) {
                handleHardDrop(player2);
            }
        });
    }

    @Override
    public void attack(int lines) {
        Platform.runLater(() -> {
            if (player2 != null) {
                Random rand = new Random();
                for (int i = 0; i < lines; i++) {
                    int[] garbageRow = new int[10]; // Assuming width 10
                    Arrays.fill(garbageRow, 8); // 8 = Gray
                    garbageRow[rand.nextInt(10)] = 0; // Hole
                    player2.attackModel.push(garbageRow);
                }
            }
        });
    }

    @Override
    public void gameStart(MatchSettings settings) {
        Platform.runLater(() -> {
            if (player1 != null && player2 != null) {
                // Initialize seeds for both players
                model.setNextBlockSeed(settings.getMySeed(), settings.getOtherSeed());

                // Start the game loop or reset if needed
                // player1.gameModel.startGame(); // If needed
                // player2.gameModel.startGame(); // If needed
            }
        });
    }

    @Override
    public void gameOver(int score) {
        Platform.runLater(() -> {
            stopGame();
            showGameOverDialog("Game Over", "Winner: Player 1\nScore: " + score);
        });
    }

    @Override
    public void onGameResult(boolean isWinner, int score) {
        Platform.runLater(() -> {
            stopGame();
            showGameOverDialog(isWinner ? "Victory!" : "Defeat",
                    (isWinner ? "You Won!" : "You Lost") + "\nScore: " + score);
        });
    }

    @Override
    public void updateState(String state) {
        // Sync state if needed
    }

    @Override
    public void updatePing(long timestamp) {
        // Handle ping
    }

    private void showGameOverDialog(String title, String message) {
        if (winnerLabel != null) {
            winnerLabel.setText(message);
        }
        showGameOverlay();
    }
}
