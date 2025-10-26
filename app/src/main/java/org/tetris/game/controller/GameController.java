package org.tetris.game.controller;

import org.tetris.Router;
import org.tetris.game.model.Board;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;
import org.util.GameColor;
import org.util.Point;

import javafx.fxml.FXML;

import javafx.animation.AnimationTimer;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameController extends BaseController<Board> implements RouterAware {
    
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
    private Button restartButton;
    
    @FXML
    private Button menuButton;
    
    private Router router;
    private AnimationTimer gameLoop;
    private long lastUpdate = 0;
    private long lastDropTime = 0; // 마지막 블록 낙하 시간
    private static final long FRAME_TIME = 16_666_667; // ~60 FPS in nanoseconds
    
    private Canvas boardCanvas;
    private GraphicsContext gc;
    private static final int CELL_SIZE = 26; // 각 셀의 크기 (픽셀)

    private Point boardSize;

    public GameController(GameModel gameModel) {
        super(gameModel);
        this.gameModel = model;
        this.boardModel = model.getBoardModel();
        this.nextBlockModel = model.getNextBlockModel();
        this.scoreModel = model.getScoreModel();
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
    }

    @FXML
    protected void initialize() {
        super.initialize();
        
        // 이전 게임 루프가 있으면 정리
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        // 게임 상태 초기화
        gameModel.reset();
        lastUpdate = 0;
        lastDropTime = 0;
        
        setBoardSize();
        setupUI();
        setupEventHandlers();
        startGameLoop();
    }
    
    private void setupUI() {
        setupCanvas();
        
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setManaged(false);
    }
    
    private void setBoardSize(){
        boardSize = model.getSize();
    }

    private void setupCanvas() {
        // 보드 크기에 맞는 Canvas 생성
        int canvasHeight = boardSize.r * CELL_SIZE;
        int canvasWidth = boardSize.c * CELL_SIZE;

        boardCanvas = new Canvas(canvasWidth, canvasHeight);
        gc = boardCanvas.getGraphicsContext2D();
        
        // gameBoard Pane에 Canvas 추가
        gameBoard.getChildren().clear();
        gameBoard.getChildren().add(boardCanvas);
        
        // 초기 보드 그리기
        updateGameBoard();
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
    }
    
    private void setupKeyboardInput() {
        root.setFocusTraversable(true);
        root.requestFocus();
        
        root.getScene().setOnKeyPressed(this::handleKeyPress);
    }
    
    private void handleKeyPress(KeyEvent e) {
        if (gameModel.isGameOver() || gameModel.isPaused()) {
            if (e.getCode() == KeyCode.P) {
                togglePause();
            }
            e.consume();
            return;
        }
        
        KeyCode code = e.getCode();

        switch (code) {
            case LEFT:
                model.moveLeft();
                break;
            case RIGHT:
                model.moveRight();
                break;
            case UP:
                model.rotate();
                break;
            case DOWN:
                model.moveDown();
                break;
            case SPACE:
                // Hard drop (구현 예정)
                break;
            case C:
                // Hold (구현 예정)
                break;
            default:
                break;
        }
        updateGameBoard();

        e.consume();
    }
    
    private void handleHardDrop() {
        int dropDistance = boardModel.hardDrop();
        scoreModel.add(dropDistance * 2); // 하드 드롭 보너스
        lockCurrentBlock();
    }
    
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
                    update(now);
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }
    
    private void update(long now) {
        if (gameModel.isPaused() || gameModel.isGameOver()) {
            return;
        }
        
        // 레벨에 따른 블록 낙하 간격 (밀리초 단위)
        // dropInterval은 프레임 수이므로, 프레임당 시간(~16.6ms)을 곱함
        int dropIntervalFrames = gameModel.getDropInterval();
        long dropIntervalNanos = dropIntervalFrames * FRAME_TIME;
        
        // 시간 기반 블록 낙하 처리
        long timeSinceLastDrop = now - lastDropTime;
        if (timeSinceLastDrop >= dropIntervalNanos) {
            boolean moved = boardModel.autoDown();
            
            if (!moved) {
                lockCurrentBlock();
            }
            
            lastDropTime = now;
        }

        // UI는 매 프레임 업데이트 (60 FPS)
        updateGameBoard();
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
    }
    
    private void lockCurrentBlock() {
        // 블럭 고정 및 라인 클리어
        int linesCleared = gameModel.lockBlockAndClearLines();
        
        // 새 블럭 생성
        gameModel.spawnNewBlock();
        
        // Model 상태 확인 후 UI 업데이트
        if (gameModel.isGameOver()) {
            handleGameOver();
        }
        
        updateGameBoard();
    }
    
    // UI 처리만 담당 (게임 오버 판단은 Model에서)
    private void handleGameOver() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        showGameOver();
    }
    
    private void updateGameBoard() {
        if (gc == null) return;
        
        int[][] board = model.getBoard();
        int rows = boardSize.r;
        int cols = boardSize.c;

        // 전체 Canvas 초기화 (검은색 배경)
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, cols * CELL_SIZE, rows * CELL_SIZE);
        
        // 각 셀을 그리기
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int cellValue = board[r][c];
                
                if (cellValue == 0) {
                    // 빈 칸 - 검은색
                    gc.setFill(Color.BLACK);
                    gc.fillRect(3 + c * CELL_SIZE, 3 + r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                } else {
                    // TODO: 셀 값에 따른 색상 매핑 필요
                    gc.setFill(model.activeBlock.getColor()); 
                    gc.fillRect(3 + c * CELL_SIZE, 3 + r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }
    
    private void updateScoreDisplay() {
        scoreLabel.setText(String.valueOf(0));
    }
    
    private void updateLevelDisplay() {
        levelLabel.setText(String.valueOf(0));
    }
    
    private void updateLinesDisplay() {
        linesLabel.setText(String.valueOf(0));
    }
    
    private void togglePause() {
        if (gameModel.isGameOver()) return;
        
        gameModel.setPaused(!gameModel.isPaused());
        pauseButton.setText(gameModel.isPaused() ? "RESUME" : "PAUSE");
        
        if (!gameModel.isPaused()) {
            root.requestFocus();
        }
    }
    
    private void restartGame() {
        // model.reset();
        gameModel.reset();
        
        // UI 초기화
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setManaged(false);
        // 디스플레이 업데이트
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
        updateGameBoard();
        
        // 게임 루프 재시작
        if (gameLoop != null) {
            lastUpdate = 0;
            lastDropTime = 0;
            gameLoop.start();
        }
        
        root.requestFocus();
    }
    
    private void goToMenu() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (router != null) {
            router.showStartMenu();
        }
    }
    
    private void showGameOver() {
        gameOverOverlay.setVisible(true);
        gameOverOverlay.setManaged(true);
    }
    
    @Override
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }
}
