package org.tetris.game.controller;

import org.tetris.Router;
import org.tetris.game.model.DualGameModel;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.model.ReplayData;
import org.tetris.network.comand.InputCommand;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.util.Iterator;
import java.util.List;

/**
 * Replay 전용 게임 컨트롤러
 * - 키보드 입력 차단 (P키 제외 - 일시정지만 허용)
 * - 저장된 입력 로그를 순서대로 재생
 * - 재생 속도 조절 가능 (1x, 2x, 4x)
 */
public class ReplayController extends DualGameController<DualGameModel> {

    @FXML
    private HBox replayControlOverlay; // Replay 전용 컨트롤 UI
    @FXML
    private Label replaySpeedLabel;
    @FXML
    private Button speedUpButton;
    @FXML
    private Button speedDownButton;
    @FXML
    private Button exitReplayButton;

    private ReplayData replayData;
    private Iterator<InputCommand> player1InputIterator;
    private Iterator<InputCommand> player2InputIterator;
    private InputCommand nextP1Input;
    private InputCommand nextP2Input;

    private long replayStartTime;
    private long accumulatedGameTime = 0; // 배속 변경을 위한 누적 게임 시간
    private double replaySpeedMultiplier = 1.0; // 0.5x, 1x, 2x, 4x
    private boolean replayFinished = false;

    public ReplayController(DualGameModel model) {
        super(model);
    }

    public void setReplayData(ReplayData replayData) {
        this.replayData = replayData;
    }

    @Override
    public void initialize() {
        // 부모의 initialize() 호출 (필수 필드 초기화)
        super.initialize();

        // Replay 전용 초기화 - super.initialize()의 Platform.runLater 이후에 실행되도록 함
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                setupReplayUI();
                // replayData가 이미 설정되어 있으면 초기화 진행
                // Router.showReplay()에서 initializeReplay()를 별도로 호출하므로 여기서는 호출하지 않음
            });
        });
    }

    @Override
    protected void setupEventHandlers() {
        // 부모의 이벤트 핸들러 설정 무시 (Replay 전용 핸들러 사용)
        // 단, 키보드 입력 처리는 필요하므로 setupKeyboardInput()은 호출하거나 직접 설정
        if (root.getScene() != null) {
            root.getScene().setOnKeyPressed(this::handleKeyPress);
        } else {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.setOnKeyPressed(this::handleKeyPress);
                }
            });
        }
    }

    private void setupReplayUI() {
        // Replay 컨트롤 오버레이 표시
        if (replayControlOverlay != null) {
            replayControlOverlay.setVisible(true);
            replayControlOverlay.setManaged(true);
        }

        // 속도 조절 버튼
        if (speedUpButton != null) {
            speedUpButton.setOnAction(e -> adjustSpeed(true));
        }
        if (speedDownButton != null) {
            speedDownButton.setOnAction(e -> adjustSpeed(false));
        }
        if (exitReplayButton != null) {
            exitReplayButton.setOnAction(e -> exitReplay());
        }

        updateSpeedLabel();

        // 게임 오버 오버레이 숨기기
        hideGameOverlay();
        hidePauseOverlay();
    }

    public void initializeReplay() {
        System.out.println("[REPLAY] initializeReplay() called");
        System.out.println("[REPLAY] player1=" + player1 + ", player2=" + player2);
        
        if (replayData == null || !replayData.isValid()) {
            System.err.println("[REPLAY] Invalid replay data: " + replayData);
            exitReplay();
            return;
        }

        // player1, player2가 초기화될 때까지 대기
        if (player1 == null || player2 == null) {
            System.out.println("[REPLAY] Players not initialized yet, retrying...");
            javafx.application.Platform.runLater(this::initializeReplay);
            return;
        }

        // 초기 설정 적용 (동일한 seed로 시작)
        dualGameModel.getPlayer1GameModel().setNextBlockSeed(replayData.getInitialSettings().getMySeed());
        dualGameModel.getPlayer2GameModel().setNextBlockSeed(replayData.getInitialSettings().getOtherSeed());

        dualGameModel.getPlayer1GameModel().reset();
        dualGameModel.getPlayer2GameModel().reset();

        player1.reset();
        player2.reset();

        // 입력 이터레이터 초기화
        List<InputCommand> p1Inputs = replayData.getPlayer1Inputs();
        List<InputCommand> p2Inputs = replayData.getPlayer2Inputs();

        player1InputIterator = p1Inputs.iterator();
        player2InputIterator = p2Inputs.iterator();

        nextP1Input = player1InputIterator.hasNext() ? player1InputIterator.next() : null;
        nextP2Input = player2InputIterator.hasNext() ? player2InputIterator.next() : null;

        replayStartTime = System.currentTimeMillis();
        accumulatedGameTime = 0; // 누적 시간 초기화
        replaySpeedMultiplier = 1.0; // 배속 초기화
        replayFinished = false;

        System.out.println("[REPLAY] Initialized with " + p1Inputs.size() + " P1 inputs, " +
                p2Inputs.size() + " P2 inputs");
        System.out.println("[REPLAY] First P1 input: " + nextP1Input);
        System.out.println("[REPLAY] First P2 input: " + nextP2Input);
        
        updateSpeedLabel();
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        // Replay 모드에서는 P키(일시정지)만 허용
        if (e.getCode() == javafx.scene.input.KeyCode.P) {
            togglePause();
            e.consume();
            return;
        }

        // 다른 모든 입력 차단
        e.consume();
        // System.out.println("[REPLAY] Keyboard input blocked in replay mode"); // 로그 너무 많을 수 있음
    }

    @Override
    protected void checkGameOverState() {
        // Replay 모드에서는 실제 게임오버 로직을 타지 않고, 입력 재생이 끝났을 때만 종료 처리
        // 단, Replay 데이터 상의 승자 정보는 나중에 표시
    }

    @Override
    protected void onBlockLocked(PlayerSlot player) {
        // Replay에서는 별도 동기화 필요 없음
    }

    // DualGameController.update(long now)는 private이라 오버라이드 불가
    // 하지만 startGameLoop() 내부의 익명 클래스에서 update()를 호출함.
    // DualGameController를 수정하여 update()를 protected로 만들거나,
    // startGameLoop()를 오버라이드해야 함.
    // 여기서는 startGameLoop()를 오버라이드하여 커스텀 루프를 돌리는 것이 안전함.

    @Override
    protected void startGameLoop() {
        gameLoop = new javafx.animation.AnimationTimer() {
            private static final long FRAME_TIME = 16_666_667L; // ~60 FPS

            @Override
            public void handle(long now) {
                if (player1 == null || player2 == null)
                    return;

                if (!firstTriggered) {
                    return;
                }

                if (lastUpdate == 0L) {
                    lastUpdate = now;
                    player1.lastDropTime = now;
                    player2.lastDropTime = now;
                    return;
                }

                long elapsed = now - lastUpdate;
                // 60FPS 제한 (약 16ms)
                if (elapsed >= FRAME_TIME) {
                    // Replay 입력 재생
                    updateReplay(now);
                    
                    // 렌더링 업데이트
                    updateReplayUI(now);
                    
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }
    
    /**
     * Replay 전용 UI 업데이트 로직
     * 블록 낙하, 이펙트, UI 렌더링을 처리합니다.
     */
    private void updateReplayUI(long now) {
        if (isPaused() || replayFinished)
            return;

        if (player1 == null || player2 == null)
            return;

        // Effects 애니메이션 업데이트
        player1.renderer.updateEffects(now);
        player2.renderer.updateEffects(now);

        // 플래시 애니메이션 처리 (Replay용 간소화 버전)
        tickReplayFlash(player1, now);
        tickReplayFlash(player2, now);

        // 블록 자동 낙하 처리 (Player 1) - 게임오버/플래시 상태가 아닌 경우에만
        if (!player1.gameModel.isGameOver() && !player1.isFlashing) {
            handleReplayPlayerDrop(player1, now);
        }
        
        // 블록 자동 낙하 처리 (Player 2) - 게임오버/플래시 상태가 아닌 경우에만
        if (!player2.gameModel.isGameOver() && !player2.isFlashing) {
            handleReplayPlayerDrop(player2, now);
        }

        // UI 렌더링 (항상 수행 - 게임오버 상태여도 보드 표시)
        updateGameBoard(player1);
        updateGameBoard(player2);
        
        // 점수 및 Next 블록 표시 업데이트
        if (player1.nextBlockModel != null) {
            player1.renderer.renderNextBlock(player1.nextBlockModel.peekNext());
        }
        if (player2.nextBlockModel != null) {
            player2.renderer.renderNextBlock(player2.nextBlockModel.peekNext());
        }
        
        // Attack 프리뷰 업데이트
        player1.renderer.renderAttackBoard(player1.attackModel.getAttacks());
        player2.renderer.renderAttackBoard(player2.attackModel.getAttacks());
    }
    
    /**
     * Replay용 플래시 애니메이션 처리 (간소화)
     * 플래시 상태를 빠르게 해제하여 입력 재생이 진행되도록 함
     */
    private void tickReplayFlash(PlayerSlot player, long now) {
        if (player == null || !player.isFlashing) {
            return;
        }
        
        // 플래시 간격 (재생 속도 반영)
        long flashInterval = (long)(100_000_000L / replaySpeedMultiplier); // 100ms 기본
        
        if (now < player.nextFlashAt) {
            return;
        }
        
        player.flashOn = !player.flashOn;
        player.flashToggleCount++;
        player.nextFlashAt = now + flashInterval;
        
        // 4번 토글 후 플래시 종료 (2번 깜빡임)
        if (player.flashToggleCount >= 4) {
            player.isFlashing = false;
            player.flashOn = false;
            player.flashMask = null;
            
            // 줄 삭제 처리
            processReplayClears(player);
        }
    }
    
    /**
     * Replay용 줄 삭제 처리
     */
    private void processReplayClears(PlayerSlot player) {
        // clearingRows, clearingCols, clearingCells 처리
        int linesCleared = 0;
        
        for (int r : player.clearingRows) {
            player.boardModel.clearRow(r);
            linesCleared++;
        }
        player.clearingRows.clear();
        
        for (int c : player.clearingCols) {
            player.boardModel.clearColumn(c);
        }
        player.clearingCols.clear();
        
        // 셀 삭제
        int h = player.boardModel.getSize().r;
        int w = player.boardModel.getSize().c;
        for (org.util.Point p : player.clearingCells) {
            if (p.r >= 0 && p.r < h && p.c >= 0 && p.c < w) {
                player.boardModel.getBoard()[p.r][p.c] = 0;
            }
        }
        player.clearingCells.clear();
        
        // 모델 업데이트
        player.gameModel.updateModels(linesCleared);
        
        // 새 블록 스폰
        player.gameModel.spawnNewBlock();
    }
    
    /**
     * Replay에서 플레이어의 블록 자동 낙하 처리
     * 주의: 이 메서드는 게임오버/플래시 상태가 아닌 경우에만 호출됨
     */
    private void handleReplayPlayerDrop(PlayerSlot player, long now) {
        // activeBlock이 null인 경우 처리 안함
        if (player.boardModel.activeBlock == null) {
            return;
        }

        int dropIntervalFrames = player.gameModel.getDropInterval();
        long dropIntervalNanos = dropIntervalFrames * 16_666_667L;
        
        // 재생 속도 반영
        long adjustedInterval = (long)(dropIntervalNanos / replaySpeedMultiplier);

        if (now - player.lastDropTime >= adjustedInterval) {
            boolean moved = player.boardModel.moveDown();
            if (!moved) {
                // 블록 고정 처리 (간소화된 버전)
                processReplayBlockLock(player);
            }
            player.lastDropTime = now;
        }
    }
    
    /**
     * Replay에서 블록 고정 처리 (간소화)
     */
    private void processReplayBlockLock(PlayerSlot player) {
        java.util.List<Integer> fullRows = player.boardModel.findFullRows();
        
        // 줄 삭제
        for (int r : fullRows) {
            player.boardModel.clearRow(r);
            player.renderer.triggerLineClearEffect(r);
        }
        
        // 모델 업데이트
        player.gameModel.updateModels(fullRows.size());
        
        // 새 블록 스폰 전에 게임오버 상태 저장
        boolean wasGameOver = player.gameModel.isGameOver();
        
        // 새 블록 스폰 (spawnNewBlock 내부에서 게임오버 체크가 이루어짐)
        player.gameModel.spawnNewBlock();
        
        // Replay 모드에서는 게임오버가 발생해도 바로 종료하지 않음
        // (저장된 입력이 모두 재생될 때까지 계속 진행)
        // 단, 이미 게임오버였던 상태는 유지
        if (!wasGameOver && player.gameModel.isGameOver()) {
            System.out.println("[REPLAY] Player game over detected during replay");
            // Replay에서는 게임오버 상태를 리셋하지 않고 유지
            // 이후 해당 플레이어의 입력은 무시됨
        }
    }

    private void updateReplay(long now) {
        if (isPaused() || replayFinished) {
            return;
        }

        // 경과 시간 계산 (누적 게임 시간 + 현재 세그먼트의 경과 시간)
        long elapsedReal = System.currentTimeMillis() - replayStartTime;
        long elapsedGame = accumulatedGameTime + (long)(elapsedReal * replaySpeedMultiplier);

        // Player 1 입력 재생 (relativeTimestamp 사용)
        while (nextP1Input != null && nextP1Input.getRelativeTimestamp() <= elapsedGame) {
            applyReplayInput(player1, nextP1Input);
            nextP1Input = player1InputIterator.hasNext() ? player1InputIterator.next() : null;
        }

        // Player 2 입력 재생 (relativeTimestamp 사용)
        while (nextP2Input != null && nextP2Input.getRelativeTimestamp() <= elapsedGame) {
            applyReplayInput(player2, nextP2Input);
            nextP2Input = player2InputIterator.hasNext() ? player2InputIterator.next() : null;
        }

        // Replay 종료 확인
        if (nextP1Input == null && nextP2Input == null && !replayFinished) {
            // 약간의 지연 후 종료 (마지막 동작 보여주기 위해)
            if (elapsedGame > replayData.getGameDurationMs() + 1000) {
                onReplayFinished();
            }
        }
    }

    /**
     * Replay 입력을 실제 플레이어에게 적용
     */
    private void applyReplayInput(PlayerSlot player, InputCommand cmd) {
        if (player == null || cmd == null) {
            return;
        }
        
        // 플래시 애니메이션 중에는 입력을 버퍼에 저장하지 않고 스킵
        // (실제 게임에서도 플래시 중에는 입력이 무시됨)
        // 단, 너무 오래 플래시가 지속되면 문제가 되므로 로그는 제거
        if (player.isFlashing) {
            return;
        }
        
        // 게임 오버 상태면 입력 무시 (이미 끝난 플레이어)
        if (player.gameModel.isGameOver()) {
            return;
        }
        
        // activeBlock이 null인 경우 방어
        if (player.boardModel.activeBlock == null) {
            return;
        }

        String action = cmd.getAction();
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
                // hardDrop은 handleHardDrop을 호출하는 것이 좋음 (이펙트 등 포함)
                handleHardDrop(player);
                return; 
        }

        updateGameBoard(player);
    }

    /**
     * 재생 속도 조절
     */
    private void adjustSpeed(boolean increase) {
        if (increase) {
            if (replaySpeedMultiplier < 4.0) {
                // 배속 변경 전 현재까지의 게임 시간 누적
                long now = System.currentTimeMillis();
                long elapsedReal = now - replayStartTime;
                accumulatedGameTime += (long)(elapsedReal * replaySpeedMultiplier);
                replayStartTime = now;
                
                replaySpeedMultiplier *= 2.0;
            }
        } else {
            if (replaySpeedMultiplier > 0.5) {
                // 배속 변경 전 현재까지의 게임 시간 누적
                long now = System.currentTimeMillis();
                long elapsedReal = now - replayStartTime;
                accumulatedGameTime += (long)(elapsedReal * replaySpeedMultiplier);
                replayStartTime = now;
                
                replaySpeedMultiplier /= 2.0;
            }
        }
        updateSpeedLabel();
        System.out.println("[REPLAY] Speed adjusted to " + replaySpeedMultiplier + "x, accumulatedTime=" + accumulatedGameTime + "ms");
    }

    private void updateSpeedLabel() {
        if (replaySpeedLabel != null) {
            replaySpeedLabel.setText(String.format("%.1fx", replaySpeedMultiplier));
        }
    }

    /**
     * Replay 종료 처리
     */
    private void onReplayFinished() {
        replayFinished = true;
        if (gameLoop != null) {
            gameLoop.stop();
        }

        Platform.runLater(() -> {
            showGameOverlay();
            if (winnerLabel != null) {
                // 내가 이겼는지 여부에 따라 표시
                String result = replayData.isMeWon() ? "YOU WIN!" : "YOU LOSE";
                winnerLabel.setText("REPLAY FINISHED\n" + result);
            }
            // 버튼 텍스트 변경
            if (restartButton != null) {
                restartButton.setText("REPLAY AGAIN");
                restartButton.setOnAction(e -> onRestartButtonClicked());
            }
        });

        System.out.println("[REPLAY] Playback finished - meWon: " + replayData.isMeWon());
    }

    /**
     * Replay 종료하고 메뉴로 돌아가기
     */
    private void exitReplay() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (router != null) {
            router.showNetworkMenu(false);
        }
    }

    @Override
    protected void goToMenu() {
        // Replay에서는 네트워크 메뉴로 돌아감
        exitReplay();
    }

    @Override
    protected void goToMenuFromPause() {
        // 일시정지에서도 네트워크 메뉴로 돌아감
        exitReplay();
    }

    @Override
    protected void togglePause() {
        // Replay에서는 일시정지만 허용 (단순 토글)
        if (player1 == null || player2 == null) {
            return;
        }

        boolean paused = !player1.gameModel.isPaused();
        player1.gameModel.setPaused(paused);
        player2.gameModel.setPaused(paused);

        if (paused) {
            showPauseOverlay();
        } else {
            hidePauseOverlay();
        }
    }

    @Override
    protected void onRestartButtonClicked() {
        // Replay 재시작
        initializeReplay();
        hideGameOverlay();
        startGameLoop();
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
    }
}
