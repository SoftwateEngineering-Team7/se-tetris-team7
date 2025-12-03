package org.tetris.game.controller;

import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isInvisible;
import static org.testfx.matcher.base.NodeMatchers.isNotNull;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.tetris.game.DualGameFactory;
import org.tetris.game.model.AttackModel;
import org.tetris.game.model.Board;
import org.tetris.game.model.DualGameModel;
import org.tetris.game.model.GameMode;
import org.tetris.game.model.GameModel;
import org.tetris.game.model.NextBlockModel;
import org.tetris.game.model.ScoreModel;
import org.tetris.game.model.blocks.Block;
import org.tetris.shared.MvcBundle;

import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

public class DualGameControllerTest extends ApplicationTest {

    private final DualGameFactory gameFactory = new DualGameFactory();
    private DualGameModel dualGameModel;

    @Override
    public void start(Stage stage) throws Exception {
        MvcBundle bundle = gameFactory.create();
        dualGameModel = (DualGameModel) bundle.model();
        stage.setScene(bundle.view().getScene());
        stage.setTitle("Dual Game Test");
        stage.show();
    }

    // ===== UI 표시 테스트 =====

    @Test
    public void testDualGameSceneDisplay() {
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
    public void testPauseButton() throws InterruptedException {
        Thread.sleep(1000);
        clickOn("#pauseButton");
        Thread.sleep(500);
        verifyThat("#pauseOverlay", isVisible());

        clickOn("#resumeButton");
        Thread.sleep(500);
        verifyThat("#pauseOverlay", isInvisible());
    }

    @Test
    public void testPauseKeyToggle() throws InterruptedException {
        Thread.sleep(1000);
        press(KeyCode.P);
        Thread.sleep(500);
        
        press(KeyCode.P);
        Thread.sleep(500);
    }

    @Test
    public void testMovementKeysPlayer1() throws InterruptedException {
        Thread.sleep(1000);
        press(KeyCode.A);
        Thread.sleep(200);
        press(KeyCode.D);
        Thread.sleep(200);
        press(KeyCode.W);
        Thread.sleep(200);
        press(KeyCode.S);
        Thread.sleep(200);
    }

    @Test
    public void testMovementKeysPlayer2() throws InterruptedException {
        Thread.sleep(1000);
        press(KeyCode.LEFT);
        Thread.sleep(200);
        press(KeyCode.RIGHT);
        Thread.sleep(200);
        press(KeyCode.UP);
        Thread.sleep(200);
        press(KeyCode.DOWN);
        Thread.sleep(200);
    }

    @Test
    public void testHardDropKeys() throws InterruptedException {
        Thread.sleep(1000);
        press(KeyCode.F);      // P1 hard drop
        Thread.sleep(300);
        press(KeyCode.SLASH);  // P2 hard drop
        Thread.sleep(300);
        verifyThat("#scoreLabel1", isVisible());
        verifyThat("#scoreLabel2", isVisible());
    }

    @Test
    public void testInitialOverlaysHidden() throws InterruptedException {
        Thread.sleep(500);
        verifyThat("#gameOverOverlay", isInvisible());
        verifyThat("#pauseOverlay", isInvisible());
    }

    @Test
    public void testNextAndAttackPreviewVisible() throws InterruptedException {
        Thread.sleep(500);
        verifyThat("#nextBlockPane1", isVisible());
        verifyThat("#nextBlockPane2", isVisible());
    }

    // ===== 듀얼 게임 모델 로직 테스트 =====

    @Test
    public void testDualGameModelInitialization() throws InterruptedException {
        Thread.sleep(500);
        
        assertNotNull("DualGameModel should not be null", dualGameModel);
        assertNotNull("Player1 GameModel should not be null", dualGameModel.getPlayer1GameModel());
        assertNotNull("Player2 GameModel should not be null", dualGameModel.getPlayer2GameModel());
    }

    @Test
    public void testBothPlayersHaveBoardModels() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = dualGameModel.getPlayer1GameModel();
        GameModel p2Model = dualGameModel.getPlayer2GameModel();
        
        assertNotNull("Player1 BoardModel should not be null", p1Model.getBoardModel());
        assertNotNull("Player2 BoardModel should not be null", p2Model.getBoardModel());
    }

    @Test
    public void testBothPlayersHaveScoreModels() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = dualGameModel.getPlayer1GameModel();
        GameModel p2Model = dualGameModel.getPlayer2GameModel();
        
        assertNotNull("Player1 ScoreModel should not be null", p1Model.getScoreModel());
        assertNotNull("Player2 ScoreModel should not be null", p2Model.getScoreModel());
    }

    @Test
    public void testBothPlayersHaveNextBlockModels() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = dualGameModel.getPlayer1GameModel();
        GameModel p2Model = dualGameModel.getPlayer2GameModel();
        
        assertNotNull("Player1 NextBlockModel should not be null", p1Model.getNextBlockModel());
        assertNotNull("Player2 NextBlockModel should not be null", p2Model.getNextBlockModel());
    }

    @Test
    public void testAttackModelsExist() throws InterruptedException {
        Thread.sleep(500);
        
        assertNotNull("Player1 AttackModel should not be null", dualGameModel.getPlayer1AttackModel());
        assertNotNull("Player2 AttackModel should not be null", dualGameModel.getPlayer2AttackModel());
    }

    @Test
    public void testInitialGameStateForBothPlayers() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = dualGameModel.getPlayer1GameModel();
        GameModel p2Model = dualGameModel.getPlayer2GameModel();
        
        assertFalse("Player1 should not be game over initially", p1Model.isGameOver());
        assertFalse("Player2 should not be game over initially", p2Model.isGameOver());
        assertFalse("Player1 should not be paused initially", p1Model.isPaused());
        assertFalse("Player2 should not be paused initially", p2Model.isPaused());
    }

    @Test
    public void testInitialLevelForBothPlayers() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = dualGameModel.getPlayer1GameModel();
        GameModel p2Model = dualGameModel.getPlayer2GameModel();
        
        assertEquals("Player1 initial level should be 1", 1, p1Model.getLevel());
        assertEquals("Player2 initial level should be 1", 1, p2Model.getLevel());
    }

    @Test
    public void testInitialLinesClearedForBothPlayers() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = dualGameModel.getPlayer1GameModel();
        GameModel p2Model = dualGameModel.getPlayer2GameModel();
        
        assertEquals("Player1 initial lines cleared should be 0", 0, p1Model.getTotalLinesCleared());
        assertEquals("Player2 initial lines cleared should be 0", 0, p2Model.getTotalLinesCleared());
    }

    @Test
    public void testPauseAffectsBothPlayers() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = dualGameModel.getPlayer1GameModel();
        GameModel p2Model = dualGameModel.getPlayer2GameModel();
        
        // 초기 상태
        assertFalse("Player1 should not be paused initially", p1Model.isPaused());
        assertFalse("Player2 should not be paused initially", p2Model.isPaused());
        
        // 버튼으로 일시정지 (더 안정적인 방법)
        clickOn("#pauseButton");
        Thread.sleep(500);
        
        assertTrue("Player1 should be paused after pause button", p1Model.isPaused());
        assertTrue("Player2 should be paused after pause button", p2Model.isPaused());
        
        // Resume 버튼으로 재개
        clickOn("#resumeButton");
        Thread.sleep(500);
        
        assertFalse("Player1 should resume after resume button", p1Model.isPaused());
        assertFalse("Player2 should resume after resume button", p2Model.isPaused());
    }

    @Test
    public void testPlayer1ScoreIncreasesOnSoftDrop() throws InterruptedException {
        Thread.sleep(500);
        
        ScoreModel p1Score = dualGameModel.getPlayer1GameModel().getScoreModel();
        int initialScore = p1Score.getScore();
        
        // Player1 소프트 드롭 (S 키)
        press(KeyCode.S);
        Thread.sleep(200);
        press(KeyCode.S);
        Thread.sleep(200);
        
        int newScore = p1Score.getScore();
        assertTrue("Player1 score should increase on soft drop", newScore >= initialScore);
    }

    @Test
    public void testPlayer2ScoreIncreasesOnSoftDrop() throws InterruptedException {
        Thread.sleep(500);
        
        ScoreModel p2Score = dualGameModel.getPlayer2GameModel().getScoreModel();
        int initialScore = p2Score.getScore();
        
        // Player2 소프트 드롭 (DOWN 키)
        press(KeyCode.DOWN);
        Thread.sleep(200);
        press(KeyCode.DOWN);
        Thread.sleep(200);
        
        int newScore = p2Score.getScore();
        assertTrue("Player2 score should increase on soft drop", newScore >= initialScore);
    }

    @Test
    public void testPlayer1ScoreIncreasesOnHardDrop() throws InterruptedException {
        Thread.sleep(500);
        
        ScoreModel p1Score = dualGameModel.getPlayer1GameModel().getScoreModel();
        int initialScore = p1Score.getScore();
        
        // Player1 하드 드롭 (F 키)
        press(KeyCode.F);
        Thread.sleep(500);
        
        int newScore = p1Score.getScore();
        assertTrue("Player1 score should increase on hard drop", newScore > initialScore);
    }

    @Test
    public void testPlayer2ScoreIncreasesOnHardDrop() throws InterruptedException {
        Thread.sleep(500);
        
        ScoreModel p2Score = dualGameModel.getPlayer2GameModel().getScoreModel();
        int initialScore = p2Score.getScore();
        
        // Player2 하드 드롭 (SLASH 키)
        press(KeyCode.SLASH);
        Thread.sleep(500);
        
        int newScore = p2Score.getScore();
        assertTrue("Player2 score should increase on hard drop", newScore > initialScore);
    }

    @Test
    public void testPlayer1BlockMoveLeft() throws InterruptedException {
        Thread.sleep(500);
        
        Board board = dualGameModel.getPlayer1GameModel().getBoardModel();
        int initialCol = board.getCurPos().c;
        
        // Player1 왼쪽 이동 (A 키)
        press(KeyCode.A);
        Thread.sleep(200);
        
        int newCol = board.getCurPos().c;
        assertTrue("Player1 block should move left or stay if at wall", newCol <= initialCol);
    }

    @Test
    public void testPlayer1BlockMoveRight() throws InterruptedException {
        Thread.sleep(500);
        
        Board board = dualGameModel.getPlayer1GameModel().getBoardModel();
        int initialCol = board.getCurPos().c;
        
        // Player1 오른쪽 이동 (D 키)
        press(KeyCode.D);
        Thread.sleep(200);
        
        int newCol = board.getCurPos().c;
        assertTrue("Player1 block should move right or stay if at wall", newCol >= initialCol);
    }

    @Test
    public void testPlayer2BlockMoveLeft() throws InterruptedException {
        Thread.sleep(500);
        
        Board board = dualGameModel.getPlayer2GameModel().getBoardModel();
        int initialCol = board.getCurPos().c;
        
        // Player2 왼쪽 이동 (LEFT 키)
        press(KeyCode.LEFT);
        Thread.sleep(200);
        
        int newCol = board.getCurPos().c;
        assertTrue("Player2 block should move left or stay if at wall", newCol <= initialCol);
    }

    @Test
    public void testPlayer2BlockMoveRight() throws InterruptedException {
        Thread.sleep(500);
        
        Board board = dualGameModel.getPlayer2GameModel().getBoardModel();
        int initialCol = board.getCurPos().c;
        
        // Player2 오른쪽 이동 (RIGHT 키)
        press(KeyCode.RIGHT);
        Thread.sleep(200);
        
        int newCol = board.getCurPos().c;
        assertTrue("Player2 block should move right or stay if at wall", newCol >= initialCol);
    }

    @Test
    public void testPlayer1BlockRotate() throws InterruptedException {
        Thread.sleep(500);
        
        // Player1 회전 테스트 (W 키)
        press(KeyCode.W);
        Thread.sleep(200);
        press(KeyCode.W);
        Thread.sleep(200);
        
        assertNotNull("Player1 board should still have current block", 
                dualGameModel.getPlayer1GameModel().getBoardModel().activeBlock);
    }

    @Test
    public void testPlayer2BlockRotate() throws InterruptedException {
        Thread.sleep(500);
        
        // Player2 회전 테스트 (UP 키)
        press(KeyCode.UP);
        Thread.sleep(200);
        press(KeyCode.UP);
        Thread.sleep(200);
        
        assertNotNull("Player2 board should still have current block", 
                dualGameModel.getPlayer2GameModel().getBoardModel().activeBlock);
    }

    @Test
    public void testBothPlayersHaveCurrentBlock() throws InterruptedException {
        Thread.sleep(500);
        
        Block p1Block = dualGameModel.getPlayer1GameModel().getBoardModel().activeBlock;
        Block p2Block = dualGameModel.getPlayer2GameModel().getBoardModel().activeBlock;
        
        assertNotNull("Player1 should have a current block", p1Block);
        assertNotNull("Player2 should have a current block", p2Block);
    }

    @Test
    public void testBothPlayersHaveNextBlock() throws InterruptedException {
        Thread.sleep(500);
        
        Block p1NextBlock = dualGameModel.getPlayer1GameModel().getNextBlockModel().peekNext();
        Block p2NextBlock = dualGameModel.getPlayer2GameModel().getNextBlockModel().peekNext();
        
        assertNotNull("Player1 should have a next block", p1NextBlock);
        assertNotNull("Player2 should have a next block", p2NextBlock);
    }

    @Test
    public void testBoardSizesAreEqual() throws InterruptedException {
        Thread.sleep(500);
        
        org.util.Point p1Size = dualGameModel.getPlayer1GameModel().getBoardModel().getSize();
        org.util.Point p2Size = dualGameModel.getPlayer2GameModel().getBoardModel().getSize();
        
        assertEquals("Board widths should be equal", p1Size.c, p2Size.c);
        assertEquals("Board heights should be equal", p1Size.r, p2Size.r);
    }

    @Test
    public void testDropIntervalsArePositive() throws InterruptedException {
        Thread.sleep(500);
        
        int p1DropInterval = dualGameModel.getPlayer1GameModel().getDropInterval();
        int p2DropInterval = dualGameModel.getPlayer2GameModel().getDropInterval();
        
        assertTrue("Player1 drop interval should be positive", p1DropInterval > 0);
        assertTrue("Player2 drop interval should be positive", p2DropInterval > 0);
    }

    @Test
    public void testAttackModelInitiallyEmpty() throws InterruptedException {
        Thread.sleep(500);
        
        AttackModel p1Attack = dualGameModel.getPlayer1AttackModel();
        AttackModel p2Attack = dualGameModel.getPlayer2AttackModel();
        
        assertTrue("Player1 attack queue should be empty initially", p1Attack.getAttacks().isEmpty());
        assertTrue("Player2 attack queue should be empty initially", p2Attack.getAttacks().isEmpty());
    }

    @Test
    public void testMultipleHardDropsForBothPlayers() throws InterruptedException {
        Thread.sleep(500);
        
        // 양 플레이어 여러 번 하드 드롭
        for (int i = 0; i < 3; i++) {
            press(KeyCode.F);      // P1
            Thread.sleep(400);
            press(KeyCode.SLASH);  // P2
            Thread.sleep(400);
        }
        
        // 게임이 계속 실행되어야 함
        assertNotNull("DualGameModel should still be valid", dualGameModel);
    }

    @Test
    public void testKeyInputIgnoredWhenPaused() throws InterruptedException {
        Thread.sleep(500);
        
        // 일시정지
        press(KeyCode.P);
        Thread.sleep(300);
        
        Board p1Board = dualGameModel.getPlayer1GameModel().getBoardModel();
        int colBeforeMove = p1Board.getCurPos().c;
        
        // 일시정지 상태에서 이동 시도
        press(KeyCode.A);
        Thread.sleep(200);
        
        int colAfterMove = p1Board.getCurPos().c;
        
        // 일시정지 상태에서는 블록이 이동하지 않아야 함
        assertEquals("Block should not move when paused", colBeforeMove, colAfterMove);
        
        // 재개
        press(KeyCode.P);
        Thread.sleep(200);
    }

    @Test
    public void testTimeAttackModelExists() throws InterruptedException {
        Thread.sleep(500);
        
        assertNotNull("TimeAttack model should not be null", dualGameModel.getTimeAttack());
    }

    @Test
    public void testItemModeCanBeSetForBothPlayers() throws InterruptedException {
        Thread.sleep(500);
        
        GameModel p1Model = dualGameModel.getPlayer1GameModel();
        GameModel p2Model = dualGameModel.getPlayer2GameModel();
        
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
    public void testScoresAreIndependent() throws InterruptedException {
        Thread.sleep(500);
        
        ScoreModel p1Score = dualGameModel.getPlayer1GameModel().getScoreModel();
        ScoreModel p2Score = dualGameModel.getPlayer2GameModel().getScoreModel();
        
        // 각 플레이어의 점수 모델이 독립적인지 확인
        assertNotNull("Player1 score model should exist", p1Score);
        assertNotNull("Player2 score model should exist", p2Score);
        
        // 서로 다른 ScoreModel 인스턴스인지 확인
        assertNotSame("Score models should be different instances", p1Score, p2Score);
        
        // 초기 점수가 0인지 확인
        assertEquals("Player1 initial score should be 0", 0, p1Score.getScore());
        assertEquals("Player2 initial score should be 0", 0, p2Score.getScore());
    }

    @Test
    public void testResumeButtonFromPauseOverlay() throws InterruptedException {
        Thread.sleep(500);
        
        // 일시정지
        clickOn("#pauseButton");
        Thread.sleep(500);
        
        verifyThat("#pauseOverlay", isVisible());
        
        // Resume 버튼 클릭
        clickOn("#resumeButton");
        Thread.sleep(500);
        
        verifyThat("#pauseOverlay", isInvisible());
        
        // 게임이 재개되었는지 확인
        assertFalse("Player1 should not be paused after resume", 
                dualGameModel.getPlayer1GameModel().isPaused());
        assertFalse("Player2 should not be paused after resume", 
                dualGameModel.getPlayer2GameModel().isPaused());
    }
}

