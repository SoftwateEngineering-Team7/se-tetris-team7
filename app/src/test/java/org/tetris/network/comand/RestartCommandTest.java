package org.tetris.network.comand;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * RestartCommand 테스트
 */
public class RestartCommandTest {

    @Test
    public void testRestartCommandCreation() {
        // When
        RestartCommand command = new RestartCommand();

        // Then
        assertNotNull(command);
    }

    @Test
    public void testRestartCommandIsCommand() {
        // When
        RestartCommand command = new RestartCommand();

        // Then
        assertTrue(command instanceof Command);
    }
}
