package org.tetris.network.comand;

import org.junit.Test;
import org.tetris.network.mocks.TestGameCommandExecutor;

import static org.junit.Assert.*;

/**
 * GameCommand 구현체들에 대한 테스트
 * MoveLeftCommand, MoveRightCommand, RotateCommand, SoftDropCommand, HardDropCommand,
 * UpdateStateCommand, GameResultCommand 테스트
 */
public class GameCommandTest {

    @Test
    public void testMoveLeftCommand() {
        TestGameCommandExecutor executor = new TestGameCommandExecutor();

        MoveLeftCommand command = new MoveLeftCommand();
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("moveLeft"));
    }

    @Test
    public void testMoveRightCommand() {
        TestGameCommandExecutor executor = new TestGameCommandExecutor();

        MoveRightCommand command = new MoveRightCommand();
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("moveRight"));
    }

    @Test
    public void testRotateCommand() {
        TestGameCommandExecutor executor = new TestGameCommandExecutor();

        RotateCommand command = new RotateCommand();
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("rotate"));
    }

    @Test
    public void testSoftDropCommand() {
        TestGameCommandExecutor executor = new TestGameCommandExecutor();

        SoftDropCommand command = new SoftDropCommand();
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("softDrop"));
    }

    @Test
    public void testHardDropCommand() {
        TestGameCommandExecutor executor = new TestGameCommandExecutor();

        HardDropCommand command = new HardDropCommand();
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("hardDrop"));
    }

    @Test
    public void testUpdateStateCommand() {
        TestGameCommandExecutor executor = new TestGameCommandExecutor();
        int[][] board = new int[20][10];
        board[19][0] = 1;
        board[18][5] = 2;
        int currentPosRow = 5;
        int currentPosCol = 3;
        int score = 1000;

        UpdateStateCommand command = new UpdateStateCommand(board, currentPosRow, currentPosCol, score);
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("updateState"));
        assertNotNull(executor.lastBoardState);
        assertEquals(1, executor.lastBoardState[19][0]);
        assertEquals(2, executor.lastBoardState[18][5]);
        assertEquals(currentPosRow, executor.lastCurrentPos.r);
        assertEquals(currentPosCol, executor.lastCurrentPos.c);
        assertEquals(score, executor.lastStateScore);
    }

    @Test
    public void testUpdateStateCommandDeepCopiesBoard() {
        int[][] originalBoard = new int[20][10];
        originalBoard[0][0] = 5;

        UpdateStateCommand command = new UpdateStateCommand(originalBoard, 0, 0, 0);
        
        // 원본 배열 변경
        originalBoard[0][0] = 99;

        // 커맨드 내부 배열은 변경되지 않아야 함
        assertEquals(5, command.getBoard()[0][0]);
    }

    @Test
    public void testUpdateStateCommandGetCurrentPos() {
        int[][] board = new int[20][10];
        int row = 10;
        int col = 5;

        UpdateStateCommand command = new UpdateStateCommand(board, row, col, 500);

        assertEquals(row, command.getCurrentPos().r);
        assertEquals(col, command.getCurrentPos().c);
    }

    @Test
    public void testGameResultCommandWinner() {
        TestGameCommandExecutor executor = new TestGameCommandExecutor();

        GameResultCommand command = new GameResultCommand(true, 5000);
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("onGameResult"));
        assertTrue(executor.lastIsWinner);
        assertEquals(5000, executor.lastScore);
    }

    @Test
    public void testGameResultCommandLoser() {
        TestGameCommandExecutor executor = new TestGameCommandExecutor();

        GameResultCommand command = new GameResultCommand(false, 3000);
        command.execute(executor);

        assertTrue(executor.executedCommands.contains("onGameResult"));
        assertFalse(executor.lastIsWinner);
        assertEquals(3000, executor.lastScore);
    }
}
