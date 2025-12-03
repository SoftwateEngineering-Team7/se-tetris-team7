package org.tetris.game.controller;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tetris.game.model.DualGameModel;
import org.tetris.game.model.GameMode;
import org.tetris.game.model.ReplayData;
import org.tetris.network.comand.InputCommand;
import org.tetris.network.dto.MatchSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ReplayIntegrationTest {

    private ReplayController controller;
    private DualGameModel model;
    private ReplayData replayData;

    @BeforeClass
    public static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @Before
    public void setUp() {
        model = new DualGameModel();
        
        // Mock ReplayData
        MatchSettings settings = new MatchSettings(1, 12345L, 67890L, GameMode.NORMAL, "Normal");
        List<InputCommand> p1Inputs = new ArrayList<>();
        p1Inputs.add(new InputCommand(1, 1, "moveLeft", 0L));
        List<InputCommand> p2Inputs = new ArrayList<>();
        p2Inputs.add(new InputCommand(2, 1, "moveRight", 0L));
        
        replayData = new ReplayData(settings, p1Inputs, p2Inputs, 10000L, 100, 100, true);
        
        // ReplayController needs to be created on JavaFX thread if it touches UI elements in constructor (it doesn't seem to)
        // But initialize() definitely needs JavaFX thread.
        controller = new ReplayController(model);
    }

    @Test
    public void testInitialization() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                controller.setReplayData(replayData);
                controller.initializeReplay();
                
                // Verify seeds are set
                assertEquals(12345L, model.getPlayer1GameModel().getNextBlockSeed());
                assertEquals(67890L, model.getPlayer2GameModel().getNextBlockSeed());
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception during initialization: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue("Timeout waiting for initialization", latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testInputBlocking() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                controller.setReplayData(replayData);
                controller.initializeReplay();
                
                // Test blocked key (e.g., UP for rotate)
                KeyEvent rotateEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.UP, false, false, false, false);
                controller.handleKeyPress(rotateEvent);
                assertTrue("Rotate event should be consumed", rotateEvent.isConsumed());
                
                // Test allowed key (P for pause)
                KeyEvent pauseEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.P, false, false, false, false);
                controller.handleKeyPress(pauseEvent);
                assertTrue("Pause event should be consumed", pauseEvent.isConsumed()); // Consumed but processed
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception during input blocking test: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue("Timeout waiting for input blocking test", latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testMenuButtonBinding() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Inject mock button
                javafx.scene.control.Button mockMenuButton = new javafx.scene.control.Button();
                controller.menuButton = mockMenuButton;
                
                // Inject root to avoid NPE in setupEventHandlers if accessed
                controller.root = new javafx.scene.layout.BorderPane();
                
                // Call initialize() which triggers setupReplayUI()
                controller.initialize();
                
                // setupReplayUI is run via Platform.runLater inside initialize, so we need to wait
                Platform.runLater(() -> {
                    try {
                        assertNotNull("Menu button action should be bound", mockMenuButton.getOnAction());
                    } catch (AssertionError e) {
                        throw e;
                    } finally {
                        latch.countDown();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown(); // Ensure latch is counted down on error
                fail("Exception during menu button binding test: " + e.getMessage());
            }
        });
        
        assertTrue("Timeout waiting for menu button binding test", latch.await(5, TimeUnit.SECONDS));
    }
}
