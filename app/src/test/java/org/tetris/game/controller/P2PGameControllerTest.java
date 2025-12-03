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
import org.tetris.game.model.GameModel;
import org.tetris.game.model.Board;
import org.tetris.game.model.ScoreModel;
import org.tetris.shared.MvcBundle;

import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import org.util.Point;
import org.util.KeyLayout;
import org.util.PlayerId;

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

    // ===== 타임어택 모델 테스트 =====

    @Test
    public void testTimeAttackModelExists() throws InterruptedException {
        Thread.sleep(500);
        
        assertNotNull("TimeAttack model should not be null", model.getTimeAttack());
    }
}
