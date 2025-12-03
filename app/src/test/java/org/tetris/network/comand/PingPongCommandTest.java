package org.tetris.network.comand;

import org.junit.Test;
import org.tetris.network.mocks.TestGameCommandExecutor;
import org.tetris.network.mocks.TestGameMenuCommandExecutor;

import static org.junit.Assert.*;

/**
 * Ping/Pong 커맨드 테스트
 */
public class PingPongCommandTest {

    @Test
    public void testPingCommandCreatesTimestamp() {
        long before = System.currentTimeMillis();
        PingCommand command = new PingCommand();
        long after = System.currentTimeMillis();

        assertTrue(command.getTimestamp() >= before);
        assertTrue(command.getTimestamp() <= after);
    }

    @Test
    public void testPingCommandExecuteOnGameExecutor() {
        TestGameCommandExecutor executor = new TestGameCommandExecutor();

        PingCommand command = new PingCommand();
        command.execute(executor);

        // PingCommand.execute()는 특별한 동작 없이 디버그 용도
        // 에러 없이 실행되면 성공
        assertNotNull(command);
    }

    @Test
    public void testPingCommandExecuteOnMenuExecutor() {
        TestGameMenuCommandExecutor executor = new TestGameMenuCommandExecutor();

        PingCommand command = new PingCommand();
        command.execute(executor);

        // 에러 없이 실행되면 성공
        assertNotNull(command);
    }

    @Test
    public void testPongCommandStoresOriginalTimestamp() {
        long originalTimestamp = 1234567890L;

        PongCommand command = new PongCommand(originalTimestamp);

        assertEquals(originalTimestamp, command.getOriginalTimestamp());
    }

    @Test
    public void testPongCommandCalculatesPingOnGameExecutor() throws InterruptedException {
        TestGameCommandExecutor executor = new TestGameCommandExecutor();
        long originalTimestamp = System.currentTimeMillis();
        
        // 약간의 시간 지연
        Thread.sleep(10);

        PongCommand command = new PongCommand(originalTimestamp);
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("updatePing"));
        assertTrue(executor.lastPing >= 10);
    }

    @Test
    public void testPongCommandCalculatesPingOnMenuExecutor() throws InterruptedException {
        TestGameMenuCommandExecutor executor = new TestGameMenuCommandExecutor();
        long originalTimestamp = System.currentTimeMillis();
        
        // 약간의 시간 지연
        Thread.sleep(10);

        PongCommand command = new PongCommand(originalTimestamp);
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("updatePing"));
        assertTrue(executor.lastPing >= 10);
    }
}
