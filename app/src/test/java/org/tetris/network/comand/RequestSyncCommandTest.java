package org.tetris.network.comand;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * RequestSyncCommand 테스트
 */
public class RequestSyncCommandTest {

    @Test
    public void testRequestSyncCommandCreation() {
        // When
        RequestSyncCommand command = new RequestSyncCommand();

        // Then
        assertNotNull(command);
        assertTrue(command instanceof Command);
    }

    @Test
    public void testRequestSyncCommandSerialization() throws IOException, ClassNotFoundException {
        // Given
        RequestSyncCommand originalCommand = new RequestSyncCommand();

        // When - 직렬화
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(originalCommand);
        oos.close();

        // Then - 역직렬화
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        RequestSyncCommand deserializedCommand = (RequestSyncCommand) ois.readObject();
        ois.close();

        assertNotNull(deserializedCommand);
        assertTrue(deserializedCommand instanceof Command);
    }

    @Test
    public void testRequestSyncCommandIsCommand() {
        // Given
        RequestSyncCommand command = new RequestSyncCommand();

        // Then
        assertTrue(command instanceof Command);
        assertFalse(command instanceof GameCommand);
        assertFalse(command instanceof GameMenuCommand);
    }

    @Test
    public void testMultipleRequestSyncCommandCreation() {
        // When
        RequestSyncCommand command1 = new RequestSyncCommand();
        RequestSyncCommand command2 = new RequestSyncCommand();

        // Then
        assertNotNull(command1);
        assertNotNull(command2);
        assertNotSame(command1, command2);
    }
}
