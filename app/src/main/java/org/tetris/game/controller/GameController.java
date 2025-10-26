package org.tetris.game.controller;

import org.tetris.Router;
import org.tetris.game.model.Board;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.ScoreModel;
import org.tetris.game.model.NextBlockModel;
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

public class GameController extends BaseController<GameModel> implements RouterAware {
    
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
    
    /** 모델 및 기타 필드 **/

    private GameModel gameModel;
    private Board boardModel;
    private NextBlockModel nextBlockModel;
    private ScoreModel scoreModel;

    private Router router;
    private AnimationTimer gameLoop;
    private long lastUpdate = 0;
    private static final long FRAME_TIME = 16_666_667; // ~60 FPS in nanoseconds
    
    private Canvas boardCanvas;
    private GraphicsContext gc;
    private static final int CELL_SIZE = 26; // 각 셀의 크기 (픽셀)

    private int frameCounter = 0;
    private Point boardSize;
    
    private boolean isPaused = false;
    private boolean isGameOver = false;

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
        boardSize = boardModel.getSize();
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
        
        KeyCode code = e.getCode();

        switch (code) {
            case LEFT:
                boardModel.moveLeft();
                updateGameBoard();
                break;
            case RIGHT:
                boardModel.moveRight();
                updateGameBoard();
                break;
            case UP:
                boardModel.rotate();
                updateGameBoard();
                break;
            case DOWN:
                boardModel.moveDown();
                updateGameBoard();
                break;
            case SPACE:
                handleHardDrop();
                break;
            case P:
                togglePause();
                break;
            case C:
                // Hold (구현 예정)
                break;
            default:
                break;
        }

        e.consume();
    }
    
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }
                
                long elapsed = now - lastUpdate;
                if (elapsed >= FRAME_TIME) {
                    update();
                    lastUpdate = now;
                }
            }
        };
        gameLoop.start();
    }
    
    private void update() {
        
        frame++;
        model.autoDown();

        updateGameBoard();
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
    }
    
    private void lockCurrentBlock() {
        // 블럭 고정 및 라인 클리어
        int linesCleared = gameModel.lockBlockAndClearLines();
        
        // 새 블럭 생성 (실패하면 게임 오버)
        boolean spawned = gameModel.spawnNewBlock();
        
        if (!spawned) {
            handleGameOver();
        }
        
        updateGameBoard();
    }
    
    private void handleGameOver() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        showGameOver();
    }
    
    private void updateGameBoard() {
        if (gc == null) return;
        
        int[][] board = boardModel.getBoard();
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
                    // 셀 값에 따른 색상 매핑
                    gc.setFill(getCellColor(cellValue)); 
                    gc.fillRect(3 + c * CELL_SIZE, 3 + r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }
    
    // 셀 값에 따른 색상 반환
    private Color getCellColor(int cellValue) {
        switch (cellValue) {
            case 1: return GameColor.BLUE.getColor();    // IBlock
            case 2: return GameColor.ORANGE.getColor();  // JBlock
            case 3: return GameColor.YELLOW.getColor();  // LBlock
            case 4: return GameColor.GREEN.getColor();   // OBlock
            case 5: return GameColor.RED.getColor();     // SBlock
            case 6: return GameColor.PURPLE.getColor();  // TBlock
            case 7: return GameColor.CYAN.getColor();    // ZBlock
            default: return Color.WHITE;
        }
    }
    
    private void updateScoreDisplay() {
        scoreLabel.setText(scoreModel.toString());
    }
    
    private void updateLevelDisplay() {
        levelLabel.setText(String.valueOf(gameModel.getLevel()));
    }
    
    private void updateLinesDisplay() {
        linesLabel.setText(String.valueOf(gameModel.getTotalLinesCleared()));
    }
    
    private void togglePause() {
        pauseButton.setText(true ? "RESUME" : "PAUSE");
    }
    
    private void restartGame() {
        // 게임 상태 초기화
        gameModel.reset();
        
        // UI 초기화
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setManaged(false);
        pauseButton.setText("PAUSE");
        
        // 디스플레이 업데이트
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
        updateGameBoard();
        
        // 게임 루프 재시작
        if (gameLoop != null) {
            lastUpdate = 0;
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
    
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}
