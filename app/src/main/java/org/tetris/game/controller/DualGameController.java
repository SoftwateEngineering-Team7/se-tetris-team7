package org.tetris.game.controller;

import java.util.List;

import org.tetris.Router;
import org.tetris.game.model.DualGameModel;
import org.tetris.game.model.GameMode;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.model.items.ItemActivation;
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
    private Label winnerLabel;

    // === 모델 및 슬롯 ===
    private final DualGameModel dualGameModel;

    private PlayerSlot player1;
    private PlayerSlot player2;
    private PlayerSlot activeItemTarget;

    private Router router;

    private LocalMultiGameEngine gameEngine;
    private GameKeyHandler keyHandler1;
    private GameKeyHandler keyHandler2;

    private static final int MIN_CELL_SIZE = 16;
    private static final double PREVIEW_RATIO = 0.8;

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

        dualGameModel.getPlayer1GameModel().setItemMode(itemMode);
        dualGameModel.getPlayer2GameModel().setItemMode(itemMode);

        dualGameModel.getPlayer1GameModel().setDifficulty();
        dualGameModel.getPlayer2GameModel().setDifficulty();
    }

    // PlayerSlot 생성 로직 통합
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

        GameViewRenderer renderer = new GameViewRenderer(
                boardPane, nextPane, boardSize, cellSize, previewSize);

        return new PlayerSlot(
                gm,
                gm.getBoardModel(),
                gm.getNextBlockModel(),
                gm.getScoreModel(),
                am,
                renderer);

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

        return Math.max(MIN_CELL_SIZE, (cellSize <= 0) ? MIN_CELL_SIZE * 2 : cellSize);
    }

    // === UI 초기 세팅 ===
    private void setupUI() {
        hideGameOverlay();
        hidePauseOverlay();
        updateUI();
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

    // === 키 입력 처리 (통합) ===
    private void handleKeyPress(KeyEvent e) {
        if (e.getCode() == KeyCode.P) {
            togglePause();
            e.consume();
            return;
        }

        if (dualGameModel.getPlayer1GameModel().isPaused() || dualGameModel.getPlayer2GameModel().isPaused()) {
            e.consume();
            return;
        }
        if (player1 == null || player2 == null) {
            e.consume();
            return;
        }

        // P1 입력 (WASD + F)
        handlePlayerInput(e, player1, KeyCode.A, KeyCode.D, KeyCode.W, KeyCode.S, KeyCode.F);

        // P2 입력 (Arrow Keys + SLASH)
        handlePlayerInput(e, player2, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.UP, KeyCode.DOWN, KeyCode.SPACE);

        updateScoreDisplay();
        e.consume();
    }

    private void handlePlayerInput(
            KeyEvent e, PlayerSlot player,
            KeyCode left, KeyCode right, KeyCode rotate,
            KeyCode down, KeyCode hardDrop) {

        GameModel gm = player.gameModel;
        if (player.isFlashing || gm.isGameOver())
            return;

        KeyCode code = e.getCode();
        boolean updateNeeded = false;

        if (code == left) {
            player.boardModel.moveLeft();
            updateNeeded = true;
        } else if (code == right) {
            player.boardModel.moveRight();
            updateNeeded = true;
        } else if (code == rotate) {
            player.boardModel.rotate();
            updateNeeded = true;
        } else if (code == down) {
            if (player.boardModel.moveDown()) {
                player.scoreModel.softDrop(1);
            }
            updateNeeded = true;
        } else if (code == hardDrop) {
            handleHardDrop(player);
            return;
        }

        if (updateNeeded) {
            updateGameBoard(player);
            if (player1 != null)
                player1.renderer.renderNextBlock(player1.nextBlockModel.peekNext());
            if (player2 != null)
                player2.renderer.renderNextBlock(player2.nextBlockModel.peekNext());
        }
    }

    private void handleHardDrop(PlayerSlot player) {
        int dropDistance = player.boardModel.hardDrop();
        player.scoreModel.add(dropDistance * 2);

        player.lastDropTime = System.nanoTime();
        lockCurrentBlock(player);
    }

    // === 게임 루프 ===
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(player1 == null || player2 == null)
                    return;

                if (lastUpdate == 0L) {
                    lastUpdate = now;
                    player1.lastDropTime = now;
                    player2.lastDropTime = now;
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
        GameModel gm1 = player1.gameModel;
        GameModel gm2 = player2.gameModel;

        if (gm1.isPaused() || gm2.isPaused())
            return;
        if (player1 == null || player2 == null)
            return;

        handlePlayerUpdate(player1, now);
        handlePlayerUpdate(player2, now);

        updateUI();
        checkGameOverState();
    }

    private void handlePlayerUpdate(PlayerSlot player, long now) {
        GameModel gm = player.gameModel;
        if (gm.isGameOver())
            return;

        if (player.isFlashing) {
            tickFlash(player, now);
            return;
        }

        int dropIntervalFrames = gm.getDropInterval();
        long dropIntervalNanos = dropIntervalFrames * FRAME_TIME;

        if (now - player.lastDropTime >= dropIntervalNanos) {
            boolean moved = player.boardModel.moveDown();
            if (moved) {
                player.scoreModel.blockDropped();
            } else {
                lockCurrentBlock(player);
            }
            player.lastDropTime = now;
        }
    }

    private void checkGameOverState() {
        GameModel gm1 = player1.gameModel;
        GameModel gm2 = player2.gameModel;

        boolean p1Over = gm1.isGameOver();
        boolean p2Over = gm2.isGameOver();

        if (!p1Over && !p2Over)
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
        if (gameLoop != null)
            gameLoop.stop();
        showGameOverlay();

        String result;
        int p1Score = player1.scoreModel.getScore();
        int p2Score = player2.scoreModel.getScore();

        // 일반 승패 판정 (게임 오버 시)
        if (p1Over && p2Over)
            result = "DRAW";
        else if (p1Over)
            result = "PLAYER 2 WINS";
        else
            result = "PLAYER 1 WINS";

        if (winnerLabel != null)
            winnerLabel.setText(result);
        if (router != null) {
            // boolean isItemMode 사용
            boolean isItem = gm1.isItemMode();
            router.showScoreBoard(true, isItem, Math.max(p1Score, p2Score));
        }
    }

    // === 블록 고정 및 로직 ===
    private void lockCurrentBlock(PlayerSlot player) {
        GameModel gm = player.gameModel;
        List<Integer> fullRows = player.boardModel.findFullRows();
        player.clearingRows.addAll(fullRows);

        activeItemTarget = player;
        gm.tryActivateItem(this);
        activeItemTarget = null;

        if (player.boardModel.getIsForceDown()) {
            boolean moved = player.boardModel.moveDown(true);
            if (!moved) {
                gm.updateModels(0);
                player.boardModel.removeCurrentBlock();
                gm.spawnNewBlock();
                updateGameBoard(player);
                checkGameOverState();
            }
            return;
        }

        if (!player.clearingRows.isEmpty() || !player.clearingCols.isEmpty() || !player.clearingCells.isEmpty()) {
            beginFlash(player, System.nanoTime());
            return;
        }

        processIncomingAttacks(player);
        updateGameBoard(player);
        gm.spawnNewBlock();
        checkGameOverState();
    }

    private void beginFlash(PlayerSlot player, long now) {
        player.isFlashing = true;
        player.flashMask = player.renderer.buildFlashMask(player.clearingRows, player.clearingCols, player.clearingCells);
        player.flashOn = false;
        player.flashToggleCount = 0;
        player.nextFlashAt = now;
    }

    private void tickFlash(PlayerSlot player, long now) {
        if (player == null || !player.isFlashing || player.flashMask == null)
            return;
        if (now < player.nextFlashAt)
            return;

        player.flashOn = !player.flashOn;
        player.flashToggleCount++;
        player.nextFlashAt = now + FLASH_INTERVAL_NANOS;

        if (player.flashToggleCount >= FLASH_TOGGLES) {
            player.isFlashing = false;
            player.flashOn = false;
            player.flashMask = null;
            processClears(player);
        }
    }

    private void processClears(PlayerSlot player) {
        GameModel gm = player.gameModel;

        if (player.clearingRows.size() >= 1) {
            sendAttack(player);
        }
        player.boardModel.activeBlock = null;

        int linesCleared = deleteCompletedRows(player);
        int colsCleared = deleteCompletedCols(player);
        deleteCompletedCells(player);

        gm.updateModels(linesCleared + colsCleared);
        processIncomingAttacks(player);

        gm.spawnNewBlock();
        checkGameOverState();
    }

    // === 공격 로직 (PlayerSlot 활용) ===
    private void sendAttack(PlayerSlot attackerSlot) {
        // 상대방 찾기
        PlayerSlot targetSlot = (attackerSlot == player1) ? player2 : player1;

        for (int row : attackerSlot.clearingRows) {
            int[] attackLine = attackerSlot.boardModel.getRowForAttack(row);
            targetSlot.attackModel.push(attackLine);
        }
    }

    private void processIncomingAttacks(PlayerSlot player) {
        while (true) {
            int[] attackLine = player.attackModel.pop();
            if (attackLine == null)
                break;

            boolean success = player.boardModel.pushUp(attackLine);
            if (!success) {
                player.gameModel.setGameOver(true);
                break;
            }
        }
    }

    // === Helper Methods for Cleaning Lists ===
    private int deleteCompletedRows(PlayerSlot player) {
        for (int r : player.clearingRows)
            player.boardModel.clearRow(r);
        int count = player.clearingRows.size();
        player.clearingRows.clear();
        return count;
    }

    private int deleteCompletedCols(PlayerSlot player) {
        for (int c : player.clearingCols)
            player.boardModel.clearColumn(c);
        int count = player.clearingCols.size();
        player.clearingCols.clear();
        return count;
    }

    private void deleteCompletedCells(PlayerSlot player) {
        int h = player.boardModel.getSize().r, w = player.boardModel.getSize().c;
        for (Point p : player.clearingCells) {
            if (p.r >= 0 && p.r < h && p.c >= 0 && p.c < w)
                player.boardModel.getBoard()[p.r][p.c] = 0;
        }
        player.clearingCells.clear();
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
            levelLabel1.setText(String.valueOf(player1.gameModel.getLevel()));
            linesLabel1.setText(String.valueOf(player1.gameModel.getTotalLinesCleared()));
            player1.renderer.renderNextBlock(player1.nextBlockModel.peekNext());
        }
        if (player2 != null) {
            levelLabel2.setText(String.valueOf(player2.gameModel.getLevel()));
            linesLabel2.setText(String.valueOf(player2.gameModel.getTotalLinesCleared()));
            player2.renderer.renderNextBlock(player2.nextBlockModel.peekNext());
        }
    }

    private void updateGameBoard(PlayerSlot player) {
        if (player == null)
            return;
        player.renderer.renderBoard(player.boardModel.getBoard(), player.flashMask, player.isFlashing, player.flashOn);
    }

    private void updateScoreDisplay() {
        if (player1 != null)
            scoreLabel1.setText(player1.scoreModel.toString());
        if (player2 != null)
            scoreLabel2.setText(player2.scoreModel.toString());
    }

    // === Pause & Menu ===
    private void togglePause() {
        if (dualGameModel.getPlayer1GameModel().isGameOver() && dualGameModel.getPlayer2GameModel().isGameOver())
            return;
        boolean paused = !dualGameModel.getPlayer1GameModel().isPaused();
        dualGameModel.getPlayer1GameModel().setPaused(paused);
        dualGameModel.getPlayer2GameModel().setPaused(paused);

        if (paused)
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
        dualGameModel.getPlayer1GameModel().setPaused(false);
        dualGameModel.getPlayer2GameModel().setPaused(false);
        hidePauseOverlay();

        if (gameEngine != null)
            gameEngine.togglePause();
        root.requestFocus();
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
        if (router != null)
            router.showStartMenu();
    }

    private void goToMenuFromPause() {
        resetGameController();
        hidePauseOverlay();
        if (router != null)
            router.showStartMenu();
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
        if (gameLoop != null)
            gameLoop.stop();
        
        lastUpdate = 0L;

        if (player1 != null)
            player1.reset();
        if (player2 != null)
            player2.reset();
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
        if (activeItemTarget != null && !activeItemTarget.clearingRows.contains(row))
            activeItemTarget.clearingRows.add(row);
    }

    @Override
    public void addClearingCol(int col) {
        if (activeItemTarget != null && !activeItemTarget.clearingCols.contains(col))
            activeItemTarget.clearingCols.add(col);
    }

    @Override
    public void addClearingCells(List<Point> cells) {
        if (activeItemTarget != null && cells != null) {
            for (Point p : cells)
                if (!activeItemTarget.clearingCells.contains(p))
                    activeItemTarget.clearingCells.add(p);
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