package org.tetris.game.controller;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isInvisible;
import static org.testfx.matcher.base.NodeMatchers.isNotNull;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.tetris.game.P2PGameFactory;
import org.tetris.shared.MvcBundle;

import javafx.stage.Stage;
import org.util.Point;
import org.util.KeyLayout;
import org.util.PlayerId;

public class P2PGameControllerTest extends ApplicationTest {

    private final P2PGameFactory gameFactory = new P2PGameFactory();
    private P2PGameController controller;

    @Override
    public void start(Stage stage) throws Exception {
        MvcBundle bundle = gameFactory.create();
        controller = (P2PGameController) bundle.controller();
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
}
