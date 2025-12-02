package org.tetris.network.comand;

import org.junit.Before;
import org.junit.Test;
import org.tetris.network.mocks.TestGameCommandExecutor;

import static org.junit.Assert.*;

/**
 * DisconnectCommand 테스트
 */
public class DisconnectCommandTest {

    private TestGameCommandExecutor executor;

    @Before
    public void setUp() {
        executor = new TestGameCommandExecutor();
    }

    @Test
    public void testDisconnectCommandWithReason() {
        // Given
        String reason = "상대방이 게임을 종료했습니다.";

        // When
        DisconnectCommand command = new DisconnectCommand(reason);

        // Then
        assertEquals(reason, command.getReason());
    }

    @Test
    public void testDisconnectCommandDefaultReason() {
        // When
        DisconnectCommand command = new DisconnectCommand();

        // Then
        assertEquals("상대방이 게임을 종료했습니다.", command.getReason());
    }

    @Test
    public void testDisconnectCommandExecute() {
        // Given
        String reason = "네트워크 오류";
        DisconnectCommand command = new DisconnectCommand(reason);

        // When
        command.execute(executor);

        // Then
        assertTrue(executor.executedCommands.contains("onOpponentDisconnect"));
        assertEquals(reason, executor.lastDisconnectReason);
    }

    @Test
    public void testDisconnectCommandWithCustomReason() {
        // Given
        String customReason = "플레이어가 강제 종료했습니다.";
        DisconnectCommand command = new DisconnectCommand(customReason);

        // When
        command.execute(executor);

        // Then
        assertEquals(customReason, executor.lastDisconnectReason);
    }
}
