package org.tetris.game.controller;

import org.tetris.network.comand.*;
import org.tetris.network.GameClient;
import org.tetris.game.model.P2PGameModel;
import org.tetris.game.model.PlayerSlot;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;

import java.util.Arrays;
import org.tetris.network.dto.MatchSettings;
import org.util.KeyLayout;
import org.util.PlayerId;
import java.util.Random;

public class P2PGameController extends DualGameController<P2PGameModel>
        implements GameCommandExecutor {

    GameClient client;

    public P2PGameController(P2PGameModel model) {
        super(model);
        this.client = GameClient.getInstance();
        client.setGameExecutor(this);
    }

    /**
     * P2P 게임은 서버로부터 GameStartCommand를 받을 때까지 일시정지 상태로 시작
     */
    @Override
    public void initialize() {
        super.initialize();
        
        // 게임을 일시정지 상태로 시작 (gameStart()에서 해제됨)
        // Platform.runLater(() -> {
        //     if (player1 != null && player2 != null) {
        //         player1.gameModel.setPaused(true);
        //         player2.gameModel.setPaused(true);
        //         System.out.println("[P2P-CONTROLLER] Game initialized in paused state. Waiting for GameStartCommand...");
        //     }
        // });
    }

    /**
     * 로컬 플레이어 슬롯 반환
     * playerNumber가 1이면 player1(왼쪽), 2이면 player2(오른쪽)
     */
    private PlayerSlot getLocalPlayer() {
        return model.getPlayerNumber() == 1 ? player1 : player2;
    }

    /**
     * 원격 플레이어 슬롯 반환
     * playerNumber가 1이면 player2(오른쪽), 2이면 player1(왼쪽)
     */
    private PlayerSlot getRemotePlayer() {
        return model.getPlayerNumber() == 1 ? player2 : player1;
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        // 로컬 플레이어의 입력만 처리하고 서버로 커맨드 전송
        PlayerSlot localPlayer = getLocalPlayer();
        if (localPlayer == null || localPlayer.gameModel.isPaused() || localPlayer.gameModel.isGameOver()) {
            return;
        }

        KeyCode code = e.getCode();
        GameCommand command = null;
        boolean updateNeeded = false;

        if (code == KeyLayout.getLeftKey(PlayerId.PLAYER1)) {
            command = new MoveLeftCommand();
            localPlayer.boardModel.moveLeft();
            updateNeeded = true;
        } else if (code == KeyLayout.getRightKey(PlayerId.PLAYER1)) {
            command = new MoveRightCommand();
            localPlayer.boardModel.moveRight();
            updateNeeded = true;
        } else if (code == KeyLayout.getUpKey(PlayerId.PLAYER1)) {
            command = new RotateCommand();
            localPlayer.boardModel.rotate();
            updateNeeded = true;
        } else if (code == KeyLayout.getDownKey(PlayerId.PLAYER1)) {
            command = new SoftDropCommand();
            if (localPlayer.boardModel.moveDown()) {
                localPlayer.scoreModel.softDrop(1);
            }
            updateNeeded = true;
        } else if (code == KeyLayout.getHardDropKey(PlayerId.PLAYER1)) {
            command = new HardDropCommand();
            handleHardDrop(localPlayer);
        }


        
        if (updateNeeded) {
            updateGameBoard(localPlayer);
            localPlayer.renderer.renderNextBlock(localPlayer.nextBlockModel.peekNext());
        }

        if (command != null) {
            client.sendCommand(command);
        }
    }

    // --- GameCommandExecutor Implementation (Remote Player Updates) ---

    @Override
    public void moveLeft() {
        Platform.runLater(() -> {
            PlayerSlot remotePlayer = getRemotePlayer();
            if (remotePlayer != null) {
                remotePlayer.boardModel.moveLeft();
                updateGameBoard(remotePlayer);
            }
        });
    }

    @Override
    public void moveRight() {
        Platform.runLater(() -> {
            PlayerSlot remotePlayer = getRemotePlayer();
            if (remotePlayer != null) {
                remotePlayer.boardModel.moveRight();
                updateGameBoard(remotePlayer);
            }
        });
    }

    @Override
    public void rotate() {
        Platform.runLater(() -> {
            PlayerSlot remotePlayer = getRemotePlayer();
            if (remotePlayer != null) {
                remotePlayer.boardModel.rotate();
                updateGameBoard(remotePlayer);
            }
        });
    }

    @Override
    public void softDrop() {
        Platform.runLater(() -> {
            PlayerSlot remotePlayer = getRemotePlayer();
            if (remotePlayer != null) {
                if (remotePlayer.boardModel.moveDown()) {
                    remotePlayer.scoreModel.softDrop(1);
                }
                updateGameBoard(remotePlayer);
            }
        });
    }

    @Override
    public void hardDrop() {
        Platform.runLater(() -> {
            PlayerSlot remotePlayer = getRemotePlayer();
            if (remotePlayer != null) {
                handleHardDrop(remotePlayer);
            }
        });
    }

    @Override
    public void attack(int lines) {
        Platform.runLater(() -> {
            PlayerSlot remotePlayer = getRemotePlayer();
            if (remotePlayer != null) {
                Random rand = new Random();
                for (int i = 0; i < lines; i++) {
                    int[] garbageRow = new int[10]; // Assuming width 10
                    Arrays.fill(garbageRow, 8); // 8 = Gray
                    garbageRow[rand.nextInt(10)] = 0; // Hole
                    remotePlayer.attackModel.push(garbageRow);
                }
            }
        });
    }

    @Override
    public void gameStart(MatchSettings settings) {
        Platform.runLater(() -> {
            if (player1 != null && player2 != null) {
                // Set player number to determine local/remote mapping
                model.setPlayerNumber(settings.getPlayerNumber());
                
                System.out.println("[P2P-CONTROLLER] I am Player " + settings.getPlayerNumber());
                System.out.println("[P2P-CONTROLLER] My seed: " + settings.getMySeed() + 
                                   ", Other seed: " + settings.getOtherSeed());
                
                // Initialize seeds for both players
                model.setNextBlockSeed(settings.getMySeed(), settings.getOtherSeed());
                
                // Reset both game models to apply new seeds
                model.getLocalGameModel().reset();
                model.getRemoteGameModel().reset();
                
                // Reset player slots
                player1.reset();
                player2.reset();
                
                // Unpause the game to start playing
                player1.gameModel.setPaused(false);
                player2.gameModel.setPaused(false);
                
                System.out.println("[P2P-CONTROLLER] Game started! Local player controls " + 
                                   (settings.getPlayerNumber() == 1 ? "left" : "right") + " screen.");
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
    protected void goToMenu()
    {
        resetGameController();
        hideGameOverlay();
        if (router != null)
            router.showNetworkMenu(false);
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
