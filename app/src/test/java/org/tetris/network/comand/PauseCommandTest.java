package org.tetris.network.comand;

import org.junit.Before;
import org.junit.Test;
import org.tetris.network.mocks.TestGameCommandExecutor;

import static org.junit.Assert.*;

/**
 * PauseCommand 테스트
 */
public class PauseCommandTest {

    private TestGameCommandExecutor executor;

    @Before
    public void setUp() {
        executor = new TestGameCommandExecutor();
    }

    @Test
    public void testPauseCommandCreation() {
        // When
        PauseCommand pauseCmd = new PauseCommand(true);
        PauseCommand resumeCmd = new PauseCommand(false);

        // Then
        assertTrue(pauseCmd.isPaused());
        assertFalse(resumeCmd.isPaused());
    }

    @Test
    public void testPauseCommandExecutePause() {
        // Given
        PauseCommand command = new PauseCommand(true);

        // When
        command.execute(executor);

        // Then
        assertTrue(executor.executedCommands.contains("pause"));
        assertTrue(executor.isPaused);
    }

    @Test
    public void testPauseCommandExecuteResume() {
        // Given
        executor.isPaused = true; // 먼저 일시정지 상태로 설정
        PauseCommand command = new PauseCommand(false);

        // When
        command.execute(executor);

        // Then
        assertTrue(executor.executedCommands.contains("resume"));
        assertFalse(executor.isPaused);
    }

    @Test
    public void testPauseCommandToggle() {
        // 일시정지
        PauseCommand pauseCmd = new PauseCommand(true);
        pauseCmd.execute(executor);
        assertTrue(executor.isPaused);

        // 재개
        PauseCommand resumeCmd = new PauseCommand(false);
        resumeCmd.execute(executor);
        assertFalse(executor.isPaused);
    }
}
