package org.tetris.game.controller;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isInvisible;
import static org.testfx.matcher.base.NodeMatchers.isNotNull;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.tetris.game.DualGameFactory;
import org.tetris.shared.MvcBundle;

import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

public class DualGameControllerTest extends ApplicationTest {

    private final DualGameFactory gameFactory = new DualGameFactory();

    @Override
    public void start(Stage stage) throws Exception {
        MvcBundle bundle = gameFactory.create();
        stage.setScene(bundle.view().getScene());
        stage.setTitle("Dual Game Test");
        stage.show();
    }

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
        verifyThat("#attackPreviewPane1", isVisible());
        verifyThat("#attackPreviewPane2", isVisible());
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
        verifyThat("#pauseOverlay", isVisible());

        press(KeyCode.P);
        Thread.sleep(500);
        verifyThat("#pauseOverlay", isInvisible());
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
        verifyThat("#attackPreviewPane1", isVisible());
        verifyThat("#attackPreviewPane2", isVisible());
    }
}
