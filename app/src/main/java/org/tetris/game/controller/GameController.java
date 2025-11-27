package org.tetris.game.controller;

import java.util.List;

import org.tetris.Router;
import org.tetris.game.comand.RestartGameCommand;
import org.tetris.game.comand.TogglePauseCommand;
import org.tetris.game.engine.SingleGameEngine;
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
import org.tetris.game.view.GameViewCallback;

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

    public GameController(GameModel model) {
        super(model);
        this.gameModel = model;
    }

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
        if (player == null)
            return;
        player.clearingCells.addAll(cells);
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

    @Override
    public void initialize() {
        // 게임 모델 초기화 (Router에서 주입받거나 생성)
        if (gameModel == null) {
            gameModel = new GameModel();
        }

        // PlayerSlot 설정
        setupPlayerSlot();

        // 엔진 초기화 및 시작
        gameEngine = SingleGameEngine.builder()
                .player(player)
                .gameModel(gameModel)
                .controller(this)
                .build();

        // 키 핸들러 초기화 (엔진 필요)
        keyHandler = new GameKeyHandler(gameEngine);

        // 이벤트 핸들러 설정
        setupEventHandlers();

        // 게임 시작
        Platform.runLater(() -> {
            gameEngine.startGame(System.currentTimeMillis());
            root.requestFocus();
        });
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

    @Override
    public void setRouter(Router router) {
        this.router = router;
    }
}