package org.tetris.game.controller;

import org.tetris.Router;
import org.tetris.game.model.Board;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.ScoreModel;
import org.tetris.game.model.blocks.Block;
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
    private HBox pauseOverlay;
    
    @FXML
    private Button restartButton;
    
    @FXML
    private Button menuButton;
    
    @FXML
    private Button resumeButton;
    
    @FXML
    private Button pauseMenuButton;
    
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
    private Canvas nextBlockCanvas;
    private GraphicsContext nextBlockGc;
    private static final int CELL_SIZE = 26; // 각 셀의 크기 (픽셀)
    private static final int PREVIEW_CELL_SIZE = 20; // 미리보기 셀 크기

    private int frame = 0;
    private Point boardSize;

    public GameController(Board model) {
        super(model);
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
    }

    @FXML
    protected void initialize() {
        super.initialize();
        
        setBoardSize();
        setupUI();
        setupEventHandlers();
        startGameLoop();
    }
    
    private void setupUI() {
        setupCanvas();
        setupNextBlockCanvas();
        
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
        updateNextBlockPreview();
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setManaged(false);
        pauseOverlay.setVisible(false);
        pauseOverlay.setManaged(false);
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
        
        // Pane의 크기가 결정된 후 Canvas를 중앙에 배치
        gameBoard.widthProperty().addListener((obs, oldVal, newVal) -> {
            double centerX = (newVal.doubleValue() - canvasWidth) / 2.0;
            boardCanvas.setLayoutX(centerX);
        });
        
        gameBoard.heightProperty().addListener((obs, oldVal, newVal) -> {
            double centerY = (newVal.doubleValue() - canvasHeight) / 2.0;
            boardCanvas.setLayoutY(centerY);
        });
        
        // 초기 보드 그리기
        updateGameBoard();
    }
    
    private void setupNextBlockCanvas() {
        // Next Block 프리뷰를 위한 Canvas 생성 (Pane과 같은 크기)
        double paneWidth = nextBlockPane.getPrefWidth();
        double paneHeight = nextBlockPane.getPrefHeight();
        
        nextBlockCanvas = new Canvas(paneWidth, paneHeight);
        nextBlockGc = nextBlockCanvas.getGraphicsContext2D();
        
        // nextBlockPane에 Canvas 추가
        nextBlockPane.getChildren().clear();
        nextBlockPane.getChildren().add(nextBlockCanvas);
        
        // Pane의 크기가 변경되면 Canvas 크기도 조정
        nextBlockPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            nextBlockCanvas.setWidth(newVal.doubleValue());
            updateNextBlockPreview();
        });
        
        nextBlockPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            nextBlockCanvas.setHeight(newVal.doubleValue());
            updateNextBlockPreview();
        });
        
        // 초기 Next Block 그리기
        updateNextBlockPreview();
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
    
    private void handleKeyPress(KeyEvent e) {
        
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
        updateNextBlockPreview();
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
                    gc.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                } else {
                    // 셀 값에 따른 색상 매핑
                    gc.setFill(getCellColor(cellValue)); 
                    gc.fillRect(
                        c * CELL_SIZE, 
                        r * CELL_SIZE, 
                        CELL_SIZE - 2, 
                        CELL_SIZE - 2
                    );
                    
                    // 테두리 효과
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(1);
                    gc.strokeRect(
                        c * CELL_SIZE, 
                        r * CELL_SIZE, 
                        CELL_SIZE - 2, 
                        CELL_SIZE - 2
                    );
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
    
    private void updateNextBlockPreview() {
        if (nextBlockGc == null) return;
        
        double canvasWidth = nextBlockCanvas.getWidth();
        double canvasHeight = nextBlockCanvas.getHeight();
        
        // Canvas 초기화 (투명 배경)
        nextBlockGc.clearRect(0, 0, canvasWidth, canvasHeight);
        
        // 다음 블록 가져오기
        Block nextBlock = nextBlockModel.peekNext();
        if (nextBlock == null) return;
 
        Color blockColor = nextBlock.getColor();
        
        // 블록 크기 계산
        int blockWidth = nextBlock.getSize().c;
        int blockHeight = nextBlock.getSize().r;
 
        // 중앙 정렬을 위한 오프셋 계산
        double offsetX = (canvasWidth - blockWidth * PREVIEW_CELL_SIZE) / 2.0;
        double offsetY = (canvasHeight - blockHeight * PREVIEW_CELL_SIZE) / 2.0;
        
        // 블록 그리기
        for (int r = 0; r < blockHeight; r++) {
            for (int c = 0; c < blockWidth; c++) {
                if (nextBlock.getCell(r, c) != 0) {
                    nextBlockGc.setFill(blockColor);
                    nextBlockGc.fillRect(
                        offsetX + c * PREVIEW_CELL_SIZE, 
                        offsetY + r * PREVIEW_CELL_SIZE, 
                        PREVIEW_CELL_SIZE - 2, 
                        PREVIEW_CELL_SIZE - 2
                    );
                    
                    // 테두리 효과
                    nextBlockGc.setStroke(Color.WHITE);
                    nextBlockGc.setLineWidth(1);
                    nextBlockGc.strokeRect(
                        offsetX + c * PREVIEW_CELL_SIZE, 
                        offsetY + r * PREVIEW_CELL_SIZE, 
                        PREVIEW_CELL_SIZE - 2, 
                        PREVIEW_CELL_SIZE - 2
                    );
                }
            }
        }
    }
    
    private void togglePause() {
        if (gameModel.isGameOver()) return;
        
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
    
    private void resumeGame() {
        gameModel.setPaused(false);
        hidePauseOverlay();
    }
    
    private void goToMenuFromPause() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (router != null) {
            router.showStartMenu();
        }
    }
    
    private void restartGame() {
        // model.reset();
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setManaged(false);
        pauseOverlay.setVisible(false);
        pauseOverlay.setManaged(false);
        
        // 디스플레이 업데이트
        updateScoreDisplay();
        updateLevelDisplay();
        updateLinesDisplay();
        updateGameBoard();
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
