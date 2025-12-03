package org.tetris.network.comand;

import org.junit.Before;
import org.junit.Test;
import org.tetris.network.mocks.TestGameMenuCommandExecutor;

import java.io.*;

import static org.junit.Assert.*;

/**
 * SyncReadyStateCommand 테스트
 */
public class SyncReadyStateCommandTest {

    private TestGameMenuCommandExecutor executor;

    @Before
    public void setUp() {
        executor = new TestGameMenuCommandExecutor();
    }

    @Test
    public void testSyncReadyStateCommandCreationWithTrue() {
        // When
        SyncReadyStateCommand command = new SyncReadyStateCommand(true);

        // Then
        assertTrue(command.isOpponentReady());
    }

    @Test
    public void testSyncReadyStateCommandCreationWithFalse() {
        // When
        SyncReadyStateCommand command = new SyncReadyStateCommand(false);

        // Then
        assertFalse(command.isOpponentReady());
    }

    @Test
    public void testSyncReadyStateCommandExecuteWithReady() {
        // Given
        SyncReadyStateCommand command = new SyncReadyStateCommand(true);

        // When
        command.execute(executor);

        // Then
        assertTrue(executor.executedCommands.contains("onReady"));
        assertTrue(executor.lastIsReady);
    }

    @Test
    public void testSyncReadyStateCommandExecuteWithNotReady() {
        // Given
        SyncReadyStateCommand command = new SyncReadyStateCommand(false);

        // When
        command.execute(executor);

        // Then
        assertTrue(executor.executedCommands.contains("onReady"));
        assertFalse(executor.lastIsReady);
    }

    @Test
    public void testSyncReadyStateCommandIsGameMenuCommand() {
        // Given
        SyncReadyStateCommand command = new SyncReadyStateCommand(true);

        // Then
        assertTrue(command instanceof GameMenuCommand);
        assertTrue(command instanceof Command);
    }

    @Test
    public void testSyncReadyStateCommandSerialization() throws IOException, ClassNotFoundException {
        // Given
        SyncReadyStateCommand originalCommand = new SyncReadyStateCommand(true);

        // When - 직렬화
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalCommand);
        oos.close();

        // Then - 역직렬화
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SyncReadyStateCommand deserializedCommand = (SyncReadyStateCommand) ois.readObject();
        ois.close();

        assertNotNull(deserializedCommand);
        assertTrue(deserializedCommand.isOpponentReady());
    }

    @Test
    public void testSyncReadyStateCommandSerializationWithFalse() throws IOException, ClassNotFoundException {
        // Given
        SyncReadyStateCommand originalCommand = new SyncReadyStateCommand(false);

        // When - 직렬화
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalCommand);
        oos.close();

        // Then - 역직렬화
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SyncReadyStateCommand deserializedCommand = (SyncReadyStateCommand) ois.readObject();
        ois.close();

        assertNotNull(deserializedCommand);
        assertFalse(deserializedCommand.isOpponentReady());
    }

    @Test
    public void testSyncReadyStateCommandToggle() {
        // 상대방 Ready
        SyncReadyStateCommand readyCmd = new SyncReadyStateCommand(true);
        readyCmd.execute(executor);
        assertTrue(executor.lastIsReady);

        // 상대방 Not Ready
        SyncReadyStateCommand notReadyCmd = new SyncReadyStateCommand(false);
        notReadyCmd.execute(executor);
        assertFalse(executor.lastIsReady);
    }
}
