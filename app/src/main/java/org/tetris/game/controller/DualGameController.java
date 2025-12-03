package org.tetris.game.controller;

import java.util.List;

import org.tetris.Router;
import org.tetris.game.model.AttackModel;
import org.tetris.game.model.DualGameModel;
import org.tetris.game.model.GameMode;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.model.items.ItemActivation;
import org.tetris.game.view.GameViewRenderer;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;
import org.util.KeyLayout;
import org.util.PlayerId;
import org.util.Point;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class DualGameController<M extends DualGameModel> extends BaseController<M>
        implements RouterAware, ItemActivation {

    // === FXML 바인딩 ===
    @FXML
    protected BorderPane root;

    // Player 1 (왼쪽)
    @FXML
    private Pane gameBoard1;
    @FXML
    private Pane nextBlockPane1;
    @FXML
    private Label scoreLabel1;
    @FXML
    private Label levelLabel1;
    @FXML
    private Label linesLabel1;
    @FXML
    private VBox timeAttackBox1;
    @FXML
    private Label timerLabel1;
    @FXML
    private Pane attackPreviewPane1;

    // Player 2 (오른쪽)
    @FXML
    private Pane gameBoard2;
    @FXML
    private Pane nextBlockPane2;
    @FXML
    private Label scoreLabel2;
    @FXML
    private Label levelLabel2;
    @FXML
    private Label linesLabel2;
    @FXML
    private VBox timeAttackBox2;
    @FXML
    private Label timerLabel2;
    @FXML
    private Pane attackPreviewPane2;

    // 오버레이 & 버튼
    @FXML
    private HBox gameOverOverlay;
    @FXML
    protected HBox pauseOverlay;
    @FXML
    private Button pauseButton;
    @FXML
    protected Button restartButton;
    @FXML
    private Button menuButton;
    @FXML
    protected Button resumeButton;
    @FXML
    private Button pauseMenuButton;
    @FXML
    protected Label winnerLabel;

    // Player 1 Control Labels
    @FXML
    private Label moveControlLabel1;
    @FXML
    private Label rotateControlLabel1;
    @FXML
    private Label softDropControlLabel1;
    @FXML
    private Label hardDropControlLabel1;

    // Player 2 Control Labels
    @FXML
    private Label moveControlLabel2;
    @FXML
    private Label rotateControlLabel2;
    @FXML
    private Label softDropControlLabel2;
    @FXML
    private Label hardDropControlLabel2;

    // === 모델 및 슬롯 ===
    protected final M dualGameModel;

    protected PlayerSlot player1;
    protected PlayerSlot player2;
    protected PlayerSlot activeItemTarget;

    protected Router router;
    protected AnimationTimer gameLoop;

    protected long lastUpdate = 0L;
    private static final long FRAME_TIME = 16_666_667L; // ~60 FPS (나노초)
    private long lastLogTime = 0L;

    // 플래시 애니메이션 파라미터
    private static final int FLASH_TIMES = 2;
    private static final int FLASH_TOGGLES = FLASH_TIMES * 2;
    private static final long FLASH_INTERVAL_NANOS = 100_000_000L; // 100ms
    private static final int MIN_CELL_SIZE = 16;
    private static final double PREVIEW_RATIO = 0.8;
    
    private boolean isTimeAttackMode = false;
    protected boolean isGameOver = false;
    protected boolean firstTriggered = false; // 게임 초기화 후 첫 프레임 트리거 플래그 -> 블록이 미리 떨어짐을 방지
    private double playTime = 0.0;

    public DualGameController(M model) {
        super(model);
        this.dualGameModel = model;
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
    }

    @FXML
    public void initialize() {
        super.initialize();

        if (gameLoop != null) {
            gameLoop.stop();
        }

        firstTriggered = false;

        // Model 초기화
        dualGameModel.getPlayer1GameModel().reset();
        dualGameModel.getPlayer2GameModel().reset();

        Platform.runLater(() -> {
            setupPlayerSlots();
            setupUI();
            root.requestFocus();
            firstTriggered = true;
        });

        setupEventHandlers();
        startGameLoop();
    }

    // Dual 모드에서도 아이템 모드 / 난이도 설정
    public void setUpGameMode(GameMode mode) {
        boolean itemMode = (mode == GameMode.ITEM);
        boolean timeMode = (mode == GameMode.TIME_ATTACK);

        dualGameModel.getPlayer1GameModel().setItemMode(itemMode);
        dualGameModel.getPlayer2GameModel().setItemMode(itemMode);

        dualGameModel.getPlayer1GameModel().setDifficulty();
        dualGameModel.getPlayer2GameModel().setDifficulty();
        dualGameModel.getTimeAttack().setTimeAttackMode(timeMode);
        isTimeAttackMode = timeMode;
        updateTimeAttackVisibility();
    }

    // PlayerSlot 생성 로직 통합
    private void setupPlayerSlots() {
        if (root.getScene() == null || root.getScene().getWindow() == null) {
            return;
        }

        Point boardSize = dualGameModel.getPlayer1GameModel().getBoardModel().getSize();

        player1 = createPlayerSlot(
                dualGameModel.getPlayer1GameModel(),
                dualGameModel.getPlayer1AttackModel(),
                gameBoard1, nextBlockPane1, attackPreviewPane1, boardSize);

        player2 = createPlayerSlot(
                dualGameModel.getPlayer2GameModel(),
                dualGameModel.getPlayer2AttackModel(),
                gameBoard2, nextBlockPane2, attackPreviewPane2, boardSize);

        player1.renderer.setupSinglePlayerLayout();
        player2.renderer.setupSinglePlayerLayout();
    }

    private PlayerSlot createPlayerSlot(GameModel gm, AttackModel am,
            Pane boardPane, Pane nextPane, Pane attackPane, Point boardSize) {

        int cellSize = calculateCellSize(boardPane, boardSize);
        int previewCellSize = Math.max(MIN_CELL_SIZE, (int) Math.round(cellSize * PREVIEW_RATIO));

        GameViewRenderer renderer = new GameViewRenderer(
                boardPane, nextPane, attackPane, boardSize, cellSize, previewCellSize);
        return new PlayerSlot(
                gm,
                gm.getBoardModel(),
                gm.getNextBlockModel(),
                gm.getScoreModel(),
                am,
                renderer);
    }

    private int calculateCellSize(Pane boardPane, Point boardSize) {
        double paneWidth = boardPane.getWidth();
        double paneHeight = boardPane.getHeight();

        if (paneWidth <= 0)
            paneWidth = boardPane.getPrefWidth();
        if (paneHeight <= 0)
            paneHeight = boardPane.getPrefHeight();

        if ((paneWidth <= 0 || paneHeight <= 0) && root.getScene() != null && root.getScene().getWindow() != null) {
            paneWidth = Math.max(paneWidth, root.getScene().getWindow().getWidth() / 2.0);
            paneHeight = Math.max(paneHeight, root.getScene().getWindow().getHeight() / 1.2);
        }

        double computed = Math.min(paneWidth / boardSize.c, paneHeight / boardSize.r);
        int cellSize = (int) Math.round(computed);

        return Math.max(MIN_CELL_SIZE, (cellSize <= 0) ? MIN_CELL_SIZE * 2 : cellSize);
    }

    // === UI 초기 세팅 ===
    private void setupUI() {
        hideGameOverlay();
        hidePauseOverlay();
        updateTimeAttackVisibility();
        updateUI();
        updateControlLabels();
    }

    private void updateControlLabels() {
        // Player 1 키 설정
        KeyCode leftKey1 = KeyLayout.getLeftKey(PlayerId.PLAYER1);
        KeyCode rightKey1 = KeyLayout.getRightKey(PlayerId.PLAYER1);
        KeyCode upKey1 = KeyLayout.getUpKey(PlayerId.PLAYER1);
        KeyCode downKey1 = KeyLayout.getDownKey(PlayerId.PLAYER1);
        KeyCode hardDropKey1 = KeyLayout.getHardDropKey(PlayerId.PLAYER1);

        if (moveControlLabel1 != null) {
            moveControlLabel1.setText("Move: " + leftKey1.getName() + " / " + rightKey1.getName());
        }
        if (rotateControlLabel1 != null) {
            rotateControlLabel1.setText("Rotate: " + upKey1.getName());
        }
        if (softDropControlLabel1 != null) {
            softDropControlLabel1.setText("Down: " + downKey1.getName());
        }
        if (hardDropControlLabel1 != null) {
            hardDropControlLabel1.setText("Drop: " + hardDropKey1.getName());
        }
 
         // Player 2 키 설정
         KeyCode leftKey2 = KeyLayout.getLeftKey(PlayerId.PLAYER2);
         KeyCode rightKey2 = KeyLayout.getRightKey(PlayerId.PLAYER2);
         KeyCode upKey2 = KeyLayout.getUpKey(PlayerId.PLAYER2);
         KeyCode downKey2 = KeyLayout.getDownKey(PlayerId.PLAYER2);
         KeyCode hardDropKey2 = KeyLayout.getHardDropKey(PlayerId.PLAYER2);
 
        if (moveControlLabel2 != null) {
            moveControlLabel2.setText("Move: " + leftKey2.getName() + " / " + rightKey2.getName());
        }
        if (rotateControlLabel2 != null) {
            rotateControlLabel2.setText("Rotate: " + upKey2.getName());
        }
        if (softDropControlLabel2 != null) {
            softDropControlLabel2.setText("Down: " + downKey2.getName());
        }
        if (hardDropControlLabel2 != null) {
            hardDropControlLabel2.setText("Drop: " + hardDropKey2.getName());
        }
    }

    protected void setupEventHandlers() {
        if (root.getScene() == null) {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null)
                    setupKeyboardInput();
            });
        } else {
            setupKeyboardInput();
        }

        pauseButton.setOnAction(e -> togglePause());
        restartButton.setOnAction(e -> onRestartButtonClicked());
        menuButton.setOnAction(e -> goToMenu());
        resumeButton.setOnAction(e -> onResumeButtonClicked());
        pauseMenuButton.setOnAction(e -> goToMenuFromPause());
    }

    private void setupKeyboardInput() {
        root.setFocusTraversable(true);
        root.requestFocus();
        root.getScene().setOnKeyPressed(this::handleKeyPress);
    }

    // === 키 입력 처리 (통합) ===
    protected void handleKeyPress(KeyEvent e) {
        if (e.getCode() == KeyCode.P) {
            togglePause();
            e.consume();
            return;
        }

        if (player1 == null || player2 == null) {
            e.consume();
            return;
        }

        // 일시정지 또는 게임 오버 시 입력 무시
        if (player1.gameModel.isPaused() || player2.gameModel.isPaused()
            || player1.gameModel.isGameOver() || player2.gameModel.isGameOver()) {
            e.consume();
            return;
        }

        handlePlayerInput(e, player1, 
                KeyLayout.getLeftKey(PlayerId.PLAYER1), 
                KeyLayout.getRightKey(PlayerId.PLAYER1),
                KeyLayout.getUpKey(PlayerId.PLAYER1),
                KeyLayout.getDownKey(PlayerId.PLAYER1),
                KeyLayout.getHardDropKey(PlayerId.PLAYER1));

        handlePlayerInput(e, player2, 
                KeyLayout.getLeftKey(PlayerId.PLAYER2), 
                KeyLayout.getRightKey(PlayerId.PLAYER2),
                KeyLayout.getUpKey(PlayerId.PLAYER2),
                KeyLayout.getDownKey(PlayerId.PLAYER2),
                KeyLayout.getHardDropKey(PlayerId.PLAYER2));

        updateScoreDisplay();
        e.consume();
    }

    protected void handlePlayerInput(
            KeyEvent e, PlayerSlot player,
            KeyCode left, KeyCode right, KeyCode rotate,
            KeyCode down, KeyCode hardDrop) {

        GameModel gm = player.gameModel;
        if (player.isFlashing || gm.isGameOver())
            return;

        KeyCode code = e.getCode();
        boolean updateNeeded = false;

        if (code == left) {
            player.boardModel.moveLeft();
            updateNeeded = true;
        } else if (code == right) {
            player.boardModel.moveRight();
            updateNeeded = true;
        } else if (code == rotate) {
            player.boardModel.rotate();
            updateNeeded = true;
        } else if (code == down) {
            if (player.boardModel.moveDown()) {
                player.scoreModel.softDrop(1);
            }
            updateNeeded = true;
        } else if (code == hardDrop) {
            handleHardDrop(player);
            return;
        }

        if (updateNeeded) {
            updateUI();
        }
    }

    protected void handleHardDrop(PlayerSlot player) {
        int dropDistance = player.boardModel.hardDrop();
        player.scoreModel.add(dropDistance * 2);

        player.lastDropTime = System.nanoTime();
        
        // Trigger Hard Drop Effect
        player.renderer.triggerHardDropEffect();
        
        lockCurrentBlock(player);
    }

    protected boolean isPaused() {
        return player1.gameModel.isPaused() || player2.gameModel.isPaused();
    }


    // === 게임 루프 ===
    protected void startGameLoop() {
        gameLoop = new AnimationTimer() {
            double deltaTime = 0.0;
            long previousTime = System.nanoTime();

            @Override
            public void handle(long now) {
                if (player1 == null || player2 == null)
                    return;

                if(!firstTriggered) {
                    return;
                }

                if (lastUpdate == 0L) {
                    lastUpdate = now;
                    player1.lastDropTime = now;
                    player2.lastDropTime = now;
                    return;
                }

                deltaTime = (now - previousTime) / 1_000_000_000.0;
                previousTime = now;

                if (!isPaused()) {
                    playTime += deltaTime; // 초 단위
                }

                long elapsed = now - lastUpdate;
                if (elapsed >= FRAME_TIME) {
                    update(now);
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }

    private void update(long now) {
        if (isPaused())
            return;

        if (lastLogTime == 0L) {
            lastLogTime = now;
        } else if (now - lastLogTime >= 1_000_000_000L) {
            System.out.println(dualGameModel.getTimeAttack().getRemainingSeconds(playTime) + " seconds left");
            lastLogTime = now;
        }

        if (player1 == null || player2 == null)
            return;

        // Update Effects Animation
        player1.renderer.updateEffects(now);
        player2.renderer.updateEffects(now);

        handlePlayerUpdate(player1, now);
        handlePlayerUpdate(player2, now);

        updateUI();
        checkGameOverState();
    }

    private void handlePlayerUpdate(PlayerSlot player, long now) {
        GameModel gm = player.gameModel;
        if (gm.isGameOver())
            return;

        if (player.isFlashing) {
            tickFlash(player, now);
            return;
        }

        int dropIntervalFrames = gm.getDropInterval();
        long dropIntervalNanos = dropIntervalFrames * FRAME_TIME;

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

    protected void checkGameOverState() {
        isGameOver = false;

        boolean p1Over = player1.gameModel.isGameOver();
        boolean p2Over = player2.gameModel.isGameOver();

        if (dualGameModel.getTimeAttack().getRemainingSeconds(playTime) == 0) {
            int score1 = player1.scoreModel.getScore();
            int score2 = player2.scoreModel.getScore();

            if (score1 > score2)
                p2Over = true;
            else if (score1 < score2)
                p1Over = true;
            else { // 동점
                p1Over = true;
                p2Over = true;
            }
        }

        if (!p1Over && !p2Over)
            return;

        isGameOver = true;

        if (gameLoop != null)
            gameLoop.stop();
        showGameOverlay();

        setWinnerLabel(p1Over, p2Over);
    }

    private void setWinnerLabel(boolean p1Over, boolean p2Over) {
        String result;

        if (p1Over && p2Over)
            result = "DRAW";
        else if (p1Over)
            result = "PLAYER 2 WINS";
        else
            result = "PLAYER 1 WINS";

        if (winnerLabel != null)
            winnerLabel.setText(result);
    }

    // === 블록 고정 및 로직 ===
    protected void lockCurrentBlock(PlayerSlot player) {
        // 블록 고정 직후 훅 호출 (상태 동기화 등)
        onBlockLocked(player);

        GameModel gm = player.gameModel;
        List<Integer> fullRows = player.boardModel.findFullRows();
        player.clearingRows.addAll(fullRows);

        // 아이템 제외하고 기본 로직으로 인한 row만 전달
        if (player.clearingRows.size() >= 2) {
            sendAttack(player);
            updateUI(); // 공격 Pane 업데이트
        }

        activeItemTarget = player;
        gm.tryActivateItem(this);
        activeItemTarget = null;

        if (player.boardModel.getIsForceDown()) {
            boolean moved = player.boardModel.moveDown(true);
            if (!moved) {
                gm.updateModels(0);
                player.boardModel.removeCurrentBlock();
                
                gm.spawnNewBlock();
                updateGameBoard(player);
                checkGameOverState();
            }
            return;
        }

        if (!player.clearingRows.isEmpty() || !player.clearingCols.isEmpty() || !player.clearingCells.isEmpty()) {
            beginFlash(player, System.nanoTime());
            return;
        }

        processIncomingAttacks(player);
        updateGameBoard(player);
        
        gm.spawnNewBlock();
        checkGameOverState();
    }

    /**
     * 블록이 바닥에 고정되고, 줄 삭제 등의 처리가 끝난 직후 호출됩니다.
     * P2P 모드에서 상태 동기화를 위해 오버라이드합니다.
     */
    protected void onBlockLocked(PlayerSlot player) {
        
    }

    private void beginFlash(PlayerSlot player, long now) {
        player.isFlashing = true;
        player.flashMask = player.renderer.buildFlashMask(player.clearingRows, player.clearingCols,
                player.clearingCells);
        player.flashOn = false;
        player.flashToggleCount = 0;
        player.nextFlashAt = now;
    }

    private void tickFlash(PlayerSlot player, long now) {
        if (player == null || !player.isFlashing || player.flashMask == null)
            return;
        if (now < player.nextFlashAt)
            return;

        player.flashOn = !player.flashOn;
        player.flashToggleCount++;
        player.nextFlashAt = now + FLASH_INTERVAL_NANOS;

        if (player.flashToggleCount >= FLASH_TOGGLES) {
            player.isFlashing = false;
            player.flashOn = false;
            player.flashMask = null;
            processClears(player);
        }
    }

    private void processClears(PlayerSlot player) {
        GameModel gm = player.gameModel;

        int linesCleared = deleteCompletedRows(player);
        int colsCleared = deleteCompletedCols(player);
        deleteCompletedCells(player);

        gm.updateModels(linesCleared + colsCleared);
        processIncomingAttacks(player);

        gm.spawnNewBlock();
        checkGameOverState();
    }

    // === 공격 로직 (PlayerSlot 활용) ===
    private void sendAttack(PlayerSlot attackerSlot) {
        // 상대방 찾기
        PlayerSlot targetSlot = (attackerSlot == player1) ? player2 : player1;

        for (int row : attackerSlot.clearingRows) {
            int[] attackLine = attackerSlot.boardModel.getRowForAttack(row);
            targetSlot.attackModel.push(attackLine);
        }
    }

    private void processIncomingAttacks(PlayerSlot player) {
        while (true) {
            int[] attackLine = player.attackModel.pop();
            if (attackLine == null)
                break;

            boolean success = player.boardModel.pushUp(attackLine);
            if (!success) {
                player.gameModel.setGameOver(true);
                break;
            }
        }
    }

    // === Helper Methods for Cleaning Lists ===
    private int deleteCompletedRows(PlayerSlot player) {
        for (int r : player.clearingRows) {
            player.boardModel.clearRow(r);
            // Trigger Line Clear Effect per row
            player.renderer.triggerLineClearEffect(r);
        }
        int count = player.clearingRows.size();
        player.clearingRows.clear();
        return count;
    }

    private int deleteCompletedCols(PlayerSlot player) {
        for (int c : player.clearingCols)
            player.boardModel.clearColumn(c);
        int count = player.clearingCols.size();
        player.clearingCols.clear();
        return count;
    }

    private void deleteCompletedCells(PlayerSlot player) {
        int h = player.boardModel.getSize().r, w = player.boardModel.getSize().c;
        for (Point p : player.clearingCells) {
            if (p.r >= 0 && p.r < h && p.c >= 0 && p.c < w)
                player.boardModel.getBoard()[p.r][p.c] = 0;
        }
        player.clearingCells.clear();
    }

    // === UI 업데이트 통합 ===
    private void updateUI() {
        updateGameBoard(player1);
        updateGameBoard(player2);
        updateScoreDisplay();

        if (player1 != null) {
            if (levelLabel1 != null) levelLabel1.setText(String.valueOf(player1.gameModel.getLevel()));
            if (linesLabel1 != null) linesLabel1.setText(String.valueOf(player1.gameModel.getTotalLinesCleared()));
            player1.renderer.renderNextBlock(player1.nextBlockModel.peekNext());
            player1.renderer.renderAttackBoard(player1.attackModel.getAttacks());
        }
        if (player2 != null) {
            if (levelLabel2 != null) levelLabel2.setText(String.valueOf(player2.gameModel.getLevel()));
            if (linesLabel2 != null) linesLabel2.setText(String.valueOf(player2.gameModel.getTotalLinesCleared()));
            player2.renderer.renderNextBlock(player2.nextBlockModel.peekNext());
            player2.renderer.renderAttackBoard(player2.attackModel.getAttacks());
        }

        updateTimeAttackTimer();
    }

    protected void updateGameBoard(PlayerSlot player) {
        if (player == null)
            return;

        player.renderer.renderBoard(player.boardModel.getBoard(), player.flashMask, player.isFlashing, player.flashOn);
    }

    private void updateScoreDisplay() {
        if (player1 != null && scoreLabel1 != null)
            scoreLabel1.setText(player1.scoreModel.toString());
        if (player2 != null && scoreLabel2 != null)
            scoreLabel2.setText(player2.scoreModel.toString());
    }

    private void updateTimeAttackVisibility() {
        boolean show = isTimeAttackMode;

        if (timeAttackBox1 != null) {
            timeAttackBox1.setVisible(show);
            timeAttackBox1.setManaged(show);
        }
        if (timeAttackBox2 != null) {
            timeAttackBox2.setVisible(show);
            timeAttackBox2.setManaged(show);
        }
    }

    private void updateTimeAttackTimer() {
        if (!isTimeAttackMode) {
            return;
        }

        int remainingSeconds = (int) Math.ceil(dualGameModel.getTimeAttack().getRemainingSeconds(playTime));
        String formatted = formatSeconds(Math.max(remainingSeconds, 0));

        if (timerLabel1 != null)
            timerLabel1.setText(formatted);
        if (timerLabel2 != null)
            timerLabel2.setText(formatted);
    }

    private String formatSeconds(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    // === Pause & Menu ===
    protected void togglePause() {
        if (isGameOver || player1 == null || player2 == null)
            return;
        boolean paused = !player1.gameModel.isPaused();
        player1.gameModel.setPaused(paused);
        player2.gameModel.setPaused(paused);

        if (paused)
            showPauseOverlay();
        else
            hidePauseOverlay();
    }

    protected void stopGame() {
        player1.gameModel.setPaused(false);
        player2.gameModel.setPaused(false);
        hidePauseOverlay();
        if (gameLoop != null) {
            gameLoop.stop(); // Stop the game loop
            lastUpdate = 0L;
        }
    }

    /**
     * Resume 버튼 클릭 시 호출 - 서브클래스에서 오버라이드 가능
     */
    protected void onResumeButtonClicked() {
        resumeGame();
    }

    private void resumeGame() {
        player1.gameModel.setPaused(false);
        player2.gameModel.setPaused(false);
        hidePauseOverlay();
        if (gameLoop != null) {
            lastUpdate = 0L;
            gameLoop.start();
        } else
            startGameLoop();
        root.requestFocus();
    }

    private void restartGame() {
        resetGameController();
        setupUI();

        startGameLoop();

        root.requestFocus();
    }
    
    /**
     * Restart 버튼 클릭 시 호출 - 서브클래스에서 오버라이드 가능
     */
    protected void onRestartButtonClicked() {
        restartGame();
    }

    protected void goToMenu() {
        resetGameController();
        hideGameOverlay();
        if (router != null)
            router.showStartMenu();
    }
    
    protected void goToMenuFromPause() {
        goToMenu();
    }

    protected void resetGameController() {
        playTime = 0.0;

        if (gameLoop != null)
            gameLoop.stop();

        lastUpdate = 0L;

        if (player1 != null)
            player1.reset();
        if (player2 != null)
            player2.reset();
    }

    protected void showGameOverlay() {
        gameOverOverlay.setVisible(true);
        gameOverOverlay.setManaged(true);
    }

    protected void hideGameOverlay() {
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setManaged(false);
    }

    protected void showPauseOverlay() {
        pauseOverlay.setVisible(true);
        pauseOverlay.setManaged(true);
    }

    protected void hidePauseOverlay() {
        pauseOverlay.setVisible(false);
        pauseOverlay.setManaged(false);
        root.requestFocus();
    }

    @Override
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }

    // === ItemActivation Implementations ===
    @Override
    public void addClearingRow(int row) {
        if (activeItemTarget != null && !activeItemTarget.clearingRows.contains(row))
            activeItemTarget.clearingRows.add(row);
    }

    @Override
    public void addClearingCol(int col) {
        if (activeItemTarget != null && !activeItemTarget.clearingCols.contains(col))
            activeItemTarget.clearingCols.add(col);
    }

    @Override
    public void addClearingCells(List<Point> cells) {
        if (activeItemTarget != null && cells != null) {
            for (Point p : cells)
                if (!activeItemTarget.clearingCells.contains(p))
                    activeItemTarget.clearingCells.add(p);
        }
    }
}
