package org.tetris.network.comand;

import org.junit.Before;
import org.junit.Test;
import org.tetris.network.mocks.TestGameCommandExecutor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * GameOverCommand 테스트
 */
public class GameOverCommandTest {

    private TestGameCommandExecutor executor;

    @Before
    public void setUp() {
        executor = new TestGameCommandExecutor();
    }

    @Test
    public void testGameOverCommandCreationWithScoreOnly() {
        // When
        GameOverCommand command = new GameOverCommand(1000);

        // Then
        assertEquals(1000, command.getScore());
        assertNotNull(command.getPendingAttacks());
        assertTrue(command.getPendingAttacks().isEmpty());
    }

    @Test
    public void testGameOverCommandCreationWithPendingAttacks() {
        // Given
        List<int[]> pendingAttacks = new ArrayList<>();
        pendingAttacks.add(new int[]{8, 8, 8, 0, 8, 8, 8, 8, 8, 8});
        pendingAttacks.add(new int[]{8, 8, 0, 8, 8, 8, 8, 8, 8, 8});

        // When
        GameOverCommand command = new GameOverCommand(500, pendingAttacks);

        // Then
        assertEquals(500, command.getScore());
        assertNotNull(command.getPendingAttacks());
        assertEquals(2, command.getPendingAttacks().size());
    }

    @Test
    public void testGameOverCommandCreationWithNullPendingAttacks() {
        // When
        GameOverCommand command = new GameOverCommand(300, null);

        // Then
        assertEquals(300, command.getScore());
        assertNotNull(command.getPendingAttacks());
        assertTrue(command.getPendingAttacks().isEmpty());
    }

    @Test
    public void testGameOverCommandExecute() {
        // Given
        GameOverCommand command = new GameOverCommand(750);

        // When
        command.execute(executor);

        // Then
        assertTrue(executor.executedCommands.contains("gameOver"));
        assertEquals(750, executor.lastScore);
        assertNotNull(executor.lastPendingAttacks);
        assertTrue(executor.lastPendingAttacks.isEmpty());
    }

    @Test
    public void testGameOverCommandExecuteWithPendingAttacks() {
        // Given
        List<int[]> pendingAttacks = new ArrayList<>();
        pendingAttacks.add(new int[]{8, 8, 8, 0, 8, 8, 8, 8, 8, 8});
        pendingAttacks.add(new int[]{8, 8, 0, 8, 8, 8, 8, 8, 8, 8});
        pendingAttacks.add(new int[]{8, 0, 8, 8, 8, 8, 8, 8, 8, 8});
        GameOverCommand command = new GameOverCommand(1200, pendingAttacks);

        // When
        command.execute(executor);

        // Then
        assertTrue(executor.executedCommands.contains("gameOver"));
        assertEquals(1200, executor.lastScore);
        assertNotNull(executor.lastPendingAttacks);
        assertEquals(3, executor.lastPendingAttacks.size());
    }

    @Test
    public void testGameOverCommandPendingAttacksIsCopied() {
        // Given
        List<int[]> pendingAttacks = new ArrayList<>();
        pendingAttacks.add(new int[]{8, 8, 8, 0, 8, 8, 8, 8, 8, 8});

        // When
        GameOverCommand command = new GameOverCommand(100, pendingAttacks);
        pendingAttacks.add(new int[]{8, 8, 0, 8, 8, 8, 8, 8, 8, 8}); // 원본 수정

        // Then - 원본 수정이 커맨드에 영향을 주지 않아야 함
        assertEquals(1, command.getPendingAttacks().size());
    }
}
