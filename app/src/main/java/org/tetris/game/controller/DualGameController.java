package org.tetris.game.controller;

import java.util.List;

import org.tetris.Router;
import org.tetris.game.model.DualGameModel;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.items.ItemActivation;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.view.GameViewRenderer;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;
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

public class DualGameController extends BaseController<DualGameModel> implements RouterAware, ItemActivation {

    // === FXML 바인딩 ===
    @FXML
    private BorderPane root;

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

    // 오버레이 & 버튼
    @FXML
    private HBox gameOverOverlay;
    @FXML
    private HBox pauseOverlay;
    @FXML
    private Button pauseButton;
    @FXML
    private Button restartButton;
    @FXML
    private Button menuButton;
    @FXML
    private Button resumeButton;
    @FXML
    private Button pauseMenuButton;
    @FXML
    private Label winnerLabel; // dual 전용 - 누가 이겼는지 표시

    // === 모델 / 기타 필드 ===
    private DualGameModel dualGameModel;
    private GameModel gameModel1;
    private GameModel gameModel2;

    private PlayerSlot player1;
    private PlayerSlot player2;
    private PlayerSlot activeItemTarget; // ItemActivation 대상 플레이어

    private Router router;

    private AnimationTimer gameLoop;
    private long lastUpdate = 0L;
    private long lastDropTime1 = 0L;
    private long lastDropTime2 = 0L;
    private static final long FRAME_TIME = 16_666_667L; // ~60 FPS (나노초)

    // 플래시 애니메이션 파라미터
    private static final int FLASH_TIMES = 2;
    private static final int FLASH_TOGGLES = FLASH_TIMES * 2;
    private static final long FLASH_INTERVAL_NANOS = 100_000_000L; // 100ms
    private static final int MIN_CELL_SIZE = 16;
    private static final double PREVIEW_RATIO = 0.8;

    public DualGameController(DualGameModel model) {
        super(model);
        this.dualGameModel = model;
        this.gameModel1 = model.getPlayer1GameModel();
        this.gameModel2 = model.getPlayer2GameModel();
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

        // Model 초기화
        gameModel1.reset();
        gameModel2.reset();

        Platform.runLater(() -> {
            setupPlayerSlots();
            setupUI();
            root.requestFocus();
        });

        setupEventHandlers();
        startGameLoop();
    }

    // Dual 모드에서도 아이템 모드 / 난이도 설정
    public void setUpGameMode(boolean itemMode) {
        gameModel1.setItemMode(itemMode);
        gameModel1.setDifficulty();

        gameModel2.setItemMode(itemMode);
        gameModel2.setDifficulty();
    }

    // PlayerSlot + BoardRender 생성
    private void setupPlayerSlots() {
        if (root.getScene() == null || root.getScene().getWindow() == null) {
            return;
        }

        Point boardSize = gameModel1.getBoardModel().getSize(); // 두 플레이어 동일 가정

        int cellSize1 = calculateCellSize(gameBoard1, boardSize);
        int cellSize2 = calculateCellSize(gameBoard2, boardSize);

        int previewCellSize1 = Math.max(MIN_CELL_SIZE, (int) Math.round(cellSize1 * PREVIEW_RATIO));
        int previewCellSize2 = Math.max(MIN_CELL_SIZE, (int) Math.round(cellSize2 * PREVIEW_RATIO));

        GameViewRenderer renderer1 = new GameViewRenderer(
                gameBoard1,
                nextBlockPane1,
                boardSize,
                cellSize1,
                previewCellSize1);

        GameViewRenderer renderer2 = new GameViewRenderer(
                gameBoard2,
                nextBlockPane2,
                boardSize,
                cellSize2,
                previewCellSize2);

        player1 = new PlayerSlot(
                gameModel1.getBoardModel(),
                gameModel1.getNextBlockModel(),
                gameModel1.getScoreModel(),
                renderer1);
        player2 = new PlayerSlot(
                gameModel2.getBoardModel(),
                gameModel2.getNextBlockModel(),
                gameModel2.getScoreModel(),
                renderer2);

        renderer1.setupSinglePlayerLayout();
        renderer2.setupSinglePlayerLayout();
    }

    private int calculateCellSize(Pane boardPane, Point boardSize) {
        double paneWidth = boardPane.getWidth();
        double paneHeight = boardPane.getHeight();

        if (paneWidth <= 0) {
            paneWidth = boardPane.getPrefWidth();
        }
        if (paneHeight <= 0) {
            paneHeight = boardPane.getPrefHeight();
        }

        if ((paneWidth <= 0 || paneHeight <= 0) && root.getScene() != null && root.getScene().getWindow() != null) {
            paneWidth = Math.max(paneWidth, root.getScene().getWindow().getWidth() / 2.0);
            paneHeight = Math.max(paneHeight, root.getScene().getWindow().getHeight() / 1.2);
        }

        double computed = Math.min(paneWidth / boardSize.c, paneHeight / boardSize.r);
        int cellSize = (int) Math.round(computed);

        if (cellSize <= 0) {
            cellSize = MIN_CELL_SIZE * 2; // 최소 보장
        }

        return Math.max(MIN_CELL_SIZE, cellSize);
    }

    // === UI 초기 세팅 ===
    private void setupUI() {
        hideGameOverlay();
        hidePauseOverlay();

        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
        updateNextBlockPreview();
    }

    private void setupEventHandlers() {
        // 키보드 입력
        if (root.getScene() == null) {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    setupKeyboardInput();
                }
            });
        } else {
            setupKeyboardInput();
        }

        // 버튼 이벤트
        pauseButton.setOnAction(e -> togglePause());
        restartButton.setOnAction(e -> restartGame());
        menuButton.setOnAction(e -> goToMenu());
        resumeButton.setOnAction(e -> resumeGame());
        pauseMenuButton.setOnAction(e -> goToMenuFromPause());
    }

    private void setupKeyboardInput() {
        root.setFocusTraversable(true);
        root.requestFocus();

        root.getScene().setOnKeyPressed(this::handleKeyPress);
    }

    // === 키 입력 처리 ===
    private void handleKeyPress(KeyEvent e) {
        KeyCode code = e.getCode();

        // 전체 공통: P키로 Pause 토글
        if (code == KeyCode.P) {
            togglePause();
            e.consume();
            return;
        }

        if (gameModel1.isPaused() || gameModel2.isPaused()) {
            e.consume();
            return;
        }

        if (player1 == null || player2 == null) {
            e.consume();
            return;
        }

        // Player 1 (WASD / F)
        if (code == KeyCode.A) {
            if (!player1.isFlashing && !gameModel1.isGameOver()) {
                player1.boardModel.moveLeft();
                updateGameBoard(player1);
            }
        } else if (code == KeyCode.D) {
            if (!player1.isFlashing && !gameModel1.isGameOver()) {
                player1.boardModel.moveRight();
                updateGameBoard(player1);
            }
        } else if (code == KeyCode.W) {
            if (!player1.isFlashing && !gameModel1.isGameOver()) {
                player1.boardModel.rotate();
                updateGameBoard(player1);
            }
        } else if (code == KeyCode.S) {
            if (!player1.isFlashing && !gameModel1.isGameOver()) {
                boolean moved = player1.boardModel.moveDown();
                if (moved) {
                    player1.scoreModel.softDrop(1);
                }
                updateGameBoard(player1);
            }
        } else if (code == KeyCode.F) {
            if (!player1.isFlashing && !gameModel1.isGameOver()) {
                handleHardDrop(player1, gameModel1);
            }
        }

        // Player 2 (화살표 / Slash)
        else if (code == KeyCode.LEFT) {
            if (!player2.isFlashing && !gameModel2.isGameOver()) {
                player2.boardModel.moveLeft();
                updateGameBoard(player2);
            }
        } else if (code == KeyCode.RIGHT) {
            if (!player2.isFlashing && !gameModel2.isGameOver()) {
                player2.boardModel.moveRight();
                updateGameBoard(player2);
            }
        } else if (code == KeyCode.UP) {
            if (!player2.isFlashing && !gameModel2.isGameOver()) {
                player2.boardModel.rotate();
                updateGameBoard(player2);
            }
        } else if (code == KeyCode.DOWN) {
            if (!player2.isFlashing && !gameModel2.isGameOver()) {
                boolean moved = player2.boardModel.moveDown();
                if (moved) {
                    player2.scoreModel.softDrop(1);
                }
                updateGameBoard(player2);
            }
        } else if (code == KeyCode.SLASH) {
            if (!player2.isFlashing && !gameModel2.isGameOver()) {
                handleHardDrop(player2, gameModel2);
            }
        }

        updateScoreDisplay();
        updateNextBlockPreview();
        e.consume();
    }

    private void handleHardDrop(PlayerSlot slot, GameModel gm) {
        int dropDistance = slot.boardModel.hardDrop();
        slot.scoreModel.add(dropDistance * 2);
        lastDropTimeForPlayer(slot, System.nanoTime());
        lockCurrentBlock(slot, gm);
    }

    // === 게임 루프 ===
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0L) {
                    lastUpdate = now;
                    lastDropTime1 = now;
                    lastDropTime2 = now;
                    return;
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
        if (gameModel1.isPaused() || gameModel2.isPaused()) {
            return;
        }

        if (player1 == null || player2 == null) {
            return;
        }

        handlePlayerUpdate(now, player1, gameModel1, true);
        handlePlayerUpdate(now, player2, gameModel2, false);

        updateGameBoard(player1);
        updateGameBoard(player2);
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
        updateNextBlockPreview();

        checkGameOverState();
    }

    private void handlePlayerUpdate(long now, PlayerSlot slot, GameModel gm, boolean isPlayerOne) {
        if (gm.isGameOver()) {
            return;
        }

        if (slot.isFlashing) {
            tickFlash(slot, now);
            return;
        }

        int dropIntervalFrames = gm.getDropInterval();
        long dropIntervalNanos = dropIntervalFrames * FRAME_TIME;

        long timeSinceLastDrop = now - (isPlayerOne ? lastDropTime1 : lastDropTime2);
        if (timeSinceLastDrop >= dropIntervalNanos) {
            boolean moved = gm.autoDown();
            if (moved) {
                slot.scoreModel.blockDropped();
            } else {
                lockCurrentBlock(slot, gm);
            }
            lastDropTimeForPlayer(slot, now);
        }
    }

    private void checkGameOverState() {
        boolean p1Over = gameModel1.isGameOver();
        boolean p2Over = gameModel2.isGameOver();

        if (!p1Over && !p2Over)
            return;

        if (gameLoop != null) {
            gameLoop.stop();
        }

        showGameOverlay();

        String result;
        if (p1Over && p2Over) {
            result = "DRAW";
        } else if (p1Over) {
            result = "PLAYER 2 WINS";
        } else {
            result = "PLAYER 1 WINS";
        }
        if (winnerLabel != null) {
            winnerLabel.setText(result);
        }

        // 점수판은 둘 중 더 큰 점수 기준으로 보여주도록 처리
        if (router != null) {
            int bestScore = Math.max(player1.scoreModel.getScore(), player2.scoreModel.getScore());
            router.showScoreBoard(true, gameModel1.isItemMode(), bestScore);
        }
    }

    // ===== 블록 고정 / 라인 클리어 / 플래시 =====
    private void lockCurrentBlock(PlayerSlot slot, GameModel gm) {
        List<Integer> fullRows = slot.boardModel.findFullRows();
        slot.clearingRows.addAll(fullRows);

        // 아이템 활성화 (플레이어 기준)
        activeItemTarget = slot;
        gm.tryActivateItem(this);
        activeItemTarget = null;

        if (slot.boardModel.getIsForceDown()) {
            boolean moved = slot.boardModel.moveDownForce();
            if (!moved) {
                gm.updateModels(0);
                gm.spawnNewBlock();
                updateGameBoard(slot);
                checkGameOverState();
            }
            return;
        }

        if (!slot.clearingRows.isEmpty() ||
            !slot.clearingCols.isEmpty() ||
            !slot.clearingCells.isEmpty()) {

            beginFlash(slot, System.nanoTime());
            return;
        }

        updateGameBoard(slot);
        gm.spawnNewBlock();
        checkGameOverState();
    }

    private void beginFlash(PlayerSlot slot, long now) {
        slot.isFlashing = true;

        slot.flashMask = slot.renderer.buildFlashMask(
                slot.clearingRows,
                slot.clearingCols,
                slot.clearingCells);

        slot.flashOn = false;
        slot.flashToggleCount = 0;
        slot.nextFlashAt = now;
    }

    private void tickFlash(PlayerSlot slot, long now) {
        if (slot == null || !slot.isFlashing || slot.flashMask == null) {
            return;
        }

        if (now < slot.nextFlashAt) {
            return;
        }

        slot.flashOn = !slot.flashOn;
        slot.flashToggleCount++;
        slot.nextFlashAt = now + FLASH_INTERVAL_NANOS;

        if (slot.flashToggleCount >= FLASH_TOGGLES) {
            slot.isFlashing = false;
            slot.flashOn = false;
            slot.flashMask = null;
            processClears(slot);
        }
    }

    private void processClears(PlayerSlot slot) {
        GameModel gm = (slot == player1) ? gameModel1 : gameModel2;

        int linesCleared = deleteCompletedRows(slot);
        int colsCleared = deleteCompletedCols(slot);
        deleteCompletedCells(slot);

        gm.updateModels(linesCleared + colsCleared);
        gm.spawnNewBlock();
        checkGameOverState();
    }

    private int deleteCompletedRows(PlayerSlot slot) {
        for (int r : slot.clearingRows) {
            slot.boardModel.clearRow(r);
        }

        int count = slot.clearingRows.size();
        slot.clearingRows.clear();

        return count;
    }

    private int deleteCompletedCols(PlayerSlot slot){
        for (int c : slot.clearingCols) {
            slot.boardModel.clearColumn(c);
        }

        int count = slot.clearingCols.size();
        slot.clearingCols.clear();

        return count;
    }

    private void deleteCompletedCells(PlayerSlot slot){
        int boardHeight = slot.boardModel.getSize().r;
        int boardWidth  = slot.boardModel.getSize().c;

        for (Point p : slot.clearingCells) {
            if (p.r >= 0 && p.r < boardHeight && p.c >= 0 && p.c < boardWidth) {
                slot.boardModel.getBoard()[p.r][p.c] = 0;
            }
        }

        slot.clearingCells.clear();
    }

    // === UI 업데이트 ===
    private void updateGameBoard(PlayerSlot slot) {
        if (slot == null)
            return;
        int[][] board = slot.boardModel.getBoard();
        slot.renderer.renderBoard(board, slot.flashMask, slot.isFlashing, slot.flashOn);
    }

    private void updateScoreDisplay() {
        scoreLabel1.setText(player1.scoreModel.toString());
        scoreLabel2.setText(player2.scoreModel.toString());
    }

    private void updateLevelDisplay() {
        levelLabel1.setText(String.valueOf(gameModel1.getLevel()));
        levelLabel2.setText(String.valueOf(gameModel2.getLevel()));
    }

    private void updateLinesDisplay() {
        linesLabel1.setText(String.valueOf(gameModel1.getTotalLinesCleared()));
        linesLabel2.setText(String.valueOf(gameModel2.getTotalLinesCleared()));
    }

    private void updateNextBlockPreview() {
        if (player1 != null) {
            player1.renderer.renderNextBlock(player1.nextBlockModel.peekNext());
        }
        if (player2 != null) {
            player2.renderer.renderNextBlock(player2.nextBlockModel.peekNext());
        }
    }

    // === Pause / Resume / Menu / Restart ===
    private void togglePause() {
        if (gameModel1.isGameOver() && gameModel2.isGameOver())
            return;

        boolean paused = !(gameModel1.isPaused() || gameModel2.isPaused());
        gameModel1.setPaused(paused);
        gameModel2.setPaused(paused);

        if (paused) {
            showPauseOverlay();
        } else {
            hidePauseOverlay();
        }
    }

    private void resumeGame() {
        gameModel1.setPaused(false);
        gameModel2.setPaused(false);
        hidePauseOverlay();

        if (gameLoop != null) {
            lastUpdate = 0L;
            lastDropTime1 = 0L;
            lastDropTime2 = 0L;
            gameLoop.start();
        } else {
            startGameLoop();
        }
        root.requestFocus();
    }

    private void goToMenuFromPause() {
        resetGameController();
        hidePauseOverlay();

        if (router != null) {
            router.showStartMenu();
        }
    }

    private void restartGame() {
        resetGameController();
        setupUI();

        if (gameLoop != null)
             gameLoop.start();
        else
            startGameLoop();

        root.requestFocus();
    }

    private void goToMenu() {
        resetGameController();
        hideGameOverlay();

        if (router != null) {
            router.showStartMenu();
        }
    }

    private void resetGameController() {
        resetGameLoop();
        gameModel1.reset();
        gameModel2.reset();
        resetPlayerSlot(player1);
        resetPlayerSlot(player2);
    }

    private void resetGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        lastUpdate = 0L;
        lastDropTime1 = 0L;
        lastDropTime2 = 0L;
    }

    private void resetPlayerSlot(PlayerSlot slot) {
        if (slot == null) {
            return;
        }

        slot.isFlashing = false;
        slot.flashOn = false;
        slot.flashToggleCount = 0;
        slot.flashMask = null;

        slot.clearingRows.clear();
        slot.clearingCols.clear();
        slot.clearingCells.clear();

        slot.renderer.boardReset();
    }

    private void lastDropTimeForPlayer(PlayerSlot slot, long now) {
        if (slot == player1) {
            lastDropTime1 = now;
        } else if (slot == player2) {
            lastDropTime2 = now;
        }
    }

    private void showGameOverlay() {
        gameOverOverlay.setVisible(true);
        gameOverOverlay.setManaged(true);
    }

    private void hideGameOverlay() {
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setManaged(false);
    }

    private void showPauseOverlay() {
        pauseOverlay.setVisible(true);
        pauseOverlay.setManaged(true);
    }

    private void hidePauseOverlay() {
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

    @Override
    public void addClearingRow(int row) {
        if (activeItemTarget == null)
            return;

        if (!activeItemTarget.clearingRows.contains(row)) {
            activeItemTarget.clearingRows.add(row);
        }
    }

    @Override
    public void addClearingCol(int col) {
        if (activeItemTarget == null)
            return;

        if (!activeItemTarget.clearingCols.contains(col)) {
            activeItemTarget.clearingCols.add(col);
        }
    }

    @Override
    public void addClearingCells(List<Point> cells) {
        if (activeItemTarget == null || cells == null)
            return;

        for (Point cell : cells) {
            if (!activeItemTarget.clearingCells.contains(cell)) {
                activeItemTarget.clearingCells.add(cell);
            }
        }
    }
}
