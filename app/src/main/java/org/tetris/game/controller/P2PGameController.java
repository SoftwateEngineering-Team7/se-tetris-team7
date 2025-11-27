package org.tetris.game.controller;

import org.tetris.shared.BaseController;

import java.util.List;

import org.tetris.Router;
import org.tetris.game.engine.P2PGameEngine;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.P2PGameModel;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.view.GameViewCallback;
import org.tetris.game.view.GameViewRenderer;
import org.tetris.network.ClientThread;
import org.tetris.shared.RouterAware;
import org.util.KeyLayout;
import org.util.Point;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class P2PGameController extends BaseController<P2PGameModel> implements RouterAware, GameViewCallback {

    // === FXML 바인딩 ===
    @FXML
    private BorderPane root;

    // Local Player (Left)
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

    // Remote Player (Right)
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

    // Overlays
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
    private Label winnerLabel;

    private P2PGameModel p2pModel;
    private GameModel localGameModel;
    private GameModel remoteGameModel;

    private PlayerSlot localPlayer;
    private PlayerSlot remotePlayer;
    // private PlayerSlot activeItemTarget;

    private P2PGameEngine gameEngine;
    private GameKeyHandler keyHandler;
    private Router router;
    private ClientThread clientThread;

    private static final int MIN_CELL_SIZE = 16;
    private static final double PREVIEW_RATIO = 0.8;

    public P2PGameController(P2PGameModel model) {
        super(model);
        this.p2pModel = model;
        this.localGameModel = model.getLocalGameModel();
        this.remoteGameModel = model.getRemoteGameModel();
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
    }

    public void setClientThread(ClientThread clientThread) {
        this.clientThread = clientThread;
        if (gameEngine != null) {
            gameEngine.setClientThread(clientThread);
        }
    }

    @Override
    public void showGameOver() {
        showGameOverlay();
        // P2P 결과 처리는 별도 커맨드(GameResultCommand)로 처리되거나 여기서 라우팅
        // router.showScoreBoard(...);
    }

    private long seed;

    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public void initialize() {
        super.initialize();

        localGameModel.reset();
        remoteGameModel.reset();

        Platform.runLater(() -> {
            setupPlayerSlots();
            setupUI();

            gameEngine = P2PGameEngine.create()
                    .player(localPlayer)
                    .player2(remotePlayer)
                    .gameModel(p2pModel)
                    .controller(this)
                    .build();

            if (clientThread != null) {
                gameEngine.setClientThread(clientThread);
            }

            // Setup KeyHandler for Local Player
            keyHandler = new GameKeyHandler(gameEngine, KeyLayout.WASD); // Or ARROWS based on pref
            // Configure Local Commands
            keyHandler.setMoveLeftCommand(game -> {
                ((P2PGameEngine) game).doLocalMoveLeft();
                if (clientThread != null)
                    clientThread.sendCommand(new org.tetris.game.comand.MoveLeftCommand());
            });
            keyHandler.setMoveRightCommand(game -> {
                ((P2PGameEngine) game).doLocalMoveRight();
                if (clientThread != null)
                    clientThread.sendCommand(new org.tetris.game.comand.MoveRightCommand());
            });
            keyHandler.setRotateCommand(game -> {
                ((P2PGameEngine) game).doLocalRotate();
                if (clientThread != null)
                    clientThread.sendCommand(new org.tetris.game.comand.RotateCommand());
            });
            keyHandler.setSoftDropCommand(game -> {
                ((P2PGameEngine) game).doLocalSoftDrop();
                if (clientThread != null)
                    clientThread.sendCommand(new org.tetris.game.comand.SoftDropCommand());
            });
            keyHandler.setHardDropCommand(game -> {
                ((P2PGameEngine) game).doLocalHardDrop();
                if (clientThread != null)
                    clientThread.sendCommand(new org.tetris.game.comand.HardDropCommand());
            });
            // Pause command is default (togglePause), which P2PGameEngine handles (local
            // pause + notify?)

            gameEngine.startGame(seed);

            root.requestFocus();
        });

        setupEventHandlers();
    }

    private void setupPlayerSlots() {
        if (root.getScene() == null || root.getScene().getWindow() == null)
            return;

        Point boardSize = localGameModel.getBoardModel().getSize();
        int cellSize1 = calculateCellSize(gameBoard1, boardSize);
        int cellSize2 = calculateCellSize(gameBoard2, boardSize);
        int previewCellSize1 = Math.max(MIN_CELL_SIZE, (int) Math.round(cellSize1 * PREVIEW_RATIO));
        int previewCellSize2 = Math.max(MIN_CELL_SIZE, (int) Math.round(cellSize2 * PREVIEW_RATIO));

        GameViewRenderer renderer1 = new GameViewRenderer(gameBoard1, nextBlockPane1, boardSize, cellSize1,
                previewCellSize1);
        GameViewRenderer renderer2 = new GameViewRenderer(gameBoard2, nextBlockPane2, boardSize, cellSize2,
                previewCellSize2);

        localPlayer = new PlayerSlot(localGameModel, localGameModel.getBoardModel(), localGameModel.getNextBlockModel(),
                localGameModel.getScoreModel(), null, renderer1);
        remotePlayer = new PlayerSlot(remoteGameModel, remoteGameModel.getBoardModel(),
                remoteGameModel.getNextBlockModel(),
                remoteGameModel.getScoreModel(), null, renderer2);

        renderer1.setupSinglePlayerLayout();
        renderer2.setupSinglePlayerLayout();
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
        return Math.max(MIN_CELL_SIZE, cellSize > 0 ? cellSize : MIN_CELL_SIZE * 2);
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
                if (newScene != null)
                    setupKeyboardInput();
            });
        } else {
            setupKeyboardInput();
        }

        pauseButton.setOnAction(e -> gameEngine.togglePause());
        menuButton.setOnAction(e -> goToMenu());
        resumeButton.setOnAction(e -> resumeGame());
        pauseMenuButton.setOnAction(e -> goToMenu()); // P2P는 재시작 없음
    }

    private void setupKeyboardInput() {
        root.setFocusTraversable(true);
        root.requestFocus();
        root.getScene().setOnKeyPressed(this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent e) {
        if (gameEngine == null || keyHandler == null)
            return;

        keyHandler.handleKeyPress(e);
    }

    // === UI Updates called by Engine ===

    public void updateGameBoard() {
        updateGameBoard(localPlayer);
        updateGameBoard(remotePlayer);
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
        updateNextBlockPreview();
    }

    private void updateGameBoard(PlayerSlot slot) {
        if (slot == null)
            return;
        int[][] board = slot.boardModel.getBoard();
        slot.renderer.renderBoard(board, slot.flashMask, slot.isFlashing, slot.flashOn);
    }

    public void updatePauseUI(boolean isPaused) {
        if (isPaused)
            showPauseOverlay();
        else
            hidePauseOverlay();
    }

    @Override
    public void updateScoreDisplay() {
        scoreLabel1.setText(localPlayer.scoreModel.toString());
        scoreLabel2.setText(remotePlayer.scoreModel.toString());
    }

    @Override
    public void updateLevelDisplay() {
        levelLabel1.setText(String.valueOf(localGameModel.getLevel()));
        levelLabel2.setText(String.valueOf(remoteGameModel.getLevel()));
    }

    @Override
    public void updateLinesDisplay() {
        linesLabel1.setText(String.valueOf(localGameModel.getTotalLinesCleared()));
        linesLabel2.setText(String.valueOf(remoteGameModel.getTotalLinesCleared()));
    }

    @Override
    public void updateNextBlockPreview() {
        if (localPlayer != null)
            localPlayer.renderer.renderNextBlock(localPlayer.nextBlockModel.peekNext());
        if (remotePlayer != null)
            remotePlayer.renderer.renderNextBlock(remotePlayer.nextBlockModel.peekNext());
    }

    // === Overlays ===

    private void showPauseOverlay() {
        pauseOverlay.setVisible(true);
        pauseOverlay.setManaged(true);
    }

    private void hidePauseOverlay() {
        pauseOverlay.setVisible(false);
        pauseOverlay.setManaged(false);
        root.requestFocus();
    }

    private void hideGameOverlay() {
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setManaged(false);
    }

    private void showGameOverlay() {
        gameOverOverlay.setVisible(true);
        gameOverOverlay.setManaged(true);
    }

    private void resumeGame() {
        gameEngine.togglePause();
        root.requestFocus();
    }

    private void goToMenu() {
        if (gameEngine != null)
            gameEngine.gameOver(0); // Cleanup
        if (router != null)
            router.showStartMenu();
    }

    // === ItemActivation ===

    @Override
    public void addClearingRow(int row) {
        // TODO: Implement item logic
    }

    @Override
    public void addClearingCol(int col) {
        // TODO: Implement item logic
    }

    @Override
    public void addClearingCells(List<Point> cells) {
        // TODO: Implement item logic
    }
}
