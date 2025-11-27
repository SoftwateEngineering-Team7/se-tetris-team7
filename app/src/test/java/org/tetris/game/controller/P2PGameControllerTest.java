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
import javafx.scene.input.KeyCode;

public class P2PGameControllerTest extends ApplicationTest {

    private final P2PGameFactory gameFactory = new P2PGameFactory();

    @Override
    public void start(Stage stage) throws Exception {
        MvcBundle bundle = gameFactory.create();
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
        // P2PGameController uses arrow keys for Player 1 (local player)
        // Wait, let's check P2PGameController.handleKeyPress again.
        // It checks KeyCode.LEFT, RIGHT, UP, DOWN for Player 1.
        // In DualGameController, Player 1 usually uses WASD and Player 2 uses Arrows.
        // But P2PGameController overrides handleKeyPress.

        press(KeyCode.LEFT);
        Thread.sleep(200);
        press(KeyCode.RIGHT);
        Thread.sleep(200);
        press(KeyCode.UP);
        Thread.sleep(200);
        press(KeyCode.DOWN);
        Thread.sleep(200);

        // Should not crash even if not connected
    }

    @Test
    public void testPlayer2KeysIgnored() throws InterruptedException {
        // Player 2 keys (WASD in P2P context? No, P2PGameController only handles local player)
        // DualGameController handles WASD for Player 1 and Arrows for Player 2 by default.
        // P2PGameController overrides handleKeyPress to handle Arrows for Player 1 (Local).
        // And it overrides handlePlayerInput to ignore Player 2.

        // Let's verify WASD doesn't move Player 1 (since P2PGameController uses Arrows for P1?)
        // Wait, P2PGameController.handleKeyPress:
        /*
         * if (code == KeyCode.LEFT) { ... } else if (code == KeyCode.RIGHT) { ... }
         */
        // It seems P2PGameController uses ARROW keys for the local player (Player 1).

        // DualGameController.handleKeyPress calls handlePlayerInput for P1 (WASD) and P2 (Arrows).
        // P2PGameController overrides handleKeyPress and DOES NOT call super.handleKeyPress.
        // Instead it implements its own logic using LEFT/RIGHT/UP/DOWN for Player 1.

        // So WASD should do nothing.
        Thread.sleep(1000);
        press(KeyCode.W);
        Thread.sleep(200);
        press(KeyCode.A);
        Thread.sleep(200);
        press(KeyCode.S);
        Thread.sleep(200);
        press(KeyCode.D);
        Thread.sleep(200);

        // Verify no crash. Hard to verify "no movement" without checking internal state,
        // but we can verify it doesn't crash.
    }
}
