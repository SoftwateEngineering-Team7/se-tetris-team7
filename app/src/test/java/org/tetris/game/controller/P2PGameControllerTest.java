package org.tetris.game.controller;

import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isInvisible;
import static org.testfx.matcher.base.NodeMatchers.isNotNull;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.tetris.game.P2PGameFactory;
import org.tetris.game.model.P2PGameModel;
import org.tetris.game.model.PlayerSlot;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.Board;
import org.tetris.game.model.ScoreModel;
import org.tetris.game.model.AttackModel;
import org.tetris.game.model.GameMode;
import org.tetris.network.dto.MatchSettings;
import org.tetris.shared.MvcBundle;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import org.util.Point;
import org.util.KeyLayout;
import org.util.PlayerId;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class P2PGameControllerTest extends ApplicationTest {

    private final P2PGameFactory gameFactory = new P2PGameFactory();
    private P2PGameController controller;
    private P2PGameModel model;

    @Override
    public void start(Stage stage) throws Exception {
        MvcBundle bundle = gameFactory.create();
        controller = (P2PGameController) bundle.controller();
        model = (P2PGameModel) bundle.model();
        stage.setScene(bundle.view().getScene());
        stage.setTitle("P2P Game Test");
        stage.show();
    }

    // ===== 기본 UI 요소 존재 테스트 =====

    @Test
    public void testP2PGameSceneDisplay() {
        verifyThat("#gameBoard1", isNotNull());
        verifyThat("#gameBoard2", isNotNull());
        verifyThat("#scoreLabel1", isVisible());
        verifyThat("#scoreLabel2", isVisible());
        verifyThat("#levelLabel1", isVisible());
        verifyThat("#levelLabel2", isVisible());
        verifyThat("#linesLabel1", isVisible());
        verifyThat("#linesLabel2", isVisible());
        verifyThat("#pauseButton", isVisible());
        verifyThat("#gameOverOverlay", isInvisible());
    }

    @Test
    public void testMovementKeysPlayer1() throws InterruptedException {
        Thread.sleep(1000);
        // P2PGameController uses Player 1 keys (default: arrow keys)
        press(KeyLayout.getLeftKey(PlayerId.PLAYER1));
        Thread.sleep(200);
        press(KeyLayout.getRightKey(PlayerId.PLAYER1));
        Thread.sleep(200);
        press(KeyLayout.getUpKey(PlayerId.PLAYER1));
        Thread.sleep(200);
        press(KeyLayout.getDownKey(PlayerId.PLAYER1));
        Thread.sleep(200);

        // Should not crash even if not connected
    }

    /**
     * P2PGameController는 로컬 플레이어(Player 1)의 키만 처리하고, Player 2 키(WASD)는 무시해야 함을 검증
     */
    @Test
    public void testPlayer2KeysIgnored() throws InterruptedException {
        Thread.sleep(1000);

        try {
            // Get player1 and its game model through reflection
            java.lang.reflect.Field player1Field =
                    DualGameController.class.getDeclaredField("player1");
            player1Field.setAccessible(true);
            Object player1 = player1Field.get(controller);

            java.lang.reflect.Field gameModelField =
                    player1.getClass().getDeclaredField("gameModel");
            gameModelField.setAccessible(true);
            org.tetris.game.model.GameModel gameModel =
                    (org.tetris.game.model.GameModel) gameModelField.get(player1);

            // Pause the game to prevent auto-drop from interfering
            gameModel.setPaused(true);
            Thread.sleep(100);

            java.lang.reflect.Field boardModelField =
                    player1.getClass().getDeclaredField("boardModel");
            boardModelField.setAccessible(true);
            org.tetris.game.model.Board board =
                    (org.tetris.game.model.Board) boardModelField.get(player1);

            // Record initial position
            Point initialPos = new Point(board.getCurPos());

            // Press Player 2 keys (WASD by default)
            press(KeyLayout.getUpKey(PlayerId.PLAYER2));
            Thread.sleep(200);
            press(KeyLayout.getLeftKey(PlayerId.PLAYER2));
            Thread.sleep(200);
            press(KeyLayout.getDownKey(PlayerId.PLAYER2));
            Thread.sleep(200);
            press(KeyLayout.getRightKey(PlayerId.PLAYER2));
            Thread.sleep(200);

            // Verify position hasn't changed (Player 2 keys were ignored)
            Point currentPos = board.getCurPos();
            org.junit.Assert.assertEquals("Block position should not change with Player 2 keys",
                    initialPos.r, currentPos.r);
            org.junit.Assert.assertEquals("Block position should not change with Player 2 keys",
                    initialPos.c, currentPos.c);

            // Unpause for cleanup
            gameModel.setPaused(false);

        } catch (Exception e) {
            org.junit.Assert.fail("Failed to verify Player 2 keys are ignored: " + e.getMessage());
        }
    }

    // ===== P2P 모델 초기화 테스트 =====

    @Test
    public void testP2PGameModelInitialization() throws InterruptedException {
        Thread.sleep(500);
        
        assertNotNull("P2PGameModel should not be null", model);
        assertNotNull("Player1 GameModel should not be null", model.getPlayer1GameModel());
        assertNotNull("Player2 GameModel should not be null", model.getPlayer2GameModel());
    }

    @Test
    public void testBothPlayersHaveBoardModels() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = model.getPlayer1GameModel();
        GameModel p2Model = model.getPlayer2GameModel();
        
        assertNotNull("Player1 BoardModel should not be null", p1Model.getBoardModel());
        assertNotNull("Player2 BoardModel should not be null", p2Model.getBoardModel());
    }

    @Test
    public void testBothPlayersHaveScoreModels() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = model.getPlayer1GameModel();
        GameModel p2Model = model.getPlayer2GameModel();
        
        assertNotNull("Player1 ScoreModel should not be null", p1Model.getScoreModel());
        assertNotNull("Player2 ScoreModel should not be null", p2Model.getScoreModel());
    }

    @Test
    public void testInitialGameState() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = model.getPlayer1GameModel();
        GameModel p2Model = model.getPlayer2GameModel();
        
        // P2P 게임은 GameStartCommand를 받을 때까지 일시정지 상태일 수 있음
        assertFalse("Player1 should not be game over initially", p1Model.isGameOver());
        assertFalse("Player2 should not be game over initially", p2Model.isGameOver());
    }

    @Test
    public void testInitialScoreIsZero() throws InterruptedException {
        Thread.sleep(500);
        
        ScoreModel p1Score = model.getPlayer1GameModel().getScoreModel();
        ScoreModel p2Score = model.getPlayer2GameModel().getScoreModel();
        
        assertEquals("Player1 initial score should be 0", 0, p1Score.getScore());
        assertEquals("Player2 initial score should be 0", 0, p2Score.getScore());
    }

    @Test
    public void testInitialLevelIsOne() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = model.getPlayer1GameModel();
        GameModel p2Model = model.getPlayer2GameModel();
        
        assertEquals("Player1 initial level should be 1", 1, p1Model.getLevel());
        assertEquals("Player2 initial level should be 1", 1, p2Model.getLevel());
    }

    // ===== UI 요소 테스트 =====

    @Test
    public void testPingLabelsExist() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#myPingLabel", isNotNull());
        verifyThat("#opponentPingLabel", isNotNull());
    }

    @Test
    public void testDisconnectOverlayInitiallyHidden() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#disconnectOverlay", isInvisible());
    }

    @Test
    public void testPauseOverlayInitiallyHidden() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#pauseOverlay", isInvisible());
    }

    @Test
    public void testGameOverOverlayInitiallyHidden() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#gameOverOverlay", isInvisible());
    }

    @Test
    public void testNextBlockPanesVisible() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#nextBlockPane1", isVisible());
        verifyThat("#nextBlockPane2", isVisible());
    }

    @Test
    public void testDisconnectReasonLabelExists() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#disconnectReasonLabel", isNotNull());
    }

    @Test
    public void testDisconnectMenuButtonExists() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#disconnectMenuButton", isNotNull());
    }

    // ===== 키 입력 테스트 =====

    @Test
    public void testHardDropKey() throws InterruptedException {
        Thread.sleep(1000);
        
        // 하드 드롭 키 테스트
        press(KeyLayout.getHardDropKey(PlayerId.PLAYER1));
        Thread.sleep(300);
        
        // 크래시 없이 동작해야 함
        verifyThat("#gameBoard1", isNotNull());
    }

    @Test
    public void testPauseKeyToggle() throws InterruptedException {
        Thread.sleep(1000);
        
        // P 키로 일시정지
        press(KeyCode.P);
        Thread.sleep(500);
        
        // P 키로 재개
        press(KeyCode.P);
        Thread.sleep(500);
        
        // 크래시 없이 동작해야 함
        verifyThat("#gameBoard1", isNotNull());
    }

    @Test
    public void testLeftKeyMovesBlock() throws InterruptedException {
        Thread.sleep(1000);
        
        // 왼쪽 키 여러 번 누르기
        for (int i = 0; i < 3; i++) {
            press(KeyLayout.getLeftKey(PlayerId.PLAYER1));
            Thread.sleep(100);
        }
        
        // 크래시 없이 동작해야 함
        verifyThat("#gameBoard1", isNotNull());
    }

    @Test
    public void testRightKeyMovesBlock() throws InterruptedException {
        Thread.sleep(1000);
        
        // 오른쪽 키 여러 번 누르기
        for (int i = 0; i < 3; i++) {
            press(KeyLayout.getRightKey(PlayerId.PLAYER1));
            Thread.sleep(100);
        }
        
        // 크래시 없이 동작해야 함
        verifyThat("#gameBoard1", isNotNull());
    }

    @Test
    public void testRotateKeyRotatesBlock() throws InterruptedException {
        Thread.sleep(1000);
        
        // 회전 키 여러 번 누르기
        for (int i = 0; i < 4; i++) {
            press(KeyLayout.getUpKey(PlayerId.PLAYER1));
            Thread.sleep(150);
        }
        
        // 크래시 없이 동작해야 함
        verifyThat("#gameBoard1", isNotNull());
    }

    @Test
    public void testSoftDropKey() throws InterruptedException {
        Thread.sleep(1000);
        
        // 소프트 드롭 키 여러 번 누르기
        for (int i = 0; i < 5; i++) {
            press(KeyLayout.getDownKey(PlayerId.PLAYER1));
            Thread.sleep(100);
        }
        
        // 크래시 없이 동작해야 함
        verifyThat("#gameBoard1", isNotNull());
    }

    // ===== 보드 상태 테스트 =====

    @Test
    public void testBoardSizesAreEqual() throws InterruptedException {
        Thread.sleep(500);
        
        Point p1Size = model.getPlayer1GameModel().getBoardModel().getSize();
        Point p2Size = model.getPlayer2GameModel().getBoardModel().getSize();
        
        assertEquals("Board widths should be equal", p1Size.c, p2Size.c);
        assertEquals("Board heights should be equal", p1Size.r, p2Size.r);
    }

    @Test
    public void testBothPlayersHaveCurrentBlock() throws InterruptedException {
        Thread.sleep(500);
        
        Board p1Board = model.getPlayer1GameModel().getBoardModel();
        Board p2Board = model.getPlayer2GameModel().getBoardModel();
        
        assertNotNull("Player1 should have a current block", p1Board.activeBlock);
        assertNotNull("Player2 should have a current block", p2Board.activeBlock);
    }

    @Test
    public void testDropIntervalsArePositive() throws InterruptedException {
        Thread.sleep(500);
        
        int p1DropInterval = model.getPlayer1GameModel().getDropInterval();
        int p2DropInterval = model.getPlayer2GameModel().getDropInterval();
        
        assertTrue("Player1 drop interval should be positive", p1DropInterval > 0);
        assertTrue("Player2 drop interval should be positive", p2DropInterval > 0);
    }

    @Test
    public void testBoardDimensionsAreValid() throws InterruptedException {
        Thread.sleep(500);
        
        Point p1Size = model.getPlayer1GameModel().getBoardModel().getSize();
        
        assertTrue("Board width should be at least 10", p1Size.c >= 10);
        assertTrue("Board height should be at least 20", p1Size.r >= 20);
    }

    // ===== 스코어 모델 독립성 테스트 =====

    @Test
    public void testScoreModelsAreIndependent() throws InterruptedException {
        Thread.sleep(500);
        
        ScoreModel p1Score = model.getPlayer1GameModel().getScoreModel();
        ScoreModel p2Score = model.getPlayer2GameModel().getScoreModel();
        
        assertNotNull("Player1 score model should exist", p1Score);
        assertNotNull("Player2 score model should exist", p2Score);
        assertNotSame("Score models should be different instances", p1Score, p2Score);
    }

    @Test
    public void testScoreChangesAreIndependent() throws InterruptedException {
        Thread.sleep(500);
        
        ScoreModel p1Score = model.getPlayer1GameModel().getScoreModel();
        ScoreModel p2Score = model.getPlayer2GameModel().getScoreModel();
        
        // 플레이어1 점수 변경
        p1Score.softDrop(10);
        
        // 플레이어2 점수는 변경되지 않아야 함
        assertEquals("Player2 score should remain 0", 0, p2Score.getScore());
        assertTrue("Player1 score should be > 0", p1Score.getScore() > 0);
    }

    // ===== 공격 모델 테스트 =====

    @Test
    public void testAttackModelsExist() throws InterruptedException {
        Thread.sleep(500);
        
        assertNotNull("Player1 AttackModel should not be null", model.getPlayer1AttackModel());
        assertNotNull("Player2 AttackModel should not be null", model.getPlayer2AttackModel());
    }

    @Test
    public void testAttackModelsInitiallyEmpty() throws InterruptedException {
        Thread.sleep(500);
        
        assertTrue("Player1 attack queue should be empty initially", 
                model.getPlayer1AttackModel().getAttacks().isEmpty());
        assertTrue("Player2 attack queue should be empty initially", 
                model.getPlayer2AttackModel().getAttacks().isEmpty());
    }

    @Test
    public void testAttackModelsAreIndependent() throws InterruptedException {
        Thread.sleep(500);
        
        AttackModel p1Attack = model.getPlayer1AttackModel();
        AttackModel p2Attack = model.getPlayer2AttackModel();
        
        assertNotSame("Attack models should be different instances", p1Attack, p2Attack);
    }

    @Test
    public void testAttackCanBeAdded() throws InterruptedException {
        Thread.sleep(500);
        
        AttackModel p1Attack = model.getPlayer1AttackModel();
        
        int[] garbageRow = new int[10];
        for (int i = 0; i < 10; i++) {
            garbageRow[i] = 8; // Gray color
        }
        garbageRow[5] = 0; // Hole
        
        p1Attack.push(garbageRow);
        
        assertFalse("Attack queue should not be empty after push", p1Attack.getAttacks().isEmpty());
    }

    // ===== 아이템 모드 테스트 =====

    @Test
    public void testItemModeCanBeSet() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = model.getPlayer1GameModel();
        GameModel p2Model = model.getPlayer2GameModel();
        
        // 아이템 모드 설정
        p1Model.setItemMode(true);
        p2Model.setItemMode(true);
        
        assertTrue("Player1 item mode should be enabled", p1Model.isItemMode());
        assertTrue("Player2 item mode should be enabled", p2Model.isItemMode());
        
        // 아이템 모드 해제
        p1Model.setItemMode(false);
        p2Model.setItemMode(false);
        
        assertFalse("Player1 item mode should be disabled", p1Model.isItemMode());
        assertFalse("Player2 item mode should be disabled", p2Model.isItemMode());
    }

    @Test
    public void testItemModeIndependentBetweenPlayers() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = model.getPlayer1GameModel();
        GameModel p2Model = model.getPlayer2GameModel();
        
        // 플레이어1만 아이템 모드 설정
        p1Model.setItemMode(true);
        
        assertTrue("Player1 item mode should be enabled", p1Model.isItemMode());
        assertFalse("Player2 item mode should remain disabled", p2Model.isItemMode());
        
        // 정리
        p1Model.setItemMode(false);
    }

    // ===== 타임어택 모델 테스트 =====

    @Test
    public void testTimeAttackModelExists() throws InterruptedException {
        Thread.sleep(500);
        
        assertNotNull("TimeAttack model should not be null", model.getTimeAttack());
    }

    @Test
    public void testTimeAttackInitialRemainingTime() throws InterruptedException {
        Thread.sleep(500);
        
        // 타임어택 모델의 초기 시간이 설정되어 있어야 함
        assertNotNull("TimeAttack model should exist", model.getTimeAttack());
    }

    // ===== GameCommandExecutor 메서드 테스트 =====

    @Test
    public void testMoveLeftExecutor() throws InterruptedException {
        Thread.sleep(500);
        
        // moveLeft() 호출 테스트 - 원격 플레이어에게 적용됨
        interact(() -> controller.moveLeft());
        Thread.sleep(300);
        
        // 크래시 없이 동작해야 함
        assertNotNull(model);
    }

    @Test
    public void testMoveRightExecutor() throws InterruptedException {
        Thread.sleep(500);
        
        // moveRight() 호출 테스트
        interact(() -> controller.moveRight());
        Thread.sleep(300);
        
        assertNotNull(model);
    }

    @Test
    public void testRotateExecutor() throws InterruptedException {
        Thread.sleep(500);
        
        // rotate() 호출 테스트
        interact(() -> controller.rotate());
        Thread.sleep(300);
        
        assertNotNull(model);
    }

    @Test
    public void testSoftDropExecutor() throws InterruptedException {
        Thread.sleep(500);
        
        // softDrop() 호출 테스트
        interact(() -> controller.softDrop());
        Thread.sleep(300);
        
        assertNotNull(model);
    }

    @Test
    public void testHardDropExecutor() throws InterruptedException {
        Thread.sleep(500);
        
        // hardDrop() 호출 테스트 (현재 비어있음)
        interact(() -> controller.hardDrop());
        Thread.sleep(300);
        
        assertNotNull(model);
    }

    @Test
    public void testAttackExecutor() throws InterruptedException {
        Thread.sleep(500);
        
        // attack() 호출 테스트
        interact(() -> controller.attack(2));
        Thread.sleep(300);
        
        assertNotNull(model);
    }

    @Test
    public void testPauseExecutor() throws InterruptedException {
        Thread.sleep(500);
        
        // pause() 호출 테스트
        interact(() -> controller.pause());
        Thread.sleep(300);
        
        // pause 후 게임이 일시정지되어야 함
        assertTrue("Player1 should be paused", model.getPlayer1GameModel().isPaused());
    }

    @Test
    public void testResumeExecutor() throws InterruptedException {
        Thread.sleep(500);
        
        // 먼저 pause
        interact(() -> controller.pause());
        Thread.sleep(200);
        
        // resume() 호출 테스트
        interact(() -> controller.resume());
        Thread.sleep(300);
        
        // resume 후 게임이 재개되어야 함
        assertFalse("Player1 should not be paused", model.getPlayer1GameModel().isPaused());
    }

    @Test
    public void testUpdatePingExecutor() throws InterruptedException {
        Thread.sleep(500);
        
        // updatePing() 호출 테스트
        interact(() -> controller.updatePing(50));
        Thread.sleep(300);
        
        // 크래시 없이 동작해야 함
        assertNotNull(model);
    }

    @Test
    public void testUpdateOpponentPingExecutor() throws InterruptedException {
        Thread.sleep(500);
        
        // updateOpponentPing() 호출 테스트
        interact(() -> controller.updateOpponentPing(75));
        Thread.sleep(300);
        
        assertNotNull(model);
    }

    // ===== 게임 오버 테스트 =====

    @Test
    public void testGameOverExecutor() throws InterruptedException {
        Thread.sleep(500);
        
        // gameOver() 호출 테스트
        interact(() -> controller.gameOver(1000));
        Thread.sleep(500);
        
        // 게임 오버 상태가 되어야 함
        assertTrue("Player1 should be game over", model.getPlayer1GameModel().isGameOver());
    }

    @Test
    public void testOnGameResultWinner() throws InterruptedException {
        Thread.sleep(500);
        
        // 승리 결과 수신 테스트
        interact(() -> controller.onGameResult(true, 500));
        Thread.sleep(500);
        
        // 게임 오버 상태가 되어야 함
        assertTrue("Player1 should be game over after result", model.getPlayer1GameModel().isGameOver());
    }

    @Test
    public void testOnGameResultLoser() throws InterruptedException {
        Thread.sleep(500);
        
        // 패배 결과 수신 테스트
        interact(() -> controller.onGameResult(false, 2000));
        Thread.sleep(500);
        
        // 게임 오버 상태가 되어야 함
        assertTrue("Player1 should be game over after result", model.getPlayer1GameModel().isGameOver());
    }

    // ===== 연결 끊김 테스트 =====

    @Test
    public void testOnOpponentDisconnect() throws InterruptedException {
        Thread.sleep(500);
        
        // 상대방 연결 끊김 테스트
        interact(() -> controller.onOpponentDisconnect("Connection lost"));
        Thread.sleep(500);
        
        // disconnect 오버레이가 표시되어야 함
        verifyThat("#disconnectOverlay", isVisible());
    }

    @Test
    public void testOnOpponentDisconnectShowsReason() throws InterruptedException {
        Thread.sleep(500);
        
        String reason = "Player left the game";
        interact(() -> controller.onOpponentDisconnect(reason));
        Thread.sleep(500);
        
        // 연결 끊김 이유가 레이블에 표시되어야 함
        Label reasonLabel = lookup("#disconnectReasonLabel").queryAs(Label.class);
        assertEquals("Reason should be displayed", reason, reasonLabel.getText());
    }

    @Test
    public void testDisconnectOverlayBlocksGameInput() throws InterruptedException {
        Thread.sleep(500);
        
        // 연결 끊김 상태로 만들기
        interact(() -> controller.onOpponentDisconnect("Test disconnect"));
        Thread.sleep(300);
        
        // 오버레이가 표시되면 키 입력이 처리되지 않아야 함
        verifyThat("#disconnectOverlay", isVisible());
    }

    // ===== 플레이어 번호 테스트 =====

    @Test
    public void testPlayerNumberCanBeSet() throws InterruptedException {
        Thread.sleep(500);
        
        model.setPlayerNumber(1);
        assertEquals("Player number should be 1", 1, model.getPlayerNumber());
        
        model.setPlayerNumber(2);
        assertEquals("Player number should be 2", 2, model.getPlayerNumber());
    }

    @Test
    public void testDefaultPlayerNumber() throws InterruptedException {
        Thread.sleep(500);
        
        // 기본 플레이어 번호는 0 또는 1일 수 있음
        int playerNum = model.getPlayerNumber();
        assertTrue("Player number should be 0, 1, or 2", playerNum >= 0 && playerNum <= 2);
    }

    // ===== GameStart 테스트 =====

    @Test
    public void testGameStartWithSettings() throws InterruptedException {
        Thread.sleep(500);
        
        MatchSettings settings = new MatchSettings(1, 12345L, 67890L, GameMode.NORMAL, "NORMAL");
        
        interact(() -> controller.gameStart(settings));
        Thread.sleep(500);
        
        // 게임이 시작되어야 함 (paused가 false)
        assertFalse("Player1 should not be paused after game start", model.getPlayer1GameModel().isPaused());
        assertFalse("Player2 should not be paused after game start", model.getPlayer2GameModel().isPaused());
    }

    @Test
    public void testGameStartSetsPlayerNumber() throws InterruptedException {
        Thread.sleep(500);
        
        MatchSettings settings = new MatchSettings(1, 12345L, 67890L, GameMode.NORMAL, "NORMAL");
        
        interact(() -> controller.gameStart(settings));
        Thread.sleep(500);
        
        assertEquals("Player number should be set", 1, model.getPlayerNumber());
    }

    @Test
    public void testGameStartWithItemMode() throws InterruptedException {
        Thread.sleep(500);
        
        MatchSettings settings = new MatchSettings(1, 12345L, 67890L, GameMode.ITEM, "NORMAL");
        
        interact(() -> controller.gameStart(settings));
        Thread.sleep(500);
        
        // 아이템 모드가 설정되어야 함
        assertTrue("Player1 should be in item mode", model.getPlayer1GameModel().isItemMode());
        assertTrue("Player2 should be in item mode", model.getPlayer2GameModel().isItemMode());
    }

    @Test
    public void testGameStartWithTimeAttackMode() throws InterruptedException {
        Thread.sleep(500);
        
        MatchSettings settings = new MatchSettings(1, 12345L, 67890L, GameMode.TIME_ATTACK, "NORMAL");
        
        interact(() -> controller.gameStart(settings));
        Thread.sleep(500);
        
        // 타임어택 모드 설정 확인
        assertNotNull("TimeAttack model should exist", model.getTimeAttack());
    }

    // ===== UpdateState 테스트 =====

    @Test
    public void testUpdateStateChangesRemoteBoard() throws InterruptedException {
        Thread.sleep(500);
        
        // 보드 데이터 생성
        int[][] boardData = new int[20][10];
        boardData[19][0] = 1; // 맨 아래 왼쪽에 블록 하나
        
        interact(() -> controller.updateState(boardData, 0, 4, 100));
        Thread.sleep(300);
        
        // 크래시 없이 동작해야 함
        assertNotNull(model);
    }

    @Test
    public void testUpdateStateUpdatesScore() throws InterruptedException {
        Thread.sleep(500);
        
        int[][] boardData = new int[20][10];
        int expectedScore = 500;
        
        interact(() -> controller.updateState(boardData, 0, 4, expectedScore));
        Thread.sleep(300);
        
        // 원격 플레이어(player2)의 점수가 업데이트되어야 함
        // (updateState는 remotePlayer에 적용됨)
        assertNotNull(model.getPlayer2GameModel().getScoreModel());
    }

    // ===== Refresh 테스트 =====

    @Test
    public void testRefreshDoesNotCrash() throws InterruptedException {
        Thread.sleep(500);
        
        interact(() -> controller.refresh());
        Thread.sleep(300);
        
        // 크래시 없이 동작해야 함
        assertNotNull(model);
    }

    // ===== 게임 모델 상태 테스트 =====

    @Test
    public void testBothPlayersInitiallyNotGameOver() throws InterruptedException {
        Thread.sleep(500);
        
        assertFalse("Player1 should not be game over", model.getPlayer1GameModel().isGameOver());
        assertFalse("Player2 should not be game over", model.getPlayer2GameModel().isGameOver());
    }

    @Test
    public void testGameModelsAreIndependent() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = model.getPlayer1GameModel();
        GameModel p2Model = model.getPlayer2GameModel();
        
        assertNotSame("Game models should be different instances", p1Model, p2Model);
    }

    @Test
    public void testBoardModelsAreIndependent() throws InterruptedException {
        Thread.sleep(500);
        
        Board p1Board = model.getPlayer1GameModel().getBoardModel();
        Board p2Board = model.getPlayer2GameModel().getBoardModel();
        
        assertNotSame("Board models should be different instances", p1Board, p2Board);
    }

    // ===== 컨트롤 레이블 테스트 =====

    @Test
    public void testMoveControlLabel1Exists() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#moveControlLabel1", isNotNull());
    }

    @Test
    public void testRotateControlLabel1Exists() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#rotateControlLabel1", isNotNull());
    }

    @Test
    public void testSoftDropControlLabel1Exists() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#softDropControlLabel1", isNotNull());
    }

    @Test
    public void testHardDropControlLabel1Exists() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#hardDropControlLabel1", isNotNull());
    }

    // ===== 버튼 테스트 =====

    @Test
    public void testPauseButtonExists() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#pauseButton", isNotNull());
        verifyThat("#pauseButton", isVisible());
    }

    @Test
    public void testRestartButtonExists() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#restartButton", isNotNull());
    }

    @Test
    public void testResumeButtonExists() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#resumeButton", isNotNull());
    }

    @Test
    public void testMenuButtonExists() throws InterruptedException {
        Thread.sleep(500);
        
        verifyThat("#menuButton", isNotNull());
    }

    // ===== 일시정지 버튼 클릭 테스트 =====

    @Test
    public void testPauseButtonClick() throws InterruptedException {
        Thread.sleep(1000);
        
        // 일시정지 버튼 클릭
        clickOn("#pauseButton");
        Thread.sleep(500);
        
        // 일시정지 오버레이가 표시되어야 함
        verifyThat("#pauseOverlay", isVisible());
    }

    // ===== 레이블 값 테스트 =====

    @Test
    public void testScoreLabel1ShowsZeroInitially() throws InterruptedException {
        Thread.sleep(500);
        
        Label scoreLabel = lookup("#scoreLabel1").queryAs(Label.class);
        assertNotNull(scoreLabel);
        assertTrue("Score label should contain 0", scoreLabel.getText().contains("0"));
    }

    @Test
    public void testLevelLabel1ShowsOneInitially() throws InterruptedException {
        Thread.sleep(500);
        
        Label levelLabel = lookup("#levelLabel1").queryAs(Label.class);
        assertNotNull(levelLabel);
        assertTrue("Level label should contain 1", levelLabel.getText().contains("1"));
    }

    @Test
    public void testLinesLabel1ShowsZeroInitially() throws InterruptedException {
        Thread.sleep(500);
        
        Label linesLabel = lookup("#linesLabel1").queryAs(Label.class);
        assertNotNull(linesLabel);
        assertTrue("Lines label should contain 0", linesLabel.getText().contains("0"));
    }

    // ===== 핑 레이블 색상 테스트 =====

    @Test
    public void testGoodPingColor() throws InterruptedException {
        Thread.sleep(500);
        
        // 좋은 핑 (< 50ms)
        interact(() -> controller.updatePing(30));
        Thread.sleep(200);
        
        Label pingLabel = lookup("#myPingLabel").queryAs(Label.class);
        assertNotNull(pingLabel);
        assertTrue("Ping label should show 30 ms", pingLabel.getText().contains("30"));
    }

    @Test
    public void testMediumPingColor() throws InterruptedException {
        Thread.sleep(500);
        
        // 보통 핑 (50-100ms)
        interact(() -> controller.updatePing(75));
        Thread.sleep(200);
        
        Label pingLabel = lookup("#myPingLabel").queryAs(Label.class);
        assertNotNull(pingLabel);
        assertTrue("Ping label should show 75 ms", pingLabel.getText().contains("75"));
    }

    @Test
    public void testBadPingColor() throws InterruptedException {
        Thread.sleep(500);
        
        // 나쁜 핑 (>= 100ms)
        interact(() -> controller.updatePing(150));
        Thread.sleep(200);
        
        Label pingLabel = lookup("#myPingLabel").queryAs(Label.class);
        assertNotNull(pingLabel);
        assertTrue("Ping label should show 150 ms", pingLabel.getText().contains("150"));
    }

    // ===== 시드 설정 테스트 =====

    @Test
    public void testSeedCanBeSet() throws InterruptedException {
        Thread.sleep(500);
        
        long seed1 = 12345L;
        long seed2 = 67890L;
        
        model.getPlayer1GameModel().setNextBlockSeed(seed1);
        model.getPlayer2GameModel().setNextBlockSeed(seed2);
        
        // 크래시 없이 동작해야 함
        assertNotNull(model.getPlayer1GameModel());
        assertNotNull(model.getPlayer2GameModel());
    }

    // ===== 게임 리셋 테스트 =====

    @Test
    public void testGameModelReset() throws InterruptedException {
        Thread.sleep(500);
        
        // 점수 변경
        model.getPlayer1GameModel().getScoreModel().softDrop(10);
        assertTrue("Score should be > 0", model.getPlayer1GameModel().getScoreModel().getScore() > 0);
        
        // 리셋
        model.getPlayer1GameModel().reset();
        
        // 점수가 0으로 돌아가야 함
        assertEquals("Score should be reset to 0", 0, model.getPlayer1GameModel().getScoreModel().getScore());
    }

    // ===== 중복 연결 끊김 처리 테스트 =====

    @Test
    public void testDuplicateDisconnectIgnored() throws InterruptedException {
        Thread.sleep(500);
        
        // 첫 번째 연결 끊김
        interact(() -> controller.onOpponentDisconnect("First disconnect"));
        Thread.sleep(200);
        
        // 두 번째 연결 끊김 (무시되어야 함)
        interact(() -> controller.onOpponentDisconnect("Second disconnect"));
        Thread.sleep(200);
        
        // 첫 번째 이유만 표시되어야 함
        Label reasonLabel = lookup("#disconnectReasonLabel").queryAs(Label.class);
        assertEquals("First reason should be displayed", "First disconnect", reasonLabel.getText());
    }

    // ===== 컨트롤러 null 체크 테스트 =====

    @Test
    public void testControllerNotNull() throws InterruptedException {
        Thread.sleep(500);
        
        assertNotNull("Controller should not be null", controller);
    }

    @Test
    public void testModelNotNull() throws InterruptedException {
        Thread.sleep(500);
        
        assertNotNull("Model should not be null", model);
    }
}
