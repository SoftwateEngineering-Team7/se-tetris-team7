package org.tetris.network.comand;

import org.junit.Test;
import org.tetris.network.mocks.TestGameMenuCommandExecutor;

import static org.junit.Assert.*;

/**
 * PlayerConnectionCommand 테스트
 */
public class PlayerConnectionCommandTest {

    @Test
    public void testPlayerConnectionCommandOpponentConnected() {
        TestGameMenuCommandExecutor executor = new TestGameMenuCommandExecutor();

        PlayerConnectionCommand command = new PlayerConnectionCommand(true);
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("onPlayerConnectionChanged"));
        assertTrue(executor.lastOpponentConnected);
    }

    @Test
    public void testPlayerConnectionCommandOpponentDisconnected() {
        TestGameMenuCommandExecutor executor = new TestGameMenuCommandExecutor();

        PlayerConnectionCommand command = new PlayerConnectionCommand(false);
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("onPlayerConnectionChanged"));
        assertFalse(executor.lastOpponentConnected);
    }

    @Test
    public void testPlayerConnectionCommandIsOpponentConnectedGetter() {
        PlayerConnectionCommand connectedCmd = new PlayerConnectionCommand(true);
        PlayerConnectionCommand disconnectedCmd = new PlayerConnectionCommand(false);

        assertTrue(connectedCmd.isOpponentConnected());
        assertFalse(disconnectedCmd.isOpponentConnected());
    }
}
