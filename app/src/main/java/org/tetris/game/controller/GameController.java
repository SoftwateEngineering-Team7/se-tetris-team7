package org.tetris.game.controller;

import java.util.List;

import org.tetris.Router;

import org.tetris.game.model.Board;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.ScoreModel;
import org.tetris.game.model.blocks.Block;

import org.tetris.game.model.NextBlockModel;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.view.GameViewRenderer;
import org.tetris.network.comand.*;
import org.tetris.network.game.SingleGameEngine;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;
import org.tetris.game.view.GameViewCallback;

import org.util.KeyLayout;
import org.util.Point;

import javafx.fxml.FXML;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class GameController extends BaseController<GameModel> implements RouterAware, GameViewCallback {

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
    private SingleGameEngine gameEngine;
    private GameKeyHandler keyHandler;

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

        // 게임 상태 초기화
        gameModel.reset();

        // Stage 크기가 잡힌 후 PlayerSlot + UI 세팅
        Platform.runLater(() -> {
            setupPlayerSlot();
            setupUI();

            // 엔진 초기화 및 시작
            gameEngine = new SingleGameEngine(player, gameModel, this);
            keyHandler = new GameKeyHandler(gameEngine); // Default WASD or ARROWS? Let's check KeyLayout default.
            // Actually GameKeyHandler default is WASD. Single player usually uses Arrows or
            // WASD.
            // Let's use ARROWS for single player as it's more standard for casual play, or
            // WASD if preferred.
            // The original code checked KeyLayout.getUpKey(), which depends on
            // KeyLayout.currentLayout.
            // So we should pass KeyLayout.ARROWS or WASD based on
            // KeyLayout.getCurrentLayout().
            // But KeyLayout.getCurrentLayout() returns a String.
            // Let's just use KeyLayout.ARROWS for now or respect KeyLayout.
            if (KeyLayout.getCurrentLayout().equals(KeyLayout.KEY_WASD)) {
                keyHandler = new GameKeyHandler(gameEngine, KeyLayout.WASD);
            } else {
                keyHandler = new GameKeyHandler(gameEngine, KeyLayout.ARROWS);
            }

            gameEngine.startGame(0);
        });

        setupEventHandlers();
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

        this.player = new PlayerSlot(boardModel, nextBlockModel, scoreModel, renderer);
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
        pauseButton.setOnAction(e -> new TogglePauseCommand().execute(gameEngine));
        restartButton.setOnAction(e -> new RestartGameCommand().execute(gameEngine));
        menuButton.setOnAction(e -> goToMenu());
        resumeButton.setOnAction(e -> resumeGame());
        pauseMenuButton.setOnAction(e -> goToMenuFromPause());
    }

    private void setupKeyboardInput() {
        root.setFocusTraversable(true);
        root.requestFocus();

        root.getScene().setOnKeyPressed(this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent event) {
        if (gameEngine == null || keyHandler == null)
            return;

        keyHandler.handleKeyPress(event);
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

    // ===== 렌더링 =====

    public void updateGameBoard() {
        int[][] board = player.boardModel.getBoard();
        player.renderer.renderBoard(board, player.flashMask, player.isFlashing, player.flashOn);
    }

    public void updateNextBlockPreview() {
        Block nextBlock = player.nextBlockModel.peekNext();
        player.renderer.renderNextBlock(nextBlock);
    }

    public void updateScoreDisplay() {
        scoreLabel.setText(player.scoreModel.toString());
    }

    public void updateLevelDisplay() {
        levelLabel.setText(String.valueOf(gameModel.getLevel()));
    }

    public void updateLinesDisplay() {
        linesLabel.setText(String.valueOf(gameModel.getTotalLinesCleared()));
    }

    // ===== 일시정지 / 재시작 / 메뉴 이동 =====

    private void showPauseOverlay() {
        pauseOverlay.setVisible(true);
        pauseOverlay.setManaged(true);
    }

    private void hidePauseOverlay() {
        pauseOverlay.setVisible(false);
        pauseOverlay.setManaged(false);
        root.requestFocus();
    }

    public void updatePauseUI(boolean isPaused) {
        if (isPaused) {
            showPauseOverlay();
        } else {
            hidePauseOverlay();
        }
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

        if (gameEngine != null) {
            // gameEngine.resume(); // 필요한 경우 추가
        }
        root.requestFocus();
    }

    private void goToMenuFromPause() {
        if (gameEngine != null) {
            gameEngine.stopGame();
        }
        hidePauseOverlay();

        if (router != null) {
            router.showStartMenu();
        }
    }

    private void goToMenu() {
        hideGameOverlay();

        if (router != null) {
            router.showStartMenu();
        }
    }

    public void showGameOver() {
        showGameOverlay();

        router.showScoreBoard(true, gameModel.isItemMode(), player.scoreModel.getScore());
    }

    @Override
    public void cleanup() {
        if (gameEngine != null) {
            gameEngine.stopGame();
        }
    }
}