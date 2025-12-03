package org.tetris.game.controller;

import org.tetris.network.comand.*;
import org.tetris.network.GameClient;
import org.tetris.network.GameServer;
import org.tetris.game.model.P2PGameModel;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.model.blocks.Block;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.application.Platform;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.tetris.network.dto.MatchSettings;
import org.util.KeyLayout;
import org.util.PlayerId;
import org.util.Point;

public class P2PGameController extends DualGameController<P2PGameModel>
        implements GameCommandExecutor {

    @FXML
    private HBox disconnectOverlay;
    @FXML
    private Label disconnectReasonLabel;
    @FXML
    private Button disconnectMenuButton;

    @FXML
    private Label myPingLabel;
    @FXML
    private Label opponentPingLabel;

    private GameClient client;
    private volatile boolean opponentDisconnected = false;
    private volatile int pauseOwner = 0;
    private MatchSettings currentSettings;

    private int localBlockCount = 1;
    private int remoteBlockCount = 1;

    private static final long PING_TIMEOUT_MS = 10000;
    private volatile long lastPingTime = 0;
    private Timer pingTimeoutTimer;
    private volatile boolean gameStarted = false;

    public P2PGameController(P2PGameModel model) {
        super(model);
        this.client = GameClient.getInstance();
        client.setGameExecutor(this);
    }

    @Override
    public void initialize() {
        super.initialize();
        Platform.runLater(() -> {
            if (disconnectMenuButton != null) {
                disconnectMenuButton.setOnAction(e -> goToMainMenuFromDisconnect());
            }
        });
    }

    @Override
    protected void update(long now) {
        if (player1 == null || player2 == null)
            return;
        if (player1.gameModel.isPaused() || player2.gameModel.isPaused())
            return;

        if(isGameOver){
            return;
        }

        handleLocalPlayerUpdate(getLocalPlayer(), now);
        handleRemotePlayerUpdate(getRemotePlayer(), now);
        updateUI();
    }

    private void handleLocalPlayerUpdate(PlayerSlot player, long now) {
        if (player.gameModel.isGameOver())
            return;
        if (player.isFlashing) {
            super.handlePlayerUpdate(player, now);
            return;
        }

        int dropIntervalFrames = player.gameModel.getDropInterval();
        long dropIntervalNanos = dropIntervalFrames * 16_666_667L;

        if (now - player.lastDropTime >= dropIntervalNanos) {
            boolean moved = player.boardModel.moveDown();
            if (moved) {
                player.scoreModel.blockDropped();
            } else {
                lockCurrentBlock(player);
            }
            player.lastDropTime = now;
        }
    }

    private void handleRemotePlayerUpdate(PlayerSlot player, long now) {
        if (player.gameModel.isGameOver())
            return;

        // [추가된 방어 코드] activeBlock이 null이면(동기화 대기 중) 아무것도 하지 않음
        if (player.boardModel.activeBlock == null) {
            return;
        }

        if (player.isFlashing) {
            super.handlePlayerUpdate(player, now);
            return;
        }

        int dropIntervalFrames = player.gameModel.getDropInterval();
        long dropIntervalNanos = dropIntervalFrames * 16_666_667L;

        if (now - player.lastDropTime >= dropIntervalNanos) {
            boolean moved = player.boardModel.moveDown();
            if (moved) {
                player.scoreModel.blockDropped();
            }
            player.lastDropTime = now;
        }
    }

    private PlayerSlot getLocalPlayer() {
        return player1;
    }

    private PlayerSlot getRemotePlayer() {
        return player2;
    }

    private boolean isHost() {
        return model.getPlayerNumber() == 1;
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        if (e.getCode() == KeyCode.P) {
            if (isHost())
                togglePauseAndSync();
            else
                toggleClientPauseMenu();
            e.consume();
            return;
        }

        PlayerSlot localPlayer = getLocalPlayer();
        if (localPlayer == null || localPlayer.gameModel.isPaused() || localPlayer.gameModel.isGameOver())
            return;
        if (localPlayer.isFlashing) {
            e.consume();
            return;
        }

        if(isGameOver){
            e.consume();
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
            client.sendCommand(new HardDropCommand());
            handleHardDrop(localPlayer);
            return;
        }

        if (updateNeeded) {
            updateGameBoard(localPlayer);
            localPlayer.renderer.renderNextBlock(localPlayer.nextBlockModel.peekNext());
        }

        if (command != null) {
            client.sendCommand(command);
        }
    }

    /**
     * [FIXED] 안전한 보드 캡처 및 전송 (Local Piercing Fix)
     * 게임 오버 상태라면, 캡처를 위해 잠깐 지웠던 블록을 다시 복구하지 않습니다.
     * 이렇게 해야 내 화면에서도 블록이 겹쳐 보이는(뚫리는) 현상이 사라집니다.
     */
    private void captureAndSendBoard(PlayerSlot player) {
        if (player != getLocalPlayer() || client == null)
            return;

        // [핵심 1] 게임 오버가 '아닐 때만' 활성 블록을 지웁니다.
        // 게임 오버라면(이미 꽉 찼다면) 굳이 지울 필요 없이 그 상태 그대로 캡처하면 됩니다.
        // (여기서 지우려고 시도하다가 겹쳐있는 기존 블록을 파먹어서 검은색 구멍이 생기는 것임)
        if (!player.gameModel.isGameOver()) {
            player.boardModel.removeCurrentBlock();
        }
        else{
            int[][] rawBoard = player.boardModel.getBoard();
            int[][] boardCopy = new int[rawBoard.length][];
            for (int i = 0; i < rawBoard.length; i++) {
                boardCopy[i] = java.util.Arrays.copyOf(rawBoard[i], rawBoard[i].length);
            }
            // 내가 가지고 있던 공격 데이터도 함께 전송
            java.util.List<int[]> myPendingAttacks = new java.util.ArrayList<>(player.attackModel.getAttacks());
            client.sendCommand(new GameOverCommand(player.scoreModel.getScore(), myPendingAttacks));
            client.sendCommand(new BoardSyncCommand(boardCopy, localBlockCount));
            return;
        }

        // 2. 보드 복사 (이 시점에서 rawBoard는 깨끗하거나, 혹은 꽉 찬 상태)
        int[][] rawBoard = player.boardModel.getBoard();
        int[][] boardCopy = new int[rawBoard.length][];
        for (int i = 0; i < rawBoard.length; i++) {
            boardCopy[i] = Arrays.copyOf(rawBoard[i], rawBoard[i].length);
        }

        // [핵심 2] 게임 오버가 '아닐 때만' 다시 복구합니다.
        // 게임 오버면 복구할 블록이 없거나, 복구하면 뚫림 현상이 생기므로 패스.
        if (player.boardModel.activeBlock != null && player.boardModel.curPos != null) {
            player.boardModel.placeBlock(player.boardModel.curPos, player.boardModel.activeBlock);
        }

        // 3. 전송 (게임 오버라도 '마지막 상태'는 전송됨)
        client.sendCommand(new BoardSyncCommand(boardCopy, localBlockCount));
    }

    @Override
    protected void lockCurrentBlock(PlayerSlot player) {
        super.lockCurrentBlock(player); // 여기서 GameModel.spawnNewBlock()이 호출됨 -> 실패 시 Game Over 설정됨

        if (player == getLocalPlayer()) {
            localBlockCount++;

            // 줄 클리어가 없으면 즉시 전송
            if (client != null && player.clearingRows.isEmpty() && player.clearingCols.isEmpty()
                    && player.clearingCells.isEmpty()) {
                captureAndSendBoard(player);
            }
        }
    }

    @Override
    protected void processClears(PlayerSlot player) {
        if (player == getRemotePlayer()) {
            player.clearingRows.clear();
            player.clearingCols.clear();
            player.clearingCells.clear();
            return;
        }
        super.processClears(player);
        if (player == getLocalPlayer() && client != null) {
            captureAndSendBoard(player);
        }
    }

    @Override
    protected void processIncomingAttacks(PlayerSlot player) {
        if (player != getLocalPlayer())
            return;
        super.processIncomingAttacks(player);
    }

    @Override
    protected void checkGameOverState() {
        PlayerSlot localPlayer = getLocalPlayer();
        if (localPlayer != null && localPlayer.gameModel.isGameOver() && !isGameOver) {
            isGameOver = true;
            if (gameLoop != null)
                gameLoop.stop();
            if (client != null) {
                // 내가 가지고 있던 공격 데이터도 함께 전송
                java.util.List<int[]> myPendingAttacks = new java.util.ArrayList<>(localPlayer.attackModel.getAttacks());
                client.sendCommand(new GameOverCommand(localPlayer.scoreModel.getScore(), myPendingAttacks));
            }
            showGameOverDialog("Game Over", "You Lost!");
        }
    }

    @Override
    public void attack(List<int[]> attackRows) {
        Platform.runLater(() -> {
            PlayerSlot localPlayer = getLocalPlayer();
            if (localPlayer != null && attackRows != null && !attackRows.isEmpty()) {
                for (int[] r : attackRows) {
                    localPlayer.attackModel.push(r);
                }
                localPlayer.renderer.renderAttackBoard(localPlayer.attackModel.getAttacks());
            }
        });
    }

    /**
     * [FIXED] 3가지 경우(뒤쳐짐, 앞서감, 일치함)를 명확히 분리
     * - activeBlock이 null이 될 수 있는 상황을 고려하여 로직 구성
     */
    Block tempSavedBlock = null;
    @Override
    public void syncBoard(int[][] boardState, int blockCount) {
        Platform.runLater(() -> {
            PlayerSlot remotePlayer = getRemotePlayer();
            if (remotePlayer != null && boardState != null) {

                if (isGameOver) {
                    remotePlayer.boardModel.removeCurrentBlock();
                    remotePlayer.boardModel.setBoard(boardState);
                    updateGameBoard(remotePlayer);
                    return;
                }

                // count가 앞서 있는 경우 같은 개수가 될 때까지 보드만 업데이트
                if(remoteBlockCount > blockCount && tempSavedBlock != null){
                    remotePlayer.boardModel.setBoard(boardState);
                    return;
                }

                // 1. [Backup] 현재 상태 백업
                Block savedBlock = remotePlayer.boardModel.activeBlock;

                // 2. [Reset] 화면 청소 및 서버 보드 적용
                // 일단 현재 블록을 지우고 서버 상태를 적용함 (가장 확실한 방법)
                remotePlayer.boardModel.removeCurrentBlock();
                remotePlayer.boardModel.setBoard(boardState);

                // 3. [Sync Logic] 3가지 케이스 분기

                // CASE 1: 로컬이 뒤쳐짐 (Remote < Server) -> Catch-up 수행
                if (remoteBlockCount < blockCount) {
                    while (remoteBlockCount < blockCount) {
                        remotePlayer.gameModel.spawnNewBlock();
                        remoteBlockCount++;
                        if(remoteBlockCount != blockCount){
                            remotePlayer.boardModel.removeCurrentBlock();
                        }
                        remotePlayer.lastDropTime = System.nanoTime();
                    }
                } else if (remoteBlockCount > blockCount) {
                    tempSavedBlock = remotePlayer.boardModel.activeBlock;
                    remotePlayer.boardModel.activeBlock = null;
                } else {
                    if(tempSavedBlock != null){
                        remotePlayer.boardModel.setActiveBlock(tempSavedBlock);
                        tempSavedBlock = null;
                    } else {
                        remotePlayer.boardModel.setActiveBlock(savedBlock);
                    }
                }

                // 4. [Final Check] 게임 오버 시 잔상 제거
                if (remotePlayer.gameModel.isGameOver()) {
                    remotePlayer.boardModel.activeBlock = null;
                }

                // 5. UI 갱신
                while (remotePlayer.attackModel.pop() != null) {
                }
                remotePlayer.renderer.renderAttackBoard(remotePlayer.attackModel.getAttacks());
                updateGameBoard(remotePlayer);
                remotePlayer.renderer.renderNextBlock(remotePlayer.nextBlockModel.peekNext());
            }
        });
    }

    // --- Other Overrides ---
    @Override
    protected void togglePause() {
        if (isHost())
            togglePauseAndSync();
        else
            toggleClientPauseMenu();
    }

    private void togglePauseAndSync() {
        if (player1 == null || player2 == null)
            return;
        boolean currentlyPaused = player1.gameModel.isPaused();
        int myPlayerNumber = model.getPlayerNumber();
        if (currentlyPaused) {
            if (pauseOwner != myPlayerNumber)
                return;
            player1.gameModel.setPaused(false);
            player2.gameModel.setPaused(false);
            hidePauseOverlay();
            pauseOwner = 0;
            client.sendCommand(new PauseCommand(false));
        } else {
            player1.gameModel.setPaused(true);
            player2.gameModel.setPaused(true);
            showPauseOverlay();
            pauseOwner = myPlayerNumber;
            client.sendCommand(new PauseCommand(true));
        }
    }

    private void toggleClientPauseMenu() {
        if (pauseOverlay == null)
            return;
        if (pauseOverlay.isVisible())
            hidePauseOverlay();
        else
            showPauseOverlay();
    }

    private void goToMainMenuFromDisconnect() {
        cleanupNetworkResources();
        hideDisconnectOverlay();
        if (router != null)
            router.showStartMenu();
    }

    private void cleanupNetworkResources() {
        try {
            if (client != null)
                client.disconnect();
            if (isHost())
                GameServer.getInstance().stop();
        } catch (Exception e) {
        }
    }

    private void showDisconnectOverlay(String reason) {
        if (disconnectOverlay != null) {
            if (disconnectReasonLabel != null)
                disconnectReasonLabel.setText(reason);
            disconnectOverlay.setVisible(true);
            disconnectOverlay.setManaged(true);
        }
    }

    private void hideDisconnectOverlay() {
        if (disconnectOverlay != null) {
            disconnectOverlay.setVisible(false);
            disconnectOverlay.setManaged(false);
        }
    }

    private void startPingTimeoutTimer() {
        stopPingTimeoutTimer();
        lastPingTime = System.currentTimeMillis();
        pingTimeoutTimer = new Timer("PingTimeoutTimer", true);
        pingTimeoutTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!gameStarted || opponentDisconnected)
                    return;
                long timeSinceLastPing = System.currentTimeMillis() - lastPingTime;
                if (timeSinceLastPing > PING_TIMEOUT_MS)
                    onOpponentDisconnect("상대방과의 연결이 끊겼습니다.\n(응답 시간 초과)");
            }
        }, 2000, 2000);
    }

    private void stopPingTimeoutTimer() {
        if (pingTimeoutTimer != null) {
            pingTimeoutTimer.cancel();
            pingTimeoutTimer = null;
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        stopPingTimeoutTimer();
        gameStarted = false;
        cleanupNetworkResources();
    }

    @Override
    protected void onResumeButtonClicked() {
        if (player1 == null || player2 == null)
            return;
        if (!isHost()) {
            hidePauseOverlay();
            return;
        }
        if (pauseOwner != model.getPlayerNumber())
            return;
        player1.gameModel.setPaused(false);
        player2.gameModel.setPaused(false);
        hidePauseOverlay();
        pauseOwner = 0;
        client.sendCommand(new PauseCommand(false));
    }

    @Override
    protected void onRestartButtonClicked() {
        hideGameOverlay();
        client.sendCommand(new RestartCommand());
    }

    @Override
    public void moveLeft() {
        Platform.runLater(() -> {
            if (getRemotePlayer() != null) {
                getRemotePlayer().boardModel.moveLeft();
                updateGameBoard(getRemotePlayer());
            }
        });
    }

    @Override
    public void moveRight() {
        Platform.runLater(() -> {
            if (getRemotePlayer() != null) {
                getRemotePlayer().boardModel.moveRight();
                updateGameBoard(getRemotePlayer());
            }
        });
    }

    @Override
    public void rotate() {
        Platform.runLater(() -> {
            if (getRemotePlayer() != null) {
                getRemotePlayer().boardModel.rotate();
                updateGameBoard(getRemotePlayer());
            }
        });
    }

    @Override
    public void softDrop() {
        Platform.runLater(() -> {
            if (getRemotePlayer() != null) {
                if (getRemotePlayer().boardModel.moveDown())
                    getRemotePlayer().scoreModel.softDrop(1);
                updateGameBoard(getRemotePlayer());
            }
        });
    }

    @Override
    public void hardDrop() {
        Platform.runLater(() -> {
            if (getRemotePlayer() != null) {
                int d = getRemotePlayer().boardModel.hardDrop();
                getRemotePlayer().scoreModel.add(d * 2);
                updateGameBoard(getRemotePlayer());
            }
        });
    }

    @Override
    protected void sendAttack(PlayerSlot attacker) {
        if (attacker == getLocalPlayer() && client != null) {
            int lines = attacker.clearingRows.size();
            if (lines >= 2) {
                java.util.List<int[]> attackRows = new java.util.ArrayList<>();
                for (int r : attacker.clearingRows)
                    attackRows.add(attacker.boardModel.getRowForAttack(r));
                client.sendCommand(new AttackCommand(attackRows));
                if (getRemotePlayer() != null) {
                    for (int[] r : attackRows)
                        getRemotePlayer().attackModel.push(r);
                    getRemotePlayer().renderer.renderAttackBoard(getRemotePlayer().attackModel.getAttacks());
                }
            }
        }
    }

    @Override
    public void gameStart(MatchSettings settings) {
        Platform.runLater(() -> {
            if (player1 != null && player2 != null) {
                currentSettings = settings;
                model.setPlayerNumber(settings.getPlayerNumber());
                if (settings.getPlayerNumber() == 2) {
                    if (restartButton != null)
                        restartButton.setVisible(false);
                    if (resumeButton != null)
                        resumeButton.setVisible(false);
                }
                stopGame();
                isGameOver = false;
                lastUpdate = 0L;
                firstTriggered = false;

                model.getPlayer1GameModel().setNextBlockSeed(settings.getMySeed());
                model.getPlayer2GameModel().setNextBlockSeed(settings.getOtherSeed());

                model.getPlayer1GameModel().reset();
                model.getPlayer2GameModel().reset();
                player1.reset();
                player2.reset();
                localBlockCount = 1;
                remoteBlockCount = 1;
                pauseOwner = 0;
                opponentDisconnected = false;
                player1.gameModel.setPaused(false);
                player2.gameModel.setPaused(false);
                hideGameOverlay();
                hidePauseOverlay();
                hideDisconnectOverlay();
                firstTriggered = true;
                gameStarted = true;
                startGameLoop();
                startPingTimeoutTimer();
            }
        });
    }

    @Override
    public void gameOver(int score, java.util.List<int[]> pendingAttacks) {
        Platform.runLater(() -> {
            if (!isGameOver) {
                isGameOver = true;
                
                // 상대방이 가지고 있던 공격 데이터를 내 attackModel에 추가
                PlayerSlot remotePlayer = getRemotePlayer();
                if (remotePlayer != null && pendingAttacks != null) {
                    remotePlayer.attackModel.reset();
                    for (int[] attack : pendingAttacks) {
                        remotePlayer.attackModel.push(attack);
                    }
                    remotePlayer.renderer.renderAttackBoard(remotePlayer.attackModel.getAttacks());
                }
                
                stopGame();
                showGameOverDialog("Game Over", "You Won!\nOpponent Score: " + score);
            }
        });
    }

    @Override
    public void onGameResult(boolean isWinner, int score) {
        Platform.runLater(() -> {
            stopGame();
            showGameOverDialog("Game Over",
                    (isWinner ? (isHost() ? "Host Win!" : "Guest Win!") : (isHost() ? "Guest Win!" : "Host Win!"))
                            + "\nYour Score: " + score);
        });
    }

    @Override
    protected void goToMenu() {
        if (client != null)
            client.disconnectGracefully();
        cleanupNetworkResources();
        resetGameController();
        hideGameOverlay();
        if (router != null)
            router.showNetworkMenu(false);
    }

    public void pause() {
        Platform.runLater(() -> {
            if (player1 != null) {
                player1.gameModel.setPaused(true);
                player2.gameModel.setPaused(true);
                showPauseOverlay();
                pauseOwner = (model.getPlayerNumber() == 1 ? 2 : 1);
            }
        });
    }

    @Override
    public void resume() {
        Platform.runLater(() -> {
            if (player1 != null) {
                player1.gameModel.setPaused(false);
                player2.gameModel.setPaused(false);
                hidePauseOverlay();
                pauseOwner = 0;
            }
        });
    }

    @Override
    public void onOpponentDisconnect(String r) {
        if (!opponentDisconnected) {
            opponentDisconnected = true;
            Platform.runLater(() -> {
                stopGame();
                showDisconnectOverlay(r);
            });
        }
    }

    @Override
    public void updateState(String s) {
    }

    @Override
    public void updatePing(long p) {
        lastPingTime = System.currentTimeMillis();
        Platform.runLater(() -> {
            if (myPingLabel != null) {
                myPingLabel.setText(p + " ms");
                myPingLabel.setStyle("-fx-text-fill: " + (p < 50 ? "#4CAF50" : p < 100 ? "#FFC107" : "#F44336"));
            }
        });
        if (client != null)
            client.sendCommand(new PingInfoCommand(p));
    }

    @Override
    public void updateOpponentPing(long p) {
        Platform.runLater(() -> {
            if (opponentPingLabel != null) {
                opponentPingLabel.setText(p + " ms");
                opponentPingLabel.setStyle("-fx-text-fill: " + (p < 50 ? "#4CAF50" : p < 100 ? "#FFC107" : "#F44336"));
            }
        });
    }

    private void showGameOverDialog(String t, String m) {
        if (winnerLabel != null)
            winnerLabel.setText(m);
        showGameOverlay();
    }
}