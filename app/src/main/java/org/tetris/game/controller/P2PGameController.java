package org.tetris.game.controller;

import org.tetris.network.comand.*;
import org.tetris.network.GameClient;
import org.tetris.network.GameServer;
import org.tetris.game.model.P2PGameModel;
import org.tetris.game.model.PlayerSlot;

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

    @FXML
    private Button replayButton;

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
        updateTimeAttackVisibility();
        // 연결 끊김 오버레이의 메인 메뉴 버튼 설정
        Platform.runLater(() -> {
            if (disconnectMenuButton != null) {
                disconnectMenuButton.setOnAction(e -> goToMainMenuFromDisconnect());
            }
            if (replayButton != null) {
                replayButton.setOnAction(e -> onReplayButtonClicked());
                replayButton.setVisible(false); // 초기에는 숨김
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

    private void updateControlLabels() {
        KeyCode leftKey = KeyLayout.getLeftKey(PlayerId.PLAYER1);
        KeyCode rightKey = KeyLayout.getRightKey(PlayerId.PLAYER1);
        KeyCode upKey = KeyLayout.getUpKey(PlayerId.PLAYER1);
        KeyCode downKey = KeyLayout.getDownKey(PlayerId.PLAYER1);
        KeyCode hardDropKey = KeyLayout.getHardDropKey(PlayerId.PLAYER1);

        if (moveControlLabel1 != null) {
            moveControlLabel1.setText("Move: " + leftKey.getName() + " / " + rightKey.getName());
        }
        if (rotateControlLabel1 != null) {
            rotateControlLabel1.setText("Rotate: " + upKey.getName());
        }
        if (softDropControlLabel1 != null) {
            softDropControlLabel1.setText("Down: " + downKey.getName());
        }
        if (hardDropControlLabel1 != null) {
            hardDropControlLabel1.setText("Drop: " + hardDropKey.getName());
        }
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
        PlayerSlot localPlayer = getLocalPlayer();

        if(localPlayer == null || localPlayer.gameModel.isGameOver()){
            e.consume();
            return;
        }

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
        if (localPlayer.gameModel.isPaused()) {
            return;
        }

        // 플래시 애니메이션 도중에는 입력 무시
        if (localPlayer.isFlashing) {
            e.consume();
            return;
        }

        KeyCode code = e.getCode();
        String action = mapKeyToAction(code);

        if (action != null) {
            // 1. 로컬 시퀀스 부여
            long localSeq = ++localSeqCounter;
            InputCommand cmd = new InputCommand(model.getPlayerNumber(), localSeq, action, gameStartTime);

            // 2. 서버에 전송
            client.sendCommand(cmd);

            // 3. 입력 로그에 저장 (네트워크 동기화용)
            inputLog.addLast(cmd);
            if (inputLog.size() > INPUT_LOG_SIZE) {
                inputLog.removeFirst();
            }
            
            // 4. Replay용 로그에도 저장
            player1InputLog.addLast(cmd);
            if (player1InputLog.size() > INPUT_LOG_SIZE) {
                player1InputLog.removeFirst();
            }

            // 5. 낙관적 로컬 적용 (즉시 반영)
            applyInputLocally(localPlayer, action);

            e.consume();
        }
    }

    /**
     * 키를 액션 문자열로 매핑
     */
    private String mapKeyToAction(KeyCode code) {
        if (code == KeyLayout.getLeftKey(PlayerId.PLAYER1)) return "moveLeft";
        if (code == KeyLayout.getRightKey(PlayerId.PLAYER1)) return "moveRight";
        if (code == KeyLayout.getUpKey(PlayerId.PLAYER1)) return "rotate";
        if (code == KeyLayout.getDownKey(PlayerId.PLAYER1)) return "softDrop";
        if (code == KeyLayout.getHardDropKey(PlayerId.PLAYER1)) return "hardDrop";
        return null;
    }

    /**
     * 로컬 플레이어에게 입력을 즉시 적용 (낙관적 업데이트)
     */
    private void applyInputLocally(PlayerSlot player, String action) {
        switch (action) {
            case "moveLeft":
                player.boardModel.moveLeft();
                updateGameBoard(player);
                break;
            case "moveRight":
                player.boardModel.moveRight();
                updateGameBoard(player);
                break;
            case "rotate":
                player.boardModel.rotate();
                updateGameBoard(player);
                break;
            case "softDrop":
                if (player.boardModel.moveDown()) {
                    player.scoreModel.softDrop(1);
                }
                updateGameBoard(player);
                break;
            case "hardDrop":
                handleHardDrop(player);
                return; // hardDrop은 별도 처리
        }
        player.renderer.renderNextBlock(player.nextBlockModel.peekNext());
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
            // 게임 오버 상태일 때만 리플레이 데이터 생성
            if (isGameOver) {
                if (gameLoop != null) {
                    gameLoop.stop();
                }
                hideGameOverlay(); // 게임 오버 오버레이가 표시되어 있다면 숨김

                // Replay 데이터 생성
                if (player1 != null && player2 != null) {
                    long gameDuration = System.currentTimeMillis() - gameStartTime;

                    // 승자 판정: player1은 로컬(ME), player2는 상대(OPPONENT)
                    boolean iLost = player1.gameModel.isGameOver();
                    boolean opponentLost = player2.gameModel.isGameOver();
                    boolean meWon;
                    
                    if (iLost && !opponentLost) {
                        meWon = false; // 내가 졌음
                    } else if (!iLost && opponentLost) {
                        meWon = true; // 내가 이김
                    } else {
                        // 둘 다 게임오버 -> 점수 비교
                        meWon = player1.scoreModel.getScore() > player2.scoreModel.getScore();
                    }

                    // Replay 데이터 생성 (currentSettings는 gameStart에서 저장한 것)
                    if (currentSettings != null) {
                        int myScore = player1.scoreModel.getScore();
                        int opponentScore = player2.scoreModel.getScore();
                        
                        lastReplayData = new org.tetris.game.model.ReplayData(
                                currentSettings,
                                new java.util.ArrayList<>(player1InputLog),  // 내 입력
                                new java.util.ArrayList<>(player2InputLog),  // 상대 입력
                                gameDuration,
                                myScore,          // 내 최종 점수
                                opponentScore,    // 상대 최종 점수
                                meWon             // 내가 이겼는지
                        );

                        System.out.println("[REPLAY] Captured - myInputs: " + player1InputLog.size() +
                                ", opponentInputs: " + player2InputLog.size() +
                                ", myScore: " + myScore + ", opponentScore: " + opponentScore +
                                ", meWon: " + meWon);
                    }
                }
            }

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

    // --- GameCommandExecutor Implementation (Remote Player Updates) ---

    @Override
    public void moveLeft() {
        Platform.runLater(() -> {
            PlayerSlot remotePlayer = getRemotePlayer();
            System.out.println("[P2P-CONTROLLER] moveLeft() - playerNumber=" + model.getPlayerNumber()
                    + ", remotePlayer=" + (remotePlayer != null ? "exists" : "null"));
            if (remotePlayer != null) {
                remotePlayer.boardModel.moveLeft();
                updateUI();
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
                updateUI();
            }
        });
    }

    @Override
    public void rotate() {
        Platform.runLater(() -> {
            PlayerSlot remotePlayer = getRemotePlayer();
            if (remotePlayer != null) {
                remotePlayer.boardModel.rotate();
                updateUI();
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
                updateUI();
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
        Platform.runLater(() -> {
            // 게임 시작 시 상태 초기화
            isGameOver = false;
            if (player1 != null) player1.gameModel.setPaused(false);
            if (player2 != null) player2.gameModel.setPaused(false);
            pauseOwner = 0;
            opponentDisconnected = false;

            // 시퀀스 초기화
            localSeqCounter = 0;
            lastConfirmedGlobalSeq = 0;
            inputLog.clear();
            lastSnapshot = null;

            // Replay 데이터 초기화
            player1InputLog.clear();
            player2InputLog.clear();
            gameStartTime = System.currentTimeMillis();
            lastReplayData = null;
            currentSettings = settings;

            if (player1 != null && player2 != null) {
                System.out.println("[P2P-CONTROLLER] gameStart() received");

                // Set player number to determine local/remote mapping
                model.setPlayerNumber(settings.getPlayerNumber());

                System.out.println("[P2P-CONTROLLER] I am Player " + settings.getPlayerNumber());
                System.out.println("[P2P-CONTROLLER] My seed: " + settings.getMySeed() +
                        ", Other seed: " + settings.getOtherSeed());

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
                }

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

                // 게임 모드 설정 (TIME_ATTACK, ITEM 등)
                setUpGameMode(settings.getGameMode());
                System.out.println("[P2P-CONTROLLER] GameMode set to: " + settings.getGameMode());

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
            PlayerSlot localPlayer = getLocalPlayer();
            PlayerSlot remotePlayer = getRemotePlayer();

            if(localPlayer == null || remotePlayer == null){
                return;
            }

            localPlayer.gameModel.setGameOver(true);
            remotePlayer.gameModel.setGameOver(true);

            showGameOverDialog("Defeat", "You Lost\nMy Score: " + score + "\nOpponent Score: " + remotePlayer.scoreModel.getScore());
        });
    }

    /**
     * [수정됨] 서버로부터 게임 결과를 수신했을 때 (승리 또는 패배)
     */
    @Override
    public void onGameResult(boolean isWinner, int score) {
        Platform.runLater(() -> {
            stopGame();
            
            // Replay 데이터 생성
            createReplayData(isWinner);
            
            // 서버 결과에 따라 메시지 분기
            PlayerSlot localPlayer = getLocalPlayer();
            PlayerSlot remotePlayer = getRemotePlayer();

            if (localPlayer == null || remotePlayer == null) {
                return;
            }

            localPlayer.gameModel.setGameOver(true);
            remotePlayer.gameModel.setGameOver(true);

            String title = isWinner ? "Victory!" : "Defeat";
            String message = (isWinner ? "You Won!" : "You Lost") +"\nMy Score: " + localPlayer.scoreModel.getScore() + "\nOpponent Score: " + score;

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
     * 호스트인 경우 주기적으로 스냅샷도 전송합니다.
     */
    @Override
    protected void onBlockLocked(PlayerSlot player) {
        // 로컬 플레이어(나)의 블록이 고정된 경우에만 전송
        if (player == getLocalPlayer()) {
            int[][] myBoard = player.boardModel.getBoard();
            Point myPos = player.boardModel.getCurPos();

            // 상태 전송 (dev 브랜치: score 포함)
            client.sendCommand(new UpdateStateCommand(myBoard, myPos.r, myPos.c, player.scoreModel.getScore()));
            
            // 호스트인 경우: 주기적으로 스냅샷 전송
            if (isHost()) {
                localBlockCount++;
                if (localBlockCount % 10 == 0) { // 10블록마다
                    sendSnapshotToServer();
                }
            }
        }
    }

    /**
     * 현재 게임 상태의 스냅샷을 서버에 전송 (호스트만)
     */
    private void sendSnapshotToServer() {
        PlayerSlot local = getLocalPlayer();
        PlayerSlot remote = getRemotePlayer();

        if (local == null || remote == null) {
            return;
        }

        // 현재 보드 상태 캡처
        int[][] localBoard = deepCopyBoard(local.boardModel.getBoard());
        int[][] remoteBoard = deepCopyBoard(remote.boardModel.getBoard());

        // 스냅샷 생성 (player1이 로컬, player2가 원격)
        SnapshotCommand snapshot = new SnapshotCommand(
                lastConfirmedGlobalSeq,
                localBoard,
                remoteBoard,
                local.scoreModel.getScore(),
                remote.scoreModel.getScore(),
                0, 0 // RNG seed는 향후 구현
        );

        client.sendCommand(snapshot);
        System.out.println("[P2P-HOST] Sent snapshot at globalSeq=" + lastConfirmedGlobalSeq +
                ", blockCount=" + localBlockCount);
    }

    /**
     * 보드 배열의 깊은 복사
     */
    private int[][] deepCopyBoard(int[][] src) {
        if (src == null) {
            return null;
        }
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = src[i].clone();
        }
        return copy;
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

    @Override
    protected void checkGameOverState() {
        // 부모의 checkGameOverState()를 호출하지 않음으로써
        // "PLAYER 2 WINS" 같은 기본 텍스트가 뜨는 것을 방지합니다.
        // 대신 로컬 플레이어(나)의 게임오버만 감지하여 서버에 패배 사실을 알립니다.
        if (player1 != null && player1.gameModel.isGameOver()) {
            // 아직 게임오버 처리가 안 된 상태라면
            if (!disconnectOverlay.isVisible() && !gameOverOverlay.isVisible()) {
                updateUI();
                
                // Replay 데이터 생성 (게임 오버 시점에서)
                if (lastReplayData == null && player2 != null && currentSettings != null) {
                    createReplayData(false); // 내가 졌음
                }
                
                gameOver(player1.scoreModel.getScore());
                client.sendCommand(new GameResultCommand(true, player1.scoreModel.getScore()));
            }
        }
    }
    
    /**
     * Replay 데이터를 생성합니다.
     * @param meWon 내가 이겼는지 여부
     */
    private void createReplayData(boolean meWon) {
        if (lastReplayData != null || player1 == null || player2 == null || currentSettings == null) {
            return;
        }
        
        long gameDuration = System.currentTimeMillis() - gameStartTime;
        int myScore = player1.scoreModel.getScore();
        int opponentScore = player2.scoreModel.getScore();
        
        lastReplayData = new org.tetris.game.model.ReplayData(
                currentSettings,
                new java.util.ArrayList<>(player1InputLog),  // 내 입력
                new java.util.ArrayList<>(player2InputLog),  // 상대 입력
                gameDuration,
                myScore,          // 내 최종 점수
                opponentScore,    // 상대 최종 점수
                meWon             // 내가 이겼는지
        );
        System.out.println("[REPLAY] Captured - myInputs: " + player1InputLog.size() +
                           ", opponentInputs: " + player2InputLog.size() +
                           ", myScore: " + myScore + ", opponentScore: " + opponentScore +
                           ", meWon: " + meWon);
                           
        // Replay 버튼 활성화
        Platform.runLater(() -> {
            if (replayButton != null) {
                replayButton.setVisible(true);
                replayButton.setManaged(true);
            }
        });
    }

    private void onReplayButtonClicked() {
        if (lastReplayData != null && router != null) {
            router.showReplay(lastReplayData);
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

    // ===== 시퀀싱 시스템 필드 =====
    private long localSeqCounter = 0; // 내가 보낸 입력의 로컬 시퀀스
    private long lastConfirmedGlobalSeq = 0; // 마지막으로 확인된 전역 시퀀스

    // 입력 로그 (최근 1000개 유지)
    private static final int INPUT_LOG_SIZE = 1000;
    private final java.util.Deque<InputCommand> inputLog = new java.util.ArrayDeque<>(INPUT_LOG_SIZE);

    // 스냅샷 캐시 (가장 최근 것만 유지)
    private SnapshotCommand lastSnapshot = null;

    // 블록 고정 카운터 (호스트만 사용)
    private int localBlockCount = 0;

    // ===== Replay System Fields =====
    private long gameStartTime;
    private final java.util.Deque<InputCommand> player1InputLog = new java.util.ArrayDeque<>();
    private final java.util.Deque<InputCommand> player2InputLog = new java.util.ArrayDeque<>();
    private org.tetris.game.model.ReplayData lastReplayData;
    private MatchSettings currentSettings; // 게임 시작 시 설정 저장

    /**
     * GameCommandExecutor.executeInput() 구현
     * 서버가 globalSeq를 부여한 InputCommand를 수신했을 때 호출됨
     */
    @Override
    public void executeInput(InputCommand cmd) {
        Platform.runLater(() -> {
            if (cmd.getGlobalSeq() <= lastConfirmedGlobalSeq) {
                // 이미 처리한 시퀀스 (중복 또는 롤백 후 재적용 완료)
                return;
            }

            // 시퀀스 업데이트
            lastConfirmedGlobalSeq = cmd.getGlobalSeq();

            // 내 입력인 경우: 이미 낙관적으로 적용했으므로 확정만 함
            if (cmd.getPlayerNumber() == model.getPlayerNumber()) {
                System.out.println("[P2P] My input confirmed: globalSeq=" + cmd.getGlobalSeq());
                // 입력 로그에서 globalSeq 업데이트
                updateInputLogWithGlobalSeq(cmd, inputLog); // 기존 inputLog 업데이트
                updateInputLogWithGlobalSeq(cmd, player1InputLog); // Replay용 로그 업데이트
                return;
            }

            // 상대방 입력인 경우: 원격 플레이어에게 적용
            PlayerSlot remotePlayer = getRemotePlayer();
            if (remotePlayer != null) {
                applyInputToPlayer(remotePlayer, cmd.getAction());
                // 상대방 입력은 player2 로그에 저장
                player2InputLog.addLast(cmd);
                if (player2InputLog.size() > INPUT_LOG_SIZE) {
                    player2InputLog.removeFirst();
                }
            }
        });
    }

    /**
     * 입력 로그에서 해당 로컬 시퀀스를 찾아 globalSeq 업데이트
     */
    private void updateInputLogWithGlobalSeq(InputCommand confirmedCmd, java.util.Deque<InputCommand> log) {
        for (InputCommand logged : log) {
            if (logged.getPlayerNumber() == confirmedCmd.getPlayerNumber() &&
                    logged.getLocalSeq() == confirmedCmd.getLocalSeq()) {
                logged.setGlobalSeq(confirmedCmd.getGlobalSeq());
                break;
            }
        }
    }

    /**
     * 특정 플레이어에게 액션 적용
     */
    private void applyInputToPlayer(PlayerSlot player, String action) {
        switch (action) {
            case "moveLeft":
                player.boardModel.moveLeft();
                break;
            case "moveRight":
                player.boardModel.moveRight();
                break;
            case "rotate":
                player.boardModel.rotate();
                break;
            case "softDrop":
                if (player.boardModel.moveDown()) {
                    player.scoreModel.softDrop(1);
                }
                break;
            case "hardDrop":
                int d = player.boardModel.hardDrop();
                player.scoreModel.add(d * 2);
                break;
        }
        updateGameBoard(player);
    }

    /**
     * GameCommandExecutor.restoreSnapshot() 구현
     * 서버로부터 스냅샷을 받아 상태 복원 + 이후 입력 재적용
     */
    @Override
    public void restoreSnapshot(SnapshotCommand snapshot) {
        Platform.runLater(() -> {
            System.out.println("[P2P] Received snapshot at globalSeq=" +
                    snapshot.getAuthoritativeSeq());

            // 1. 스냅샷이 현재보다 오래된 것이면 무시
            if (snapshot.getAuthoritativeSeq() <= lastConfirmedGlobalSeq) {
                return;
            }

            // 2. 스냅샷 캐시에 저장
            lastSnapshot = snapshot;

            // 3. 상태 복원
            restoreGameState(snapshot);

            // 4. 스냅샷 이후의 입력들을 재적용 (Rollback & Replay)
            replayInputsAfter(snapshot.getAuthoritativeSeq());

            // 5. 시퀀스 업데이트
            lastConfirmedGlobalSeq = snapshot.getAuthoritativeSeq();
        });
    }

    /**
     * 스냅샷으로 게임 상태 복원
     */
    private void restoreGameState(SnapshotCommand snapshot) {
        PlayerSlot localPlayer = getLocalPlayer();
        PlayerSlot remotePlayer = getRemotePlayer();

        if (localPlayer != null) {
            // 로컬 플레이어 상태 복원
            int[][] localBoard = (model.getPlayerNumber() == 1)
                    ? snapshot.getPlayer1Board()
                    : snapshot.getPlayer2Board();
            
            // 보드 데이터 복사
            int[][] currentBoard = localPlayer.boardModel.getBoard();
            for (int i = 0; i < localBoard.length && i < currentBoard.length; i++) {
                System.arraycopy(localBoard[i], 0, currentBoard[i], 0, 
                        Math.min(localBoard[i].length, currentBoard[i].length));
            }

            int localScore = (model.getPlayerNumber() == 1)
                    ? snapshot.getPlayer1Score()
                    : snapshot.getPlayer2Score();
            localPlayer.scoreModel.setScore(localScore);
        }

        if (remotePlayer != null) {
            // 원격 플레이어 상태 복원
            int[][] remoteBoard = (model.getPlayerNumber() == 1)
                    ? snapshot.getPlayer2Board()
                    : snapshot.getPlayer1Board();
            
            // 보드 데이터 복사
            int[][] currentBoard = remotePlayer.boardModel.getBoard();
            for (int i = 0; i < remoteBoard.length && i < currentBoard.length; i++) {
                System.arraycopy(remoteBoard[i], 0, currentBoard[i], 0,
                        Math.min(remoteBoard[i].length, currentBoard[i].length));
            }

            int remoteScore = (model.getPlayerNumber() == 1)
                    ? snapshot.getPlayer2Score()
                    : snapshot.getPlayer1Score();
            remotePlayer.scoreModel.setScore(remoteScore);
        }

        updateUI();
    }

    /**
     * 스냅샷 이후의 입력들을 순서대로 재적용 (Replay)
     */
    private void replayInputsAfter(long snapshotSeq) {
        System.out.println("[P2P] Replaying inputs after globalSeq=" + snapshotSeq);

        for (InputCommand cmd : inputLog) {
            // globalSeq가 설정되지 않았거나, 스냅샷보다 이전 것은 스킵
            if (cmd.getGlobalSeq() <= snapshotSeq) {
                continue;
            }

            // 내 입력인 경우: 로컬 플레이어에게 재적용
            if (cmd.getPlayerNumber() == model.getPlayerNumber()) {
                applyInputToPlayer(getLocalPlayer(), cmd.getAction());
            }
            // 상대 입력은 executeInput에서 이미 적용되었으므로 스킵
        }
    }
}
