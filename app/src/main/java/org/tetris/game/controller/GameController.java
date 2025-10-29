package org.tetris.game.controller;

import java.util.List;

import org.tetris.Router;
import org.tetris.game.model.Board;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.ScoreModel;
import org.tetris.game.model.blocks.Block;
import org.tetris.game.model.NextBlockModel;
import org.tetris.shared.BaseController;
import org.tetris.shared.RouterAware;
import org.util.GameColor;
import org.util.KeyLayout;
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
    private long lastDropTime = 0; // 마지막 블록 낙하 시간
    private static final long FRAME_TIME = 16_666_667; // ~60 FPS in nanoseconds

    private Canvas boardCanvas;
    private GraphicsContext gc;
    private Canvas nextBlockCanvas;
    private GraphicsContext nextBlockGc;
    private static final int CELL_SIZE = 26; // 각 셀의 크기 (픽셀)
    private static final int PREVIEW_CELL_SIZE = 20; // 미리보기 셀 크기

    private Point boardSize;

    private boolean isPaused = false;
    private boolean isGameOver = false;

    // 애니메이션을 위한 필드 추가
    private boolean isFlashingRows = false;
    private boolean[] rowFlashMask; // r 인덱스별로 해당 행이 플래시 대상인지
    private List<Integer> clearingRows; // 애니메이션 끝나고 실제로 지울 행들(원본 인덱스)
    private boolean flashOn = false; // 현재 흰색으로 켜진 상태인지
    private int flashToggleCount = 0; // on/off 토글 횟수
    private long nextFlashAt = 0L;

    private static final int FLASH_TIMES = 2; // 2번 반짝
    private static final int FLASH_TOGGLES = FLASH_TIMES * 2; // on/off 합쳐서 4회 토글
    private static final long FLASH_INTERVAL_NANOS = 100_000_000L; // 100ms

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

    private void setBoardSize() {
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
        if (gameModel.isGameOver() || gameModel.isPaused()) {
            if (e.getCode() == KeyCode.P) {
                togglePause();
            }
            e.consume();
            return;
        }

        KeyCode code = e.getCode();

        // switch문으로는 static 메서드 호출이 불가능하므로 if-else로 처리
        if(code == KeyLayout.getLeftKey()) {
            boardModel.moveLeft();
            updateGameBoard();
        } else if(code == KeyLayout.getRightKey()) {
            boardModel.moveRight();
            updateGameBoard();
        } else if(code == KeyLayout.getUpKey()) {
            boardModel.rotate();
            updateGameBoard();
        } else if(code == KeyLayout.getDownKey()) {
            boardModel.moveDown();
            updateGameBoard();
        } else if(code == KeyCode.SPACE) {
            handleHardDrop();
        } else if(code == KeyCode.P) {
            togglePause();
        } else{
            // 기타 키는 무시
        }

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
        // 애니메이션 동안은 일반 업데이트 중지
        if (isFlashingRows) {
            tickRowFlash(now);
            updateGameBoard(); // 매 프레임 다시 그림
            return;
        }

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
        updateNextBlockPreview();
    }

    private void lockCurrentBlock() {
        // 라인 검사
        List<Integer> fullRows = boardModel.findFullRows();

        if (!fullRows.isEmpty()) {
            // 애니메이션부터 시작
            beginRowFlash(fullRows, System.nanoTime());
            return;
        }

        // 평상시 처리 (라인이 없으면 모델 업데이트/스폰만)
        gameModel.updateModels(0);
        gameModel.spawnNewBlock();

        if (gameModel.isGameOver()) {
            handleGameOver();
        }

        updateGameBoard();
    }

    // 플래시 애니메이션 시작
    private void beginRowFlash(List<Integer> rows, long now) {
        isFlashingRows = true;
        clearingRows = new java.util.ArrayList<>(rows);
        // 실제 삭제 시 인덱스 보존을 위해 내림차순으로 정렬(아래에서 위로 지움)
        clearingRows.sort(java.util.Comparator.reverseOrder());

        // 마스크 초기화
        rowFlashMask = new boolean[boardSize.r];
        for (int r : rows)
            rowFlashMask[r] = true;

        flashOn = false; // 첫 토글에서 on으로 바뀌게
        flashToggleCount = 0;
        nextFlashAt = now; // 즉시 첫 토글 시작
    }

    private void tickRowFlash(long now) {
        if (now < nextFlashAt)
            return;

        // on/off 토글
        flashOn = !flashOn;
        flashToggleCount++;
        nextFlashAt = now + FLASH_INTERVAL_NANOS;

        if (flashToggleCount >= FLASH_TOGGLES) {
            // 실제 삭제 수행
            for (int r : clearingRows) {
                boardModel.clearRow(r);
            }
            int linesCleared = clearingRows.size();

            // 상태 리셋
            isFlashingRows = false;
            flashOn = false;
            clearingRows.clear();
            rowFlashMask = null;

            // 모델 업데이트 및 새 블록
            gameModel.updateModels(linesCleared);
            gameModel.spawnNewBlock();

            if (gameModel.isGameOver()) {
                handleGameOver();
            }
        }
    }

    // UI 처리만 담당 (게임 오버 판단은 Model에서)
    private void handleGameOver() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        showGameOver();
    }

    private void updateGameBoard() {
        if (gc == null)
            return;

        int[][] board = boardModel.getBoard();
        int rows = boardSize.r;
        int cols = boardSize.c;

        // 전체 Canvas 초기화 (검은색 배경)
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, cols * CELL_SIZE, rows * CELL_SIZE);

        // 각 셀을 그리기
        for (int r = 0; r < rows; r++) {
            final boolean flashThisRow = isFlashingRows && rowFlashMask != null && rowFlashMask[r] && flashOn;
            for (int c = 0; c < cols; c++) {
                int cellValue = board[r][c];

                if (cellValue == 0 && !flashThisRow) {
                    // 빈 칸(플래시 아님)
                    gc.setFill(Color.BLACK);
                    gc.fillRect(
                        c * CELL_SIZE, 
                        r * CELL_SIZE, 
                        CELL_SIZE, 
                        CELL_SIZE);
                    continue;
                }

                // 플래시 중인 행은 백색으로, 아니면 원래 색상
                Color fill = flashThisRow ? Color.WHITE : getCellColor(cellValue);
                gc.setFill(fill);
                gc.fillRect(
                    c * CELL_SIZE, 
                    r * CELL_SIZE, 
                    CELL_SIZE - 2, 
                    CELL_SIZE - 2);

                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1);
                gc.strokeRect(
                    c * CELL_SIZE, 
                    r * CELL_SIZE, 
                    CELL_SIZE - 2, 
                    CELL_SIZE - 2);
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
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (router != null) {
            router.showStartMenu();
        }
    }

    private void restartGame() {
        // model.reset();
        gameModel.reset();

        // UI 초기화
        gameOverOverlay.setVisible(false);
        gameOverOverlay.setManaged(false);
        pauseOverlay.setVisible(false);
        pauseOverlay.setManaged(false);

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