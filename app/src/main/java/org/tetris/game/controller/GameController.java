package org.tetris.game.controller;

import java.util.List;

import org.tetris.Router;

import org.tetris.game.model.Board;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.ScoreModel;
import org.tetris.game.model.blocks.Block;
import org.tetris.game.model.items.ItemActivation;
import org.tetris.game.model.NextBlockModel;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.view.GameViewRenderer;

import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;

import org.util.KeyLayout;
import org.util.PlayerId;
import org.util.Point;

import javafx.fxml.FXML;

import javafx.animation.AnimationTimer;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class GameController extends BaseController<GameModel> implements RouterAware, ItemActivation {

    // ===== FXML 바인딩 =====

    @FXML
    private BorderPane root;

    @FXML
    private Pane gameBoard;

    @FXML
    private Pane nextBlockPane;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label levelLabel;

    @FXML
    private Label linesLabel;

    @FXML
    private Button pauseButton;

    @FXML
    private HBox gameOverOverlay;

    @FXML
    private HBox pauseOverlay;

    @FXML
    private Button restartButton;

    @FXML
    private Button menuButton;

    @FXML
    private Button resumeButton;

    @FXML
    private Button pauseMenuButton;

    // ===== 모델 및 기타 필드 =====

    private GameModel gameModel;
    private Router router;

    private PlayerSlot player; // 싱글플레이용 슬롯 1개

    private AnimationTimer gameLoop;
    private long lastUpdate = 0;
    private long lastDropTime = 0; // 마지막 블록 낙하 시간
    private static final long FRAME_TIME = 16_666_667; // ~60 FPS (나노초)

    // 플래시 애니메이션 파라미터
    private static final int FLASH_TIMES = 2;
    private static final int FLASH_TOGGLES = FLASH_TIMES * 2; // on/off 합계
    private static final long FLASH_INTERVAL_NANOS = 100_000_000L; // 100ms

    // ===== ItemActivation 구현 (아이템이 지울 행/열/셀 추가) =====

    @Override
    public void addClearingRow(int row) {
        if (player == null)
            return;
        if (!player.clearingRows.contains(row)) {
            player.clearingRows.add(row);
        }
    }

    @Override
    public void addClearingCol(int col) {
        if (player == null)
            return;
        if (!player.clearingCols.contains(col)) {
            player.clearingCols.add(col);
        }
    }

    @Override
    public void addClearingCells(List<Point> cells) {
        if (player == null || cells == null)
            return;
        for (Point cell : cells) {
            if (!player.clearingCells.contains(cell)) {
                player.clearingCells.add(cell);
            }
        }
    }

    // ===== 생성자 / Router =====

    public GameController(GameModel gameModel) {
        super(gameModel);
        this.gameModel = model;
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
    }

    // ===== 초기화 =====

    @FXML
    public void initialize() {
        super.initialize();

        // 이전 게임 루프 정리
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // 게임 상태 초기화
        gameModel.reset();
        lastUpdate = 0;
        lastDropTime = 0;

        // Stage 크기가 잡힌 후 PlayerSlot + UI 세팅
        Platform.runLater(() -> {
            setupPlayerSlot();
            setupUI();
        });

        setupEventHandlers();
        startGameLoop();
    }

    // PlayerSlot + BoardRender 생성 (PlayerSlot이 렌더러를 가짐)
    private void setupPlayerSlot() {
        if (root.getScene() == null || root.getScene().getWindow() == null) {
            return;
        }

        double stageWidth = root.getScene().getWindow().getWidth();
        double stageHeight = root.getScene().getWindow().getHeight();

        // 화면 크기에 따라 셀 크기 계산
        int cellSize = (int) Math.round(
                Math.min((stageWidth - 450) / 13.0, stageHeight / 23.0));

        int previewCellSize = (int) Math.round(cellSize * 0.8);

        // GameModel에서 모델 꺼내서 PlayerSlot 구성
        Board boardModel = gameModel.getBoardModel();
        NextBlockModel nextBlockModel = gameModel.getNextBlockModel();
        ScoreModel scoreModel = gameModel.getScoreModel();

        Point boardSize = boardModel.getSize();

        // 이 플레이어 전용 BoardRender 생성
        GameViewRenderer renderer = new GameViewRenderer(gameBoard, nextBlockPane, boardSize, cellSize,
                previewCellSize);

        this.player = new PlayerSlot(gameModel, boardModel, nextBlockModel, scoreModel, null, renderer);
        renderer.setupSinglePlayerLayout();
    }

    // UI 기본 상태 세팅
    private void setupUI() {
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
        updateNextBlockPreview();

        hideGameOverlay();
        hidePauseOverlay();
    }

    private void setupEventHandlers() {
        // 키보드 입력 처리
        if (root.getScene() == null) {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    setupKeyboardInput();
                }
            });
        } else {
            setupKeyboardInput();
        }

        // 버튼 이벤트 핸들러
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

    // ===== 입력 처리 =====

    private void handleKeyPress(KeyEvent e) {
        if (gameModel.isGameOver() || gameModel.isPaused()) {
            if (e.getCode() == KeyCode.P) {
                togglePause();
            }
            e.consume();
            return;
        }

        KeyCode code = e.getCode();

        if (code == KeyCode.P) {
            e.consume();
            togglePause();
            return;
        }

        // 플래시 애니메이션 도중에는 입력 무시
        if (player.isFlashing) {
            e.consume();
            return;
        }

        if (code == KeyLayout.getLeftKey(PlayerId.PLAYER1)) {
            player.boardModel.moveLeft();
            updateGameBoard();
        } else if (code == KeyLayout.getRightKey(PlayerId.PLAYER1)) {
            player.boardModel.moveRight();
            updateGameBoard();
        } else if (code == KeyLayout.getUpKey(PlayerId.PLAYER1)) {
            player.boardModel.rotate();
            updateGameBoard();
        } else if (code == KeyLayout.getDownKey(PlayerId.PLAYER1)) {
            boolean moved = player.boardModel.moveDown();
            if (moved) {
                player.scoreModel.softDrop(1); // 수동으로 1칸 내릴 때 점수
            }
            updateGameBoard();
        } else if (code == KeyLayout.getHardDropKey(PlayerId.PLAYER1)) {
            handleHardDrop();
        }

        e.consume();
    }

    private void handleHardDrop() {
        if (player == null)
            return;

        int dropDistance = player.boardModel.hardDrop();
        player.scoreModel.add(dropDistance * 2); // 하드 드롭 보너스
        lastDropTime = System.nanoTime(); // 하드 드롭 후 타이머 리셋
        lockCurrentBlock();
    }

    // ===== 게임 모드 설정 =====

    /**
     * 게임 모드 설정
     * 
     * @param itemMode 아이템 모드 여부
     */
    public void setUpGameMode(boolean itemMode) {
        gameModel.setItemMode(itemMode);
        gameModel.setDifficulty();
    }

    // ===== 메인 게임 루프 =====

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    lastDropTime = now;
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
        if (gameModel.isPaused() || gameModel.isGameOver()) {
            return;
        }

        if (player != null && player.isFlashing) {
            tickFlash(now);
            updateGameBoard();
            return;
        }

        // 레벨에 따른 블록 낙하 간격 (프레임 기준)
        int dropIntervalFrames = gameModel.getDropInterval();
        long dropIntervalNanos = dropIntervalFrames * FRAME_TIME;

        long timeSinceLastDrop = now - lastDropTime;
        if (timeSinceLastDrop >= dropIntervalNanos) {
            boolean moved = player.boardModel.autoDown();

            if (player != null) {
                if (moved) {
                    player.scoreModel.blockDropped();
                } else {
                    lockCurrentBlock();
                }
            }

            lastDropTime = now;
        }

        updateGameBoard();
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
        updateNextBlockPreview();
    }

    // ===== 블록 고정 / 라인 클리어 / 플래시 =====

    private void lockCurrentBlock() {
        List<Integer> fullRows = player.boardModel.findFullRows();
        player.clearingRows.addAll(fullRows);

        // 아이템 활성화 (필요 시 rows/cols/cells 추가)
        gameModel.tryActivateItem(this);

        if (player.boardModel.getIsForceDown()) {
            boolean moved = player.boardModel.moveDown(true);
            if (!moved) {
                player.boardModel.removeCurrentBlock();
                // 더 이상 내려갈 수 없으면 새 블록 생성
                gameModel.updateModels(0);
                gameModel.spawnNewBlock();
                updateGameBoard();
                if (gameModel.isGameOver()) {
                    handleGameOver();
                }
            }
            return;
        }

        if (!player.clearingRows.isEmpty() ||
                !player.clearingCols.isEmpty() ||
                !player.clearingCells.isEmpty()) {

            beginFlash(System.nanoTime());
            return; // 플래시 종료 후 실제 삭제
        }

        updateGameBoard();
        gameModel.spawnNewBlock();
        if (gameModel.isGameOver()) {
            handleGameOver();
        }
    }

    // 플래시 애니메이션 시작
    private void beginFlash(long now) {
        player.isFlashing = true;

        player.flashMask = player.renderer.buildFlashMask(
                player.clearingRows,
                player.clearingCols,
                player.clearingCells);

        player.flashOn = false;
        player.flashToggleCount = 0;
        player.nextFlashAt = now;
    }

    // 플래시 애니메이션 틱
    private void tickFlash(long now) {
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
            processClears();
        }
    }

    private void processClears() {
        int linesCleared = deleteCompletedRows();
        int colsCleared = deleteCompletedCols();
        deleteCompletedCells();

        // 점수 및 새 블록 생성
        gameModel.updateModels(linesCleared + colsCleared);
        gameModel.spawnNewBlock();
        if (gameModel.isGameOver()) {
            handleGameOver();
        }
    }

    // 행 실제 삭제 처리
    private int deleteCompletedRows() {
        for (int r : player.clearingRows) {
            player.boardModel.clearRow(r);
        }

        int count = player.clearingRows.size();
        player.clearingRows.clear();

        return count;
    }

    // 열 실제 삭제 처리
    private int deleteCompletedCols() {
        for (int c : player.clearingCols) {
            player.boardModel.clearColumn(c);
        }

        int count = player.clearingCols.size();
        player.clearingCols.clear();

        return count;
    }

    // 셀 실제 삭제 처리
    private void deleteCompletedCells() {
        int boardHeight = player.boardModel.getSize().r;
        int boardWidth = player.boardModel.getSize().c;

        for (Point p : player.clearingCells) {
            if (p.r >= 0 && p.r < boardHeight && p.c >= 0 && p.c < boardWidth) {
                player.boardModel.getBoard()[p.r][p.c] = 0;
            }
        }

        player.clearingCells.clear();
    }

    // ===== 렌더링 =====

    private void updateGameBoard() {
        int[][] board = player.boardModel.getBoard();
        player.renderer.renderBoard(board, player.flashMask, player.isFlashing, player.flashOn);
    }

    private void updateNextBlockPreview() {
        Block nextBlock = player.nextBlockModel.peekNext();
        player.renderer.renderNextBlock(nextBlock);
    }

    private void updateScoreDisplay() {
        scoreLabel.setText(player.scoreModel.toString());
    }

    private void updateLevelDisplay() {
        levelLabel.setText(String.valueOf(gameModel.getLevel()));
    }

    private void updateLinesDisplay() {
        linesLabel.setText(String.valueOf(gameModel.getTotalLinesCleared()));
    }

    // ===== 일시정지 / 재시작 / 메뉴 이동 =====

    private void togglePause() {
        if (gameModel.isGameOver())
            return;

        gameModel.setPaused(!gameModel.isPaused());

        if (gameModel.isPaused()) {
            showPauseOverlay();
        } else {
            hidePauseOverlay();
        }
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

    private void showGameOverlay() {
        gameOverOverlay.setVisible(true);
        gameOverOverlay.setManaged(true);
    }

    private void hideGameOverlay() {
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setManaged(false);
        root.requestFocus();
    }

    private void resumeGame() {
        gameModel.setPaused(false);
        hidePauseOverlay();

        if (gameLoop != null) {
            lastUpdate = 0;
            lastDropTime = 0;
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
        gameModel.reset();
        resetPlayerSlot();
    }

    private void resetGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        lastUpdate = 0;
        lastDropTime = 0;
    }

    private void resetPlayerSlot() {
        if (player != null) {
            player.reset();
        }
    }

    void handleGameOver() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        showGameOver();
    }

    private void showGameOver() {
        showGameOverlay();

        router.showScoreBoard(true, gameModel.isItemMode(), player.scoreModel.getScore());
    }

    @Override
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }
}