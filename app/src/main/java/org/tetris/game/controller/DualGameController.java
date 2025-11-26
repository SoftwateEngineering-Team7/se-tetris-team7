package org.tetris.game.controller;

import java.util.List;

import org.tetris.Router;
import org.tetris.game.model.DualGameModel;
import org.tetris.game.model.GameMode;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.items.ItemActivation;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.view.GameViewRenderer;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;
import org.util.Point;

import org.tetris.network.game.LocalMultiGameEngine;
import org.tetris.game.view.GameViewCallback;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * 로컬 멀티플레이(1 vs 1) 게임을 관리하는 컨트롤러입니다.
 * 각 플레이어의 키 입력, 블록 낙하, 게임 루프, 승패 판정 등
 * 멀티플레이 관련 로직을 담당합니다.
 */
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

    private LocalMultiGameEngine gameEngine;
    private GameKeyHandler keyHandler1;
    private GameKeyHandler keyHandler2;

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
    @Override
    public void initialize() {
        super.initialize();

        setupEventHandlers();

        // 엔진 시작은 initialize 후 runLater에서 처리됨
        Platform.runLater(() -> {
            setupPlayerSlots();
            setupUI();
            root.requestFocus();
        });
    }

    // Dual 모드에서도 아이템 모드 / 난이도 설정
    public void setUpGameMode(GameMode mode) {
        boolean itemMode = (mode == GameMode.ITEM);

        gameModel1.setItemMode(itemMode);
        gameModel2.setItemMode(itemMode);

        gameModel1.setDifficulty();
        gameModel2.setDifficulty();
    }

    // PlayerSlot + BoardRender 생성
    private void setupPlayerSlots() {
        // Remove null check to allow initialization in tests
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

        // 엔진 초기화
        gameEngine = new LocalMultiGameEngine(player1, player2, dualGameModel, new GameViewCallback() {
            @Override
            public void updateGameBoard() {
                DualGameController.this.updateGameBoard(player1);
                DualGameController.this.updateGameBoard(player2);
            }

            @Override
            public void updateScoreDisplay() {
                DualGameController.this.updateScoreDisplay();
            }

            @Override
            public void updateLevelDisplay() {
                DualGameController.this.updateLevelDisplay();
            }

            @Override
            public void updateLinesDisplay() {
                DualGameController.this.updateLinesDisplay();
            }

            @Override
            public void updateNextBlockPreview() {
                DualGameController.this.updateNextBlockPreview();
            }

            @Override
            public void showGameOver() {
                checkGameOverState();
            }

            @Override
            public void updatePauseUI(boolean isPaused) {
                if (isPaused)
                    showPauseOverlay();
                else
                    hidePauseOverlay();
            }

            @Override
            public void addClearingRow(int row) {
            } // Handled by engine internal logic usually

            @Override
            public void addClearingCol(int col) {
            }

            @Override
            public void addClearingCells(List<Point> cells) {
            }
        });

        // Key Handlers
        keyHandler1 = new GameKeyHandler(gameEngine, org.util.KeyLayout.WASD);

        keyHandler2 = new GameKeyHandler(gameEngine, org.util.KeyLayout.ARROWS);
        // Configure P2 Commands
        keyHandler2.setMoveLeftCommand(game -> ((LocalMultiGameEngine) game).moveLeftP2());
        keyHandler2.setMoveRightCommand(game -> ((LocalMultiGameEngine) game).moveRightP2());
        keyHandler2.setRotateCommand(game -> ((LocalMultiGameEngine) game).rotateP2());
        keyHandler2.setSoftDropCommand(game -> ((LocalMultiGameEngine) game).softDropP2());
        keyHandler2.setHardDropCommand(game -> ((LocalMultiGameEngine) game).hardDropP2());
        // Pause is shared, so P key on either works (default behavior of
        // GameKeyHandler)

        gameEngine.startGame(0);
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
        if (gameEngine == null || keyHandler1 == null || keyHandler2 == null)
            return;

        // Delegate to handlers
        // We need to check which keys are pressed or just pass to both?
        // GameKeyHandler checks specific keys. If key matches, it executes and
        // consumes.
        // If we pass to handler1, and it consumes, handler2 won't see it?
        // KeyEvent is passed by reference?
        // If handler1 consumes it, e.isConsumed() will be true?
        // GameKeyHandler.handleKeyPress calls e.consume().

        // So we should try handler1, then handler2.
        // But wait, if handler1 checks WASD and P.
        // handler2 checks ARROWS and P.
        // If P is pressed, handler1 consumes it. handler2 won't see it. That's fine
        // (shared pause).
        // If A is pressed, handler1 consumes.
        // If Left is pressed, handler1 ignores (returns). handler2 sees it.

        keyHandler1.handleKeyPress(e);
        if (!e.isConsumed()) {
            keyHandler2.handleKeyPress(e);
        }
    }

    // === UI 업데이트 ===
    private void updateGameBoard(PlayerSlot slot) {
        if (slot == null)
            return;
        int[][] board = slot.boardModel.getBoard();
        slot.renderer.renderBoard(board, slot.flashMask, slot.isFlashing, slot.flashOn);
    }

    private void updateScoreDisplay() {
        if (player1 != null)
            scoreLabel1.setText(player1.scoreModel.toString());
        if (player2 != null)
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
            if (gameEngine != null)
                gameEngine.togglePause();
        } else {
            hidePauseOverlay();
            if (gameEngine != null)
                gameEngine.togglePause();
        }
    }

    private void resumeGame() {
        gameModel1.setPaused(false);
        gameModel2.setPaused(false);
        hidePauseOverlay();

        if (gameEngine != null)
            gameEngine.togglePause();
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

        // Model 초기화
        gameModel1.reset();
        gameModel2.reset();

        Platform.runLater(() -> {
            setupPlayerSlots();
            setupUI();
            root.requestFocus();
        });
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
        // gameModel reset is done in restartGame or initialize
        resetPlayerSlot(player1);
        resetPlayerSlot(player2);
    }

    private void resetGameLoop() {
        if (gameEngine != null)
            gameEngine.stopGame();
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

    private void checkGameOverState() {
        boolean p1Over = gameModel1.isGameOver();
        boolean p2Over = gameModel2.isGameOver();

        if (!p1Over && !p2Over)
            return;

        resetGameLoop();
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

    @Override
    public void cleanup() {
        if (gameEngine != null)
            gameEngine.stopGame();
    }

    // === ItemActivation Implementation (Delegated to engines via adapters usually,
    // but kept for interface compliance) ===
    @Override
    public void addClearingRow(int row) {
        // This might be called if something uses DualGameController as ItemActivation
        // directly.
        // But engines use adapters.
        // We can leave it empty or try to delegate if we know the target.
    }

    @Override
    public void addClearingCol(int col) {
    }

    @Override
    public void addClearingCells(List<Point> cells) {
    }

    // === Inner Adapter Class ===
    private class DualGameViewAdapter implements GameViewCallback {
        private final PlayerSlot slot;
        private final int playerIndex; // 1 or 2

        public DualGameViewAdapter(PlayerSlot slot, int playerIndex) {
            this.slot = slot;
            this.playerIndex = playerIndex;
        }

        @Override
        public void updateGameBoard() {
            DualGameController.this.updateGameBoard(slot);
        }

        @Override
        public void updateScoreDisplay() {
            DualGameController.this.updateScoreDisplay();
        }

        @Override
        public void updateLevelDisplay() {
            DualGameController.this.updateLevelDisplay();
        }

        @Override
        public void updateLinesDisplay() {
            DualGameController.this.updateLinesDisplay();
        }

        @Override
        public void updateNextBlockPreview() {
            slot.renderer.renderNextBlock(slot.nextBlockModel.peekNext());
        }

        @Override
        public void showGameOver() {
            checkGameOverState();
        }

        @Override
        public void updatePauseUI(boolean isPaused) {
            if (isPaused)
                showPauseOverlay();
            else
                hidePauseOverlay();
        }

        @Override
        public void addClearingRow(int row) {
            if (!slot.clearingRows.contains(row))
                slot.clearingRows.add(row);
        }

        @Override
        public void addClearingCol(int col) {
            if (!slot.clearingCols.contains(col))
                slot.clearingCols.add(col);
        }

        @Override
        public void addClearingCells(List<Point> cells) {
            for (Point cell : cells) {
                if (!slot.clearingCells.contains(cell))
                    slot.clearingCells.add(cell);
            }
        }
    }
}
