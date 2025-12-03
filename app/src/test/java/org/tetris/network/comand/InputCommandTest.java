package org.tetris.network.comand;

import org.junit.Before;
import org.junit.Test;
import org.tetris.network.mocks.TestGameCommandExecutor;

import static org.junit.Assert.*;

import java.io.*;

/**
 * InputCommand 단위 테스트
 */
public class InputCommandTest {

    private TestGameCommandExecutor executor;

    @Before
    public void setUp() {
        executor = new TestGameCommandExecutor();
    }

    @Test
    public void testInputCommandCreation() {
        // Given
        int playerNumber = 1;
        long localSeq = 5;
        String action = "moveLeft";

        // When
        InputCommand cmd = new InputCommand(playerNumber, localSeq, action, 0L);

        // Then
        assertEquals(playerNumber, cmd.getPlayerNumber());
        assertEquals(localSeq, cmd.getLocalSeq());
        assertEquals(action, cmd.getAction());
        assertEquals(-1, cmd.getGlobalSeq()); // 초기값은 -1
        assertTrue(cmd.getTimestamp() > 0); // 타임스탬프는 현재 시간
    }

    @Test
    public void testSetGlobalSeq() {
        // Given
        InputCommand cmd = new InputCommand(1, 10, "rotate", 0L);
        long globalSeq = 100;

        // When
        cmd.setGlobalSeq(globalSeq);

        // Then
        assertEquals(globalSeq, cmd.getGlobalSeq());
    }

    @Test
    public void testDifferentActions() {
        String[] actions = {"moveLeft", "moveRight", "rotate", "softDrop", "hardDrop"};

        for (String action : actions) {
            InputCommand cmd = new InputCommand(2, 1, action, 0L);
            assertEquals(action, cmd.getAction());
        }
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        // Given
        InputCommand original = new InputCommand(1, 42, "hardDrop", 0L);
        original.setGlobalSeq(100);

        // When - 직렬화
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        // When - 역직렬화
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        InputCommand deserialized = (InputCommand) ois.readObject();
        ois.close();

        // Then
        assertEquals(original.getPlayerNumber(), deserialized.getPlayerNumber());
        assertEquals(original.getLocalSeq(), deserialized.getLocalSeq());
        assertEquals(original.getGlobalSeq(), deserialized.getGlobalSeq());
        assertEquals(original.getAction(), deserialized.getAction());
        assertEquals(original.getTimestamp(), deserialized.getTimestamp());
    }

    @Test
    public void testToString() {
        // Given
        InputCommand cmd = new InputCommand(2, 15, "softDrop", 0L);
        cmd.setGlobalSeq(200);

        // When
        String result = cmd.toString();

        // Then
        assertTrue(result.contains("player=2"));
        assertTrue(result.contains("local=15"));
        assertTrue(result.contains("global=200"));
        assertTrue(result.contains("action=softDrop"));
    }

    @Test
    public void testExecute() {
        // Given
        InputCommand cmd = new InputCommand(1, 5, "moveRight", 0L);
        cmd.setGlobalSeq(50);

        // When
        cmd.execute(executor);

        // Then
        assertTrue(executor.executedCommands.contains("executeInput"));
        assertEquals(cmd, executor.lastInputCommand);
    }

    @Test
    public void testSequenceOrdering() {
        // Given
        InputCommand cmd1 = new InputCommand(1, 1, "moveLeft", 0L);
        InputCommand cmd2 = new InputCommand(1, 2, "moveRight", 0L);
        InputCommand cmd3 = new InputCommand(1, 3, "rotate", 0L);

        cmd1.setGlobalSeq(10);
        cmd2.setGlobalSeq(11);
        cmd3.setGlobalSeq(12);

        // Then
        assertTrue(cmd1.getLocalSeq() < cmd2.getLocalSeq());
        assertTrue(cmd2.getLocalSeq() < cmd3.getLocalSeq());
        assertTrue(cmd1.getGlobalSeq() < cmd2.getGlobalSeq());
        assertTrue(cmd2.getGlobalSeq() < cmd3.getGlobalSeq());
    }

    @Test
    public void testTimestamp() throws InterruptedException {
        // Given
        long before = System.currentTimeMillis();
        Thread.sleep(10); // 약간의 시간 차이 보장

        // When
        InputCommand cmd = new InputCommand(1, 1, "moveLeft", before);

        Thread.sleep(10);
        long after = System.currentTimeMillis();

        // Then
        assertTrue(cmd.getTimestamp() >= before);
        assertTrue(cmd.getTimestamp() <= after);
    }

    @Test
    public void testPlayerNumberValidation() {
        // Player 1
        InputCommand cmd1 = new InputCommand(1, 1, "moveLeft", 0L);
        assertEquals(1, cmd1.getPlayerNumber());

        // Player 2
        InputCommand cmd2 = new InputCommand(2, 1, "moveRight", 0L);
        assertEquals(2, cmd2.getPlayerNumber());
    }

    @Test
    public void testLocalSeqIncrement() {
        // Given
        InputCommand cmd1 = new InputCommand(1, 1, "moveLeft", 0L);
        InputCommand cmd2 = new InputCommand(1, 2, "moveRight", 0L);
        InputCommand cmd3 = new InputCommand(1, 3, "rotate", 0L);

        // Then
        assertEquals(1, cmd1.getLocalSeq());
        assertEquals(2, cmd2.getLocalSeq());
        assertEquals(3, cmd3.getLocalSeq());
    }
}
