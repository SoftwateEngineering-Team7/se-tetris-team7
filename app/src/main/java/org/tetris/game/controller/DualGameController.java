package org.tetris.game.controller;

import java.util.List;

import org.tetris.Router;
import org.tetris.game.engine.LocalMultiGameEngine;
import org.tetris.game.model.DualGameModel;
import org.tetris.game.model.GameMode;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.model.items.ItemActivation;
import org.tetris.game.view.GameViewRenderer;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;
import org.util.Point;
import org.util.KeyLayout;
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

public class DualGameController extends BaseController<DualGameModel> implements RouterAware, ItemActivation {

    // === FXML 바인딩 ===
    @FXML private BorderPane root;

    // Player 1 (왼쪽)
    @FXML private Pane gameBoard1;
    @FXML private Pane nextBlockPane1;
    @FXML private Label scoreLabel1;
    @FXML private Label levelLabel1;
    @FXML private Label linesLabel1;

    // Player 2 (오른쪽)
    @FXML private Pane gameBoard2;
    @FXML private Pane nextBlockPane2;
    @FXML private Label scoreLabel2;
    @FXML private Label levelLabel2;
    @FXML private Label linesLabel2;

    // 오버레이 & 버튼
    @FXML private HBox gameOverOverlay;
    @FXML private HBox pauseOverlay;
    @FXML private Button pauseButton;
    @FXML private Button restartButton;
    @FXML private Button menuButton;
    @FXML private Button resumeButton;
    @FXML private Button pauseMenuButton;
    @FXML private Label winnerLabel;

    private static final int MIN_CELL_SIZE = 16;
    private static final double PREVIEW_RATIO = 0.8;

    private DualGameModel dualGameModel;
    private PlayerSlot player1;
    private PlayerSlot player2;
    private LocalMultiGameEngine gameEngine;
    private GameKeyHandler keyHandler1;
    private GameKeyHandler keyHandler2;
    private Router router;
    private ItemActivation activeItemTarget;

    public DualGameController(DualGameModel model) {
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

        setupEventHandlers();

        Platform.runLater(() -> {
            setupPlayerSlots();
            setupGameEngine();
            setupUI();
            root.requestFocus();
        });
    }

    public void setUpGameMode(GameMode mode) {
        boolean itemMode = (mode == GameMode.ITEM);
        dualGameModel.getPlayer1GameModel().setItemMode(itemMode);
        dualGameModel.getPlayer2GameModel().setItemMode(itemMode);
        dualGameModel.getPlayer1GameModel().setDifficulty();
        dualGameModel.getPlayer2GameModel().setDifficulty();
    }

    private void setupPlayerSlots() {
        Point boardSize = dualGameModel.getPlayer1GameModel().getBoardModel().getSize();

        int cellSize1 = calculateCellSize(gameBoard1, boardSize);
        int cellSize2 = calculateCellSize(gameBoard2, boardSize);

        int previewCellSize1 = Math.max(MIN_CELL_SIZE, (int) Math.round(cellSize1 * PREVIEW_RATIO));
        int previewCellSize2 = Math.max(MIN_CELL_SIZE, (int) Math.round(cellSize2 * PREVIEW_RATIO));

        GameViewRenderer renderer1 = new GameViewRenderer(
                gameBoard1, nextBlockPane1, boardSize, cellSize1, previewCellSize1);
        
        GameViewRenderer renderer2 = new GameViewRenderer(
                gameBoard2, nextBlockPane2, boardSize, cellSize2, previewCellSize2);

        player1 = new PlayerSlot(
                dualGameModel.getPlayer1GameModel(),
                dualGameModel.getPlayer1GameModel().getBoardModel(),
                dualGameModel.getPlayer1GameModel().getNextBlockModel(),
                dualGameModel.getPlayer1GameModel().getScoreModel(),
                dualGameModel.getPlayer1AttackModel(),
                renderer1);

        player2 = new PlayerSlot(
                dualGameModel.getPlayer2GameModel(),
                dualGameModel.getPlayer2GameModel().getBoardModel(),
                dualGameModel.getPlayer2GameModel().getNextBlockModel(),
                dualGameModel.getPlayer2GameModel().getScoreModel(),
                dualGameModel.getPlayer2AttackModel(),
                renderer2);
    }

    private void setupGameEngine() {
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
                if (isPaused) showPauseOverlay();
                else hidePauseOverlay();
            }

            @Override public void addClearingRow(int row) {}
            @Override public void addClearingCol(int col) {}
            @Override public void addClearingCells(List<Point> cells) {}
        });

        // Key Handlers
        keyHandler1 = new GameKeyHandler(gameEngine, KeyLayout.WASD);

        keyHandler2 = new GameKeyHandler(gameEngine, KeyLayout.ARROWS);
        
        // Configure P2 Commands
        keyHandler2.setMoveLeftCommand(game -> ((LocalMultiGameEngine) game).moveLeftP2());
        keyHandler2.setMoveRightCommand(game -> ((LocalMultiGameEngine) game).moveRightP2());
        keyHandler2.setRotateCommand(game -> ((LocalMultiGameEngine) game).rotateP2());
        keyHandler2.setSoftDropCommand(game -> ((LocalMultiGameEngine) game).softDropP2());
        keyHandler2.setHardDropCommand(game -> ((LocalMultiGameEngine) game).hardDropP2());

        gameEngine.startGame(0);
    }

    private int calculateCellSize(Pane boardPane, Point boardSize) {
        double paneWidth = boardPane.getWidth();
        double paneHeight = boardPane.getHeight();

        if (paneWidth <= 0) paneWidth = boardPane.getPrefWidth();
        if (paneHeight <= 0) paneHeight = boardPane.getPrefHeight();

        if ((paneWidth <= 0 || paneHeight <= 0) && root.getScene() != null && root.getScene().getWindow() != null) {
            paneWidth = Math.max(paneWidth, root.getScene().getWindow().getWidth() / 2.0);
            paneHeight = Math.max(paneHeight, root.getScene().getWindow().getHeight() / 1.2);
        }

        double computed = Math.min(paneWidth / boardSize.c, paneHeight / boardSize.r);
        int cellSize = (int) Math.round(computed);

        return Math.max(MIN_CELL_SIZE, (cellSize <= 0) ? MIN_CELL_SIZE * 2 : cellSize);
    }

    private void setupUI() {
        hideGameOverlay();
        hidePauseOverlay();
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
        updateNextBlockPreview();
    }

    private void setupEventHandlers() {
        if (root.getScene() == null) {
            root.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) setupKeyboardInput();
            });
        } else {
            setupKeyboardInput();
        }

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

    private void handleKeyPress(KeyEvent e) {
        if (gameEngine == null) return;
        
        // Delegate to handlers
        // Note: GameKeyHandler consumes event if matched.
        // We try handler1 first, then handler2.
        
        keyHandler1.handleKeyPress(e);
        if (!e.isConsumed()) {
            keyHandler2.handleKeyPress(e);
        }
    }

    // === UI 업데이트 ===
    private void updateGameBoard(PlayerSlot slot) {
        if (slot == null) return;
        int[][] board = slot.boardModel.getBoard();
        slot.renderer.renderBoard(board, slot.flashMask, slot.isFlashing, slot.flashOn);
    }

    private void updateScoreDisplay() {
        if (player1 != null) scoreLabel1.setText(player1.scoreModel.toString());
        if (player2 != null) scoreLabel2.setText(player2.scoreModel.toString());
    }

    private void updateLevelDisplay() {
        if (player1 != null) levelLabel1.setText(String.valueOf(player1.gameModel.getLevel()));
        if (player2 != null) levelLabel2.setText(String.valueOf(player2.gameModel.getLevel()));
    }

    private void updateLinesDisplay() {
        if (player1 != null) linesLabel1.setText(String.valueOf(player1.gameModel.getTotalLinesCleared()));
        if (player2 != null) linesLabel2.setText(String.valueOf(player2.gameModel.getTotalLinesCleared()));
    }

    private void updateNextBlockPreview() {
        if (player1 != null) player1.renderer.renderNextBlock(player1.nextBlockModel.peekNext());
        if (player2 != null) player2.renderer.renderNextBlock(player2.nextBlockModel.peekNext());
    }

    // === Pause & Menu ===
    private void togglePause() {
        if (gameEngine != null) gameEngine.togglePause();
    }

    private void resumeGame() {
        if (gameEngine != null && gameEngine.isPaused()) {
            gameEngine.togglePause();
        }
        root.requestFocus();
    }

    private void restartGame() {
        if (gameEngine != null) {
            gameEngine.restartGame();
        }
        root.requestFocus();
    }

    private void goToMenu() {
        if (gameEngine != null) gameEngine.stopGame();
        hideGameOverlay();
        if (router != null) router.showStartMenu();
    }

    private void goToMenuFromPause() {
        if (gameEngine != null) gameEngine.stopGame();
        hidePauseOverlay();
        if (router != null) router.showStartMenu();
    }

    // === ItemActivation ===

    @Override
    public void addClearingRow(int row) {
        if (activeItemTarget != null) {
            activeItemTarget.addClearingRow(row);
        }
    }

    @Override
    public void addClearingCol(int col) {
        if (activeItemTarget != null) {
            activeItemTarget.addClearingCol(col);
        }
    }

    @Override
    public void addClearingCells(List<Point> cells) {
        if (activeItemTarget != null) {
            activeItemTarget.addClearingCells(cells);
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

    private void checkGameOverState() {
        boolean p1Over = dualGameModel.getPlayer1GameModel().isGameOver();
        boolean p2Over = dualGameModel.getPlayer2GameModel().isGameOver();

        if (!p1Over && !p2Over) return;

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

        if (router != null) {
            int bestScore = Math.max(player1.scoreModel.getScore(), player2.scoreModel.getScore());
            router.showScoreBoard(true, dualGameModel.getPlayer1GameModel().isItemMode(), bestScore);
        }
    }

    @Override
    public void cleanup() {
        if (gameEngine != null) {
            gameEngine.stopGame();
        }
    }
}