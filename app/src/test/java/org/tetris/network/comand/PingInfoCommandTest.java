package org.tetris.network.comand;

import org.junit.Before;
import org.junit.Test;
import org.tetris.network.mocks.TestGameCommandExecutor;

import static org.junit.Assert.*;

/**
 * PingInfoCommand 테스트
 */
public class PingInfoCommandTest {

    private TestGameCommandExecutor executor;

    @Before
    public void setUp() {
        executor = new TestGameCommandExecutor();
    }

    @Test
    public void testPingInfoCommandCreation() {
        // Given
        long expectedPing = 50L;

        // When
        PingInfoCommand command = new PingInfoCommand(expectedPing);

        // Then
        assertEquals(expectedPing, command.getPing());
    }

    @Test
    public void testPingInfoCommandExecute() {
        // Given
        long expectedPing = 100L;
        PingInfoCommand command = new PingInfoCommand(expectedPing);

        // When
        command.execute(executor);

        // Then
        assertTrue(executor.executedCommands.contains("updateOpponentPing"));
        assertEquals(expectedPing, executor.lastPing);
    }

    @Test
    public void testPingInfoCommandWithVariousPingValues() {
        // Given
        long[] pingValues = {0L, 1L, 50L, 100L, 500L, 1000L, Long.MAX_VALUE};

        for (long ping : pingValues) {
            // When
            PingInfoCommand command = new PingInfoCommand(ping);
            executor.executedCommands.clear();
            command.execute(executor);

            // Then
            assertEquals(ping, command.getPing());
            assertEquals(ping, executor.lastPing);
        }
    }
}
