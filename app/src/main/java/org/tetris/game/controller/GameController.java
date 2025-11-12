package org.tetris.game.controller;

import java.util.ArrayList;
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

import javafx.application.Platform;
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
    private int cellSize = 26; // 각 셀의 크기 (픽셀)
    private static final int PREVIEW_CELL_SIZE = 20; // 미리보기 셀 크기

    private Point boardSize;

    // === 아래로 교체 ===
    private boolean isFlashing = false; // 애니메이션 진행 여부
    private boolean flashOn = false; // on/off 상태
    private int flashToggleCount = 0; // 토글 횟수
    private long nextFlashAt = 0L; // 다음 토글 시각

    // 격자 단위 마스크: true면 현재 플래시 대상 셀
    private boolean[][] flashMask;

    // 삭제 대상(애니메이션 종료 시 실제로 지울 것들)
    public static List<Integer> clearingRows = new java.util.ArrayList<>();
    public static List<Integer> clearingCols = new java.util.ArrayList<>();
    public static List<Point> clearingCells = new java.util.ArrayList<>();

    private static final int FLASH_TIMES = 2;
    private static final int FLASH_TOGGLES = FLASH_TIMES * 2; // on/off 합계
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
    public void initialize() {
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
        Platform.runLater(() -> setupUI());
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
        double stageWidth = root.getScene().getWindow().getWidth();
        double stageHeight = root.getScene().getWindow().getHeight();

        // 화면 크기에 따라 셀 크기 비율 계산
        cellSize = (int) Math.round(Math.min((stageWidth - 450) / 13, stageHeight / 23));

        // 보드 크기에 맞는 Canvas 생성
        int canvasHeight = boardSize.r * cellSize;
        int canvasWidth = boardSize.c * cellSize;

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
            boolean moved = boardModel.moveDown();
            if (moved) {
                scoreModel.softDrop(1); // 수동으로 1칸 내릴 때 점수
            }
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
    
    /**
     * 게임 모드 설정
     * @param itemMode 아이템 모드 여부
     * @param difficulty 난이도
     */
    public void setUpGameMode(boolean itemMode)
    {
        gameModel.setItemMode(itemMode);
        gameModel.setDifficulty();
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
        if (isFlashing) {
            tickFlash(now);
            updateGameBoard();
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
            boolean moved = gameModel.autoDown();

            if (moved) {
                // 자동으로 1칸 떨어질 때마다 점수 획득
                scoreModel.blockDropped();
            } else {
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
        clearingRows.clear();
        clearingCols.clear();
        clearingCells.clear();

        List<Integer> fullRows = boardModel.findFullRows();
        List<Integer> fullCols = new ArrayList<>(); // 이미 가지고 계신 리스트 사용

        clearingRows.addAll(fullRows);

        gameModel.tryActivateItem();

        if (boardModel.getIsForceDown()) {
            boardModel.moveDownForce();
            return;
        }

        if (!clearingRows.isEmpty() || !clearingCols.isEmpty() || !clearingCells.isEmpty()) {
            beginFlash(fullRows, fullCols, java.util.Collections.emptyList(), System.nanoTime());
            return; // 애니메이션 끝나면 실제 삭제 수행
        }

        // 평상시 처리
        gameModel.updateModels(0); // 필요 시 오버로드(행/열 분리)로 교체 권장
        gameModel.spawnNewBlock();
        if (gameModel.isGameOver())
            handleGameOver();
        updateGameBoard();
    }

    // 행/열/임의셀을 한 번에 받는 시작 진입점
    private void beginFlash(List<Integer> rows, List<Integer> cols, List<Point> cells, long now) {
        isFlashing = true;

        if (rows != null)
            clearingRows.addAll(rows);
        if (cols != null)
            clearingCols.addAll(cols);
        if (cells != null)
            clearingCells.addAll(cells);

        clearingRows.sort(java.util.Comparator.reverseOrder());
        clearingCols.sort(java.util.Comparator.reverseOrder());

        // 마스크 초기화
        flashMask = new boolean[boardSize.r][boardSize.c];
        markFlashRows(clearingRows, flashMask);
        markFlashCols(clearingCols, flashMask);
        markFlashCells(clearingCells, flashMask);

        flashOn = false; // 첫 토글에서 on
        flashToggleCount = 0;
        nextFlashAt = now; // 즉시 시작
    }

    private void tickFlash(long now) {
        if (now < nextFlashAt)
            return;

        flashOn = !flashOn;
        flashToggleCount++;
        nextFlashAt = now + FLASH_INTERVAL_NANOS;

        if (flashToggleCount >= FLASH_TOGGLES) {
            isFlashing = false;
            flashOn = false;
            processClears(); // 실제 삭제
            flashMask = null;
        }
    }

    private void processClears() {
        for (int r : clearingRows)
            boardModel.clearRow(r);
        int linesCleared = clearingRows.size();

        for (int c : clearingCols)
            boardModel.clearColumn(c);
        int colsCleared = clearingCols.size();

        for (Point p : clearingCells) {
            if (p.r >= 0 && p.r < boardSize.r && p.c >= 0 && p.c < boardSize.c) {
                boardModel.getBoard()[p.r][p.c] = 0;
            }
        }

        clearingRows.clear();
        clearingCols.clear();
        clearingCells.clear();

        gameModel.updateModels(linesCleared + colsCleared);
        gameModel.spawnNewBlock();
        if (gameModel.isGameOver())
            handleGameOver();
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
        gc.fillRect(0, 0, cols * cellSize, rows * cellSize);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boolean flashingThisCell = isFlashing && flashMask != null && flashMask[r][c] && flashOn;
                int cellValue = board[r][c];

                if (cellValue == 0 && !flashingThisCell)
                    continue;

                Color fill = flashingThisCell ? Color.WHITE : getCellColor(cellValue);
                gc.setFill(fill);
                gc.fillRect(c * cellSize, r * cellSize, cellSize - 2, cellSize - 2);

                gc.setStroke(Color.WHITE);
                gc.setLineWidth(1);
                gc.strokeRect(c * cellSize, r * cellSize, cellSize - 2, cellSize - 2);

                // 셀 내 문자 그리기
                String cellText = getCellText(cellValue);
                if (!cellText.isEmpty()) {
                    gc.setFill(Color.BLACK); // 텍스트 색상
                    gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, cellSize * 0.6));

                    // 텍스트 중앙 정렬
                    javafx.scene.text.Text text = new javafx.scene.text.Text(cellText);
                    text.setFont(gc.getFont());
                    double textWidth = text.getBoundsInLocal().getWidth();
                    double textHeight = text.getBoundsInLocal().getHeight();

                    double textX = c * cellSize + (cellSize - textWidth) / 2;
                    double textY = r * cellSize + (cellSize + textHeight) / 2 - 2;

                    gc.fillText(cellText, textX, textY);
                }
                
            }
        }
    }

    public static void addClearingRow(int row) {
        if (!clearingRows.contains(row)) {
            clearingRows.add(row);
        }
    }

    public static void addClearingCol(int col) {
        if (!clearingCols.contains(col)) {
            clearingCols.add(col);
        }
    }

    public static void addClearingCells(List<Point> cells) {
        for (Point cell : cells) {
            if (!clearingCells.contains(cell)) {
                clearingCells.add(cell);
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

    // 셀 값에 따른 문자 반환
    private String getCellText(int cellValue) {
        switch (cellValue) {
            case 9:
                return "L";
            case 10:
                return "W";
            case 11:
                return "V";
            case 12:
                return "B";
            case 13:
                return "C";
            default:
                return "";
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

                String cellText = getCellText(nextBlock.getCell(r, c));
                if (!cellText.isEmpty()) {
                    gc.setFill(Color.BLACK); // 텍스트 색상
                    gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, cellSize * 0.6));

                    // 텍스트 중앙 정렬
                    javafx.scene.text.Text text = new javafx.scene.text.Text(cellText);
                    text.setFont(gc.getFont());
                    double textWidth = text.getBoundsInLocal().getWidth();
                    double textHeight = text.getBoundsInLocal().getHeight();

                    double textX = c * cellSize + (cellSize - textWidth) / 2;
                    double textY = r * cellSize + (cellSize + textHeight) / 2 - 2;

                    gc.fillText(cellText, textX, textY);
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

    private void markFlashRows(List<Integer> rows, boolean[][] mask) {
        if (rows == null)
            return;
        for (int r : rows) {
            if (r < 0 || r >= boardSize.r)
                continue;
            java.util.Arrays.fill(mask[r], true);
        }
    }

    private void markFlashCols(List<Integer> cols, boolean[][] mask) {
        if (cols == null)
            return;
        for (int c : cols) {
            if (c < 0 || c >= boardSize.c)
                continue;
            for (int r = 0; r < boardSize.r; r++) {
                mask[r][c] = true;
            }
        }
    }

    private void markFlashCells(List<Point> cells, boolean[][] mask) {
        if (cells == null)
            return;
        for (Point p : cells) {
            if (p.r >= 0 && p.r < boardSize.r && p.c >= 0 && p.c < boardSize.c) {
                mask[p.r][p.c] = true;
            }
        }
    }

    private void showGameOver() {
        gameOverOverlay.setVisible(true);
        gameOverOverlay.setManaged(true);
        
        router.showScoreBoard(true, gameModel.isItemMode(), scoreModel.getScore());
    }

    @Override
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }
}