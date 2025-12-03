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
import org.tetris.network.dto.MatchSettings;
import org.util.KeyLayout;
import org.util.PlayerId;
import org.util.Point;

import java.util.Random;

public class P2PGameController extends DualGameController<P2PGameModel>
        implements GameCommandExecutor {

    // FXML 바인딩 - 연결 끊김 오버레이
    @FXML
    private HBox disconnectOverlay;
    @FXML
    private Label disconnectReasonLabel;
    @FXML
    private Button disconnectMenuButton;

    // FXML 바인딩 - Ping 레이블
    @FXML
    private Label myPingLabel;
    @FXML
    private Label opponentPingLabel;

    private GameClient client;

    // 상대방 연결 끊김 플래그
    private volatile boolean opponentDisconnected = false;

    // 일시정지 권한 관리: 누가 일시정지했는지 추적 (0: 없음, 1: 호스트, 2: 클라이언트)
    private volatile int pauseOwner = 0;

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

        // 연결 끊김 오버레이의 메인 메뉴 버튼 설정
        Platform.runLater(() -> {
            if (disconnectMenuButton != null) {
                disconnectMenuButton.setOnAction(e -> goToMainMenuFromDisconnect());
            }
        });
    }

    /**
     * 로컬 플레이어 슬롯 반환
     * 항상 player1 (오른쪽, ME 위치)을 로컬 플레이어로 사용
     */
    private PlayerSlot getLocalPlayer() {
        return player1;
    }

    /**
     * 원격 플레이어 슬롯 반환
     * 항상 player2 (왼쪽, OPPONENT 위치)를 원격 플레이어로 사용
     */
    private PlayerSlot getRemotePlayer() {
        return player2;
    }

    /**
     * 현재 플레이어가 호스트인지 확인
     */
    private boolean isHost() {
        return model.getPlayerNumber() == 1;
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        // P2P 모드에서 P 키로 일시정지
        if (e.getCode() == KeyCode.P) {
            if (isHost()) {
                // 호스트: 게임을 실제로 일시정지하고 상대방에게 동기화
                togglePauseAndSync();
            } else {
                // 클라이언트: 게임은 멈추지 않고 메뉴만 표시 (나가기 용도)
                toggleClientPauseMenu();
            }
            e.consume();
            return;
        }

        // 로컬 플레이어의 입력만 처리하고 서버로 커맨드 전송
        PlayerSlot localPlayer = getLocalPlayer();
        if (localPlayer == null || localPlayer.gameModel.isPaused() || localPlayer.gameModel.isGameOver()) {
            return;
        }

        // 플래시 애니메이션 도중에는 입력 무시
        if (localPlayer.isFlashing) {
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

    /**
     * PAUSE 버튼 클릭 시 호출되는 메서드 오버라이드
     * P2P 모드에서는 호스트/클라이언트에 따라 다르게 동작
     */
    @Override
    protected void togglePause() {
        if (isHost()) {
            // 호스트: 게임을 실제로 일시정지하고 상대방에게 동기화
            togglePauseAndSync();
        } else {
            // 클라이언트: 게임은 멈추지 않고 메뉴만 표시 (나가기 용도)
            toggleClientPauseMenu();
        }
    }

    /**
     * P2P 모드에서 일시정지 토글 및 상대방에게 동기화
     * 일시정지를 누른 사람만 해제 가능
     */
    private void togglePauseAndSync() {
        if (player1 == null || player2 == null) {
            return;
        }

        boolean currentlyPaused = player1.gameModel.isPaused();
        int myPlayerNumber = model.getPlayerNumber();

        if (currentlyPaused) {
            // 일시정지 해제 시도 - 본인이 일시정지한 경우에만 해제 가능
            if (pauseOwner != myPlayerNumber) {
                System.out.println("[P2P-CONTROLLER] Cannot resume: pause was initiated by player " + pauseOwner);
                return;
            }

            // 일시정지 해제
            player1.gameModel.setPaused(false);
            player2.gameModel.setPaused(false);
            hidePauseOverlay();
            pauseOwner = 0;

            // 상대방에게 일시정지 해제 전송
            client.sendCommand(new PauseCommand(false));
            System.out.println("[P2P-CONTROLLER] Pause released by player " + myPlayerNumber);
        } else {
            // 일시정지 시작
            player1.gameModel.setPaused(true);
            player2.gameModel.setPaused(true);
            showPauseOverlay();
            pauseOwner = myPlayerNumber;

            // 상대방에게 일시정지 상태 전송
            client.sendCommand(new PauseCommand(true));
            System.out.println("[P2P-CONTROLLER] Pause initiated by player " + myPlayerNumber);
        }
    }

    /**
     * 클라이언트용 일시정지 메뉴 토글
     * 게임은 멈추지 않고 메뉴만 표시/숨김 (나가기 용도)
     */
    private void toggleClientPauseMenu() {
        if (pauseOverlay == null) {
            return;
        }

        if (pauseOverlay.isVisible()) {
            // 메뉴 숨기기
            hidePauseOverlay();
            System.out.println("[P2P-CONTROLLER] Client pause menu hidden");
        } else {
            // 메뉴 표시 (게임은 계속 진행)
            showPauseOverlay();
            System.out.println("[P2P-CONTROLLER] Client pause menu shown (game continues)");
        }
    }

    /**
     * 연결 끊김 시 메인 메뉴로 이동
     */
    private void goToMainMenuFromDisconnect() {
        // 네트워크 리소스 정리
        cleanupNetworkResources();

        // 오버레이 숨기기
        hideDisconnectOverlay();

        // 메인 메뉴로 이동
        if (router != null) {
            router.showStartMenu();
        }
    }

    /**
     * 네트워크 리소스 정리
     */
    private void cleanupNetworkResources() {
        try {
            // 클라이언트 연결 종료
            if (client != null) {
                client.disconnect();
            }

            // 호스트인 경우 서버도 중지
            if (isHost()) {
                GameServer.getInstance().stop();
            }
        } catch (Exception e) {
            System.err.println("[P2P-CONTROLLER] Error cleaning up network resources: " + e.getMessage());
        }
    }

    /**
     * 연결 끊김 오버레이 표시
     */
    private void showDisconnectOverlay(String reason) {
        if (disconnectOverlay != null) {
            if (disconnectReasonLabel != null) {
                disconnectReasonLabel.setText(reason);
            }
            disconnectOverlay.setVisible(true);
            disconnectOverlay.setManaged(true);
        }
    }

    /**
     * 연결 끊김 오버레이 숨기기
     */
    private void hideDisconnectOverlay() {
        if (disconnectOverlay != null) {
            disconnectOverlay.setVisible(false);
            disconnectOverlay.setManaged(false);
        }
    }

    /**
     * 게임 정리 (화면 전환 시 호출)
     */
    @Override
    public void refresh() {

    }

    /**
     * P2P 모드에서 Resume 버튼 클릭 처리
     * 호스트: 게임 resume 및 상대방에게 동기화
     * 클라이언트: 메뉴만 닫기 (게임은 계속 진행 중)
     */
    @Override
    protected void onResumeButtonClicked() {
        if (player1 == null || player2 == null) {
            return;
        }

        if (!isHost()) {
            // 클라이언트: 메뉴만 닫기 (게임 상태는 변경하지 않음)
            hidePauseOverlay();
            System.out.println("[P2P-CONTROLLER] Client resume button clicked - hiding menu only");
            return;
        }

        int myPlayerNumber = model.getPlayerNumber();

        // 호스트: 본인이 일시정지한 경우에만 해제 가능
        if (pauseOwner != myPlayerNumber) {
            System.out
                    .println("[P2P-CONTROLLER] Cannot resume via button: pause was initiated by player " + pauseOwner);
            return;
        }

        // 일시정지 해제
        player1.gameModel.setPaused(false);
        player2.gameModel.setPaused(false);
        hidePauseOverlay();
        pauseOwner = 0;

        // 상대방에게 일시정지 해제 전송
        client.sendCommand(new PauseCommand(false));
        System.out.println("[P2P-CONTROLLER] Resume button clicked - pause released by player " + myPlayerNumber);
    }

    /**
     * P2P 모드에서 Restart 버튼 클릭 처리
     * 서버에 RestartCommand를 전송하여 새 seed로 게임 재시작
     */
    @Override
    protected void onRestartButtonClicked() {
        System.out.println("[P2P-CONTROLLER] Restart requested - sending RestartCommand to server");

        // 게임 오버 오버레이 숨기기
        hideGameOverlay();

        // 서버에 재시작 요청 전송
        client.sendCommand(new RestartCommand());
    }

    @Override
    protected void checkGameOverState() {
        // 부모의 checkGameOverState()를 호출하지 않음으로써
        // "PLAYER 2 WINS" 같은 기본 텍스트가 뜨는 것을 방지합니다.
        // 대신 로컬 플레이어(나)의 게임오버만 감지하여 서버에 패배 사실을 알립니다.
        if (player1 != null && player1.gameModel.isGameOver()) {
            // 아직 게임오버 처리가 안 된 상태라면
            if (!disconnectOverlay.isVisible() && !gameOverOverlay.isVisible()) {
                updateUI();
                gameOver(player1.scoreModel.getScore());
                client.sendCommand(new GameResultCommand(true, player1.scoreModel.getScore()));
            }
        }
    }

    // --- GameCommandExecutor Implementation (Remote Player Updates) ---

    @Override
    public void moveLeft() {
        Platform.runLater(() -> {
            PlayerSlot remotePlayer = getRemotePlayer();
            System.out.println("[P2P-CONTROLLER] moveLeft() - playerNumber=" + model.getPlayerNumber()
                    + ", remotePlayer=" + (remotePlayer != null ? "exists" : "null"));
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
            System.out.println("[P2P-CONTROLLER] moveRight() - playerNumber=" + model.getPlayerNumber()
                    + ", remotePlayer=" + (remotePlayer != null ? "exists" : "null"));
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
        // playerNumber는 즉시 설정 (isHost() 판단에 필요)
        model.setPlayerNumber(settings.getPlayerNumber());
        System.out.println("[P2P-CONTROLLER] gameStart() - I am Player " + settings.getPlayerNumber());

        Platform.runLater(() -> {
            System.out.println("[P2P-CONTROLLER] gameStart() Platform.runLater executing");

            // 클라이언트인 경우 restart, resume 버튼 숨기기
            if (settings.getPlayerNumber() == 2) {
                if (restartButton != null) {
                    restartButton.setVisible(false);
                    restartButton.setManaged(false);
                }
                if (resumeButton != null) {
                    resumeButton.setVisible(false);
                    resumeButton.setManaged(false);
                }
            } else {
                // 호스트인 경우 버튼 표시
                if (restartButton != null) {
                    restartButton.setVisible(true);
                    restartButton.setManaged(true);
                }
                if (resumeButton != null) {
                    resumeButton.setVisible(true);
                    resumeButton.setManaged(true);
                }
            }

            if (player1 != null && player2 != null) {
                System.out.println("[P2P-CONTROLLER] My seed: " + settings.getMySeed() +
                        ", Other seed: " + settings.getOtherSeed());

                // 기존 게임 루프 정리
                stopGame();

                // 게임 루프 변수 리셋
                lastUpdate = 0L;
                firstTriggered = false;

                // Initialize seeds for both players
                // player1은 항상 로컬(ME), player2는 항상 원격(OPPONENT)
                // 따라서 mySeed는 player1에, otherSeed는 player2에 설정
                model.getPlayer1GameModel().setNextBlockSeed(settings.getMySeed());
                model.getPlayer2GameModel().setNextBlockSeed(settings.getOtherSeed());

                // Reset both game models to apply new seeds
                model.getPlayer1GameModel().reset();
                model.getPlayer2GameModel().reset();

                // Reset player slots
                player1.reset();
                player2.reset();

                // 일시정지 소유자 초기화
                pauseOwner = 0;
                opponentDisconnected = false;

                // Unpause the game to start playing
                player1.gameModel.setPaused(false);
                player2.gameModel.setPaused(false);

                // 오버레이 숨기기
                hideGameOverlay();
                hidePauseOverlay();
                hideDisconnectOverlay();

                // firstTriggered 설정 후 게임 루프 재시작
                firstTriggered = true;
                startGameLoop();

                System.out.println("[P2P-CONTROLLER] Game started! Local player controls " +
                        (settings.getPlayerNumber() == 1 ? "left" : "right") + " screen.");
            }
        });
    }

    @Override
    public void gameOver(int score) {
        Platform.runLater(() -> {
            stopGame();
            // 로컬 플레이어(나)의 게임이 끝났으므로 패배(Defeat) 메시지 표시
            showGameOverDialog("Defeat", "You Lost\nMy Score: " + score);
        });
    }

    /**
     * [수정됨] 서버로부터 게임 결과를 수신했을 때 (승리 또는 패배)
     */
    @Override
    public void onGameResult(boolean isWinner, int score) {
        Platform.runLater(() -> {
            stopGame();
            // 서버 결과에 따라 메시지 분기
            String title = isWinner ? "Victory!" : "Defeat";
            String message = (isWinner ? "You Won!" : "You Lost") + "\nOpponent Score: " + score;

            showGameOverDialog(title, message);
        });
    }

    @Override
    protected void goToMenu() {
        resetGameController();
        hideGameOverlay();
        if (router != null)
            router.showNetworkMenu(isHost(), true); // 호스트 여부 전달
    }

    @Override
    protected void goToMenuFromPause() {
        cleanupNetworkResources();
        resetGameController();
        hideGameOverlay();
        if (router != null)
            router.showStartMenu();
    }

    public void pause() {
        System.out.println("[P2P-CONTROLLER] pause() called from network (opponent paused)");
        Platform.runLater(() -> {
            if (player1 != null && player2 != null) {
                // P2P에서는 상대방이 pause해도 내 게임은 멈추지 않음
                // 단, UI는 표시하여 상대방이 일시정지했음을 알림
                player1.gameModel.setPaused(true);
                player2.gameModel.setPaused(true);
                showPauseOverlay();

                // 상대방이 일시정지함 - pauseOwner는 상대방의 playerNumber
                int opponentPlayerNumber = model.getPlayerNumber() == 1 ? 2 : 1;
                pauseOwner = opponentPlayerNumber;

                System.out.println("[P2P-CONTROLLER] Paused by opponent (player " + opponentPlayerNumber + ")");
            } else {
                System.out.println("[P2P-CONTROLLER] pause() failed: player1=" + player1 + ", player2=" + player2);
            }
        });
    }

    @Override
    public void resume() {
        System.out.println("[P2P-CONTROLLER] resume() called from network");
        Platform.runLater(() -> {
            if (player1 != null && player2 != null) {
                player1.gameModel.setPaused(false);
                player2.gameModel.setPaused(false);
                hidePauseOverlay();
                pauseOwner = 0;
                System.out.println("[P2P-CONTROLLER] Resumed by opponent");
            } else {
                System.out.println("[P2P-CONTROLLER] resume() failed: player1=" + player1 + ", player2=" + player2);
            }
        });
    }

    @Override
    public void onOpponentDisconnect(String reason) {
        if (opponentDisconnected) {
            return; // 중복 처리 방지
        }
        opponentDisconnected = true;

        Platform.runLater(() -> {
            System.out.println("[P2P-CONTROLLER] Opponent disconnected: " + reason);
            stopGame();

            // 상대방 연결 끊김 전용 오버레이 표시
            showDisconnectOverlay(reason);
        });
    }

    /**
     * 상대방으로부터 수신한 보드 상태와 점수를 강제로 동기화합니다.
     * (UpdateStateCommand에 의해 호출됨)
     */
    @Override
    public void updateState(int[][] boardData, int currentPosRow, int currentPosCol, int score) {
        Platform.runLater(() -> {
            PlayerSlot remotePlayer = getRemotePlayer();
            if (remotePlayer != null) {
                // 1. 보드 데이터 덮어쓰기 (Correction)
                int[][] currentBoard = remotePlayer.boardModel.getBoard();
                if (boardData.length == currentBoard.length && boardData[0].length == currentBoard[0].length) {
                    for (int i = 0; i < boardData.length; i++) {
                        System.arraycopy(boardData[i], 0, currentBoard[i], 0, boardData[i].length);
                    }
                }
                remotePlayer.boardModel.setCurPos(new Point(currentPosRow, currentPosCol));
                remotePlayer.scoreModel.setScore(score);

                // 수신된 보드 상태를 기반으로 로컬 시뮬레이션(Attack, Line Clear 등)을 수행
                super.lockCurrentBlock(remotePlayer);

                // 3. 화면 갱신
                // updateGameBoard(remotePlayer); // lockCurrentBlock 내부에서 수행됨

                System.out.println("[P2P-SYNC] Remote board state corrected.");
            }
        });
    }

    /**
     * 블록 고정 로직 오버라이드
     * 원격 플레이어의 경우 로컬 시뮬레이션에 의한 블록 고정을 막습니다.
     * 오직 UpdateStateCommand 수신 시에만 블록을 고정하고 스폰합니다.
     */
    @Override
    protected void lockCurrentBlock(PlayerSlot player) {
        if (player == getRemotePlayer()) {
            return;
        }
        super.lockCurrentBlock(player);
    }

    /**
     * 내 블록이 고정될 때마다 현재 상태를 상대방에게 전송합니다.
     */
    @Override
    protected void onBlockLocked(PlayerSlot player) {
        // 로컬 플레이어(나)의 블록이 고정된 경우에만 전송
        if (player == getLocalPlayer()) {
            int[][] myBoard = player.boardModel.getBoard();
            Point myPos = player.boardModel.getCurPos();

            // 상태 전송
            client.sendCommand(new UpdateStateCommand(myBoard, myPos.r, myPos.c, player.scoreModel.getScore()));
            // System.out.println("[P2P-SYNC] Sent board state update.");
        }
    }

    @Override
    public void updatePing(long ping) {
        Platform.runLater(() -> {
            if (myPingLabel != null) {
                myPingLabel.setText(ping + " ms");
                // 색상 변경: 좋음(녹색), 보통(노랑), 나쁨(빨강)
                if (ping < 50) {
                    myPingLabel.setStyle("-fx-text-fill: #4CAF50;"); // 녹색
                } else if (ping < 100) {
                    myPingLabel.setStyle("-fx-text-fill: #FFC107;"); // 노랑
                } else {
                    myPingLabel.setStyle("-fx-text-fill: #F44336;"); // 빨강
                }
            }
        });

        // 상대방에게 내 ping 값 공유
        if (client != null) {
            client.sendCommand(new PingInfoCommand(ping));
        }
    }

    /**
     * 상대방 ping 업데이트
     */
    @Override
    public void updateOpponentPing(long ping) {
        Platform.runLater(() -> {
            if (opponentPingLabel != null) {
                opponentPingLabel.setText(ping + " ms");
                // 색상 변경
                if (ping < 50) {
                    opponentPingLabel.setStyle("-fx-text-fill: #4CAF50;");
                } else if (ping < 100) {
                    opponentPingLabel.setStyle("-fx-text-fill: #FFC107;");
                } else {
                    opponentPingLabel.setStyle("-fx-text-fill: #F44336;");
                }
            }
        });
    }

    private void showGameOverDialog(String title, String message) {
        if (winnerLabel != null) {
            winnerLabel.setText(message);
        }
        showGameOverlay();
    }
}
