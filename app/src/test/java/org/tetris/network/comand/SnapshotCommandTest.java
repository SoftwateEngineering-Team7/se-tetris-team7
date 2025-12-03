package org.tetris.network.comand;

import org.junit.Before;
import org.junit.Test;
import org.tetris.network.mocks.TestGameCommandExecutor;

import static org.junit.Assert.*;

import java.io.*;

/**
 * SnapshotCommand 단위 테스트
 */
public class SnapshotCommandTest {

    private TestGameCommandExecutor executor;

    @Before
    public void setUp() {
        executor = new TestGameCommandExecutor();
    }

    @Test
    public void testSnapshotCommandCreation() {
        // Given
        long authSeq = 100;
        int[][] p1Board = createTestBoard(20, 10, 1);
        int[][] p2Board = createTestBoard(20, 10, 2);
        int p1Score = 1000;
        int p2Score = 2000;
        long p1Seed = 12345;
        long p2Seed = 67890;

        // When
        SnapshotCommand cmd = new SnapshotCommand(
                authSeq, p1Board, p2Board, p1Score, p2Score, p1Seed, p2Seed);

        // Then
        assertEquals(authSeq, cmd.getAuthoritativeSeq());
        assertEquals(p1Score, cmd.getPlayer1Score());
        assertEquals(p2Score, cmd.getPlayer2Score());
        assertEquals(p1Seed, cmd.getPlayer1Seed());
        assertEquals(p2Seed, cmd.getPlayer2Seed());
    }

    @Test
    public void testBoardDeepCopy() {
        // Given
        int[][] originalBoard = createTestBoard(20, 10, 5);
        SnapshotCommand cmd = new SnapshotCommand(
                1, originalBoard, createTestBoard(20, 10, 0),
                100, 200, 0, 0);

        // When
        int[][] retrievedBoard = cmd.getPlayer1Board();
        originalBoard[0][0] = 999; // 원본 수정

        // Then
        assertNotEquals(999, retrievedBoard[0][0]); // Deep copy이므로 영향 없음
        assertEquals(5, retrievedBoard[0][0]); // 원래 값 유지
    }

    @Test
    public void testBoardGetterDeepCopy() {
        // Given
        int[][] p1Board = createTestBoard(20, 10, 3);
        SnapshotCommand cmd = new SnapshotCommand(
                1, p1Board, createTestBoard(20, 10, 0),
                100, 200, 0, 0);

        // When
        int[][] board1 = cmd.getPlayer1Board();
        int[][] board2 = cmd.getPlayer1Board();
        board1[0][0] = 999;

        // Then
        assertNotSame(board1, board2); // 다른 인스턴스
        assertNotEquals(999, board2[0][0]); // board1 수정이 board2에 영향 없음
    }

    @Test
    public void testNullBoard() {
        // When
        SnapshotCommand cmd = new SnapshotCommand(
                1, null, null, 100, 200, 0, 0);

        // Then
        assertNull(cmd.getPlayer1Board());
        assertNull(cmd.getPlayer2Board());
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        // Given
        int[][] p1Board = createTestBoard(20, 10, 7);
        int[][] p2Board = createTestBoard(20, 10, 8);
        SnapshotCommand original = new SnapshotCommand(
                150, p1Board, p2Board, 5000, 6000, 11111, 22222);

        // When - 직렬화
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        // When - 역직렬화
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SnapshotCommand deserialized = (SnapshotCommand) ois.readObject();
        ois.close();

        // Then
        assertEquals(original.getAuthoritativeSeq(), deserialized.getAuthoritativeSeq());
        assertEquals(original.getPlayer1Score(), deserialized.getPlayer1Score());
        assertEquals(original.getPlayer2Score(), deserialized.getPlayer2Score());
        assertEquals(original.getPlayer1Seed(), deserialized.getPlayer1Seed());
        assertEquals(original.getPlayer2Seed(), deserialized.getPlayer2Seed());

        // 보드 내용 검증
        assertArrayEquals(original.getPlayer1Board(), deserialized.getPlayer1Board());
        assertArrayEquals(original.getPlayer2Board(), deserialized.getPlayer2Board());
    }

    @Test
    public void testToString() {
        // Given
        SnapshotCommand cmd = new SnapshotCommand(
                250, createTestBoard(20, 10, 0), createTestBoard(20, 10, 0),
                3000, 4000, 0, 0);

        // When
        String result = cmd.toString();

        // Then
        assertTrue(result.contains("authSeq=250"));
        assertTrue(result.contains("p1Score=3000"));
        assertTrue(result.contains("p2Score=4000"));
    }

    @Test
    public void testExecute() {
        // Given
        SnapshotCommand cmd = new SnapshotCommand(
                100, createTestBoard(20, 10, 0), createTestBoard(20, 10, 0),
                1000, 2000, 0, 0);

        // When
        cmd.execute(executor);

        // Then
        assertTrue(executor.executedCommands.contains("restoreSnapshot"));
        assertEquals(cmd, executor.lastSnapshotCommand);
    }

    @Test
    public void testBoardDimensions() {
        // Given
        int height = 20;
        int width = 10;
        int[][] p1Board = createTestBoard(height, width, 1);
        int[][] p2Board = createTestBoard(height, width, 2);

        // When
        SnapshotCommand cmd = new SnapshotCommand(
                1, p1Board, p2Board, 100, 200, 0, 0);

        // Then
        assertEquals(height, cmd.getPlayer1Board().length);
        assertEquals(width, cmd.getPlayer1Board()[0].length);
        assertEquals(height, cmd.getPlayer2Board().length);
        assertEquals(width, cmd.getPlayer2Board()[0].length);
    }

    @Test
    public void testScoreBoundaries() {
        // Given & When
        SnapshotCommand cmd1 = new SnapshotCommand(
                1, createTestBoard(20, 10, 0), createTestBoard(20, 10, 0),
                0, 0, 0, 0);

        SnapshotCommand cmd2 = new SnapshotCommand(
                1, createTestBoard(20, 10, 0), createTestBoard(20, 10, 0),
                Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0);

        // Then
        assertEquals(0, cmd1.getPlayer1Score());
        assertEquals(0, cmd1.getPlayer2Score());
        assertEquals(Integer.MAX_VALUE, cmd2.getPlayer1Score());
        assertEquals(Integer.MAX_VALUE, cmd2.getPlayer2Score());
    }

    @Test
    public void testAuthoritativeSequence() {
        // Given
        long[] sequences = {0, 1, 100, 1000, Long.MAX_VALUE};

        for (long seq : sequences) {
            // When
            SnapshotCommand cmd = new SnapshotCommand(
                    seq, createTestBoard(20, 10, 0), createTestBoard(20, 10, 0),
                    100, 200, 0, 0);

            // Then
            assertEquals(seq, cmd.getAuthoritativeSeq());
        }
    }

    @Test
    public void testBothPlayersData() {
        // Given
        int[][] p1Board = createTestBoard(20, 10, 1);
        int[][] p2Board = createTestBoard(20, 10, 2);

        // When
        SnapshotCommand cmd = new SnapshotCommand(
                50, p1Board, p2Board, 1500, 2500, 111, 222);

        // Then
        assertEquals(1, cmd.getPlayer1Board()[0][0]);
        assertEquals(2, cmd.getPlayer2Board()[0][0]);
        assertEquals(1500, cmd.getPlayer1Score());
        assertEquals(2500, cmd.getPlayer2Score());
        assertEquals(111, cmd.getPlayer1Seed());
        assertEquals(222, cmd.getPlayer2Seed());
    }

    /**
     * 테스트용 보드 생성 헬퍼 메서드
     * @param height 보드 높이
     * @param width 보드 너비
     * @param fillValue 채울 값
     * @return 생성된 보드
     */
    private int[][] createTestBoard(int height, int width, int fillValue) {
        int[][] board = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board[i][j] = fillValue;
            }
        }
        return board;
    }
}
