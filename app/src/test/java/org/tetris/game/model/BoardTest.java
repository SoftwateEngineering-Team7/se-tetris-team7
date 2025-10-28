package org.tetris.game.model;

import static org.junit.Assert.*;

import org.junit.Test;

import org.tetris.game.model.blocks.*;

import org.util.Point;

public class BoardTest {
    
    @Test
    public void testBoardInitialization() {
        Board board = new Board(5, 5);
        String expected =
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n";

        System.err.println(board);

        assertEquals(expected, board.toString());
    }

    @Test
    public void testBoardPlaceBlock() {
        Board board = new Board(5, 5);
        Block block = new ZBlock();
        board.placeBlock(new Point(1, 1), block);

        String expected =
            "7 7 0 0 0 \n" +
            "0 7 7 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n";

        System.err.println(board);

        assertEquals(expected, board.toString());

    }

    @Test
    public void testBoardRemoveBlock()
    {
        Board board = new Board(5, 5);
        Block block = new ZBlock();
        Point position = new Point(1, 1);

        board.placeBlock(position, block);
        board.removeBlock(position, block);

        String expected =
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n";

        System.err.println(board);

        assertEquals(expected, board.toString());
    }

    @Test
    public void testBoardIsValidPos() {
        Board board = new Board(5, 5);
        Block block = new ZBlock();

        assertEquals(false, board.isValidPos(new Point(0, 0), block));

        assertEquals(true, board.isValidPos(new Point(1, 1), block));
    }

    @Test
    public void testBoardMoveDown() {
        Board board = new Board(5, 5);
        Block block = new ZBlock();

        board.setActiveBlock(block);

        board.moveDown();

        String expected =
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 7 7 0 0 \n" +
            "0 0 7 7 0 \n" +
            "0 0 0 0 0 \n";

        System.out.println(board);

        assertEquals(expected, board.toString());
    }

    @Test
    public void testSetActiveBlock() {
        Board board = new Board(5, 5);
        Block block = new IBlock();

        boolean result = board.setActiveBlock(block);

        assertTrue(result);
        assertNotNull(board.activeBlock);
        assertEquals(block, board.activeBlock);
    }

    @Test
    public void testSetActiveBlockGameOver() {
        Board board = new Board(5, 5);
        
        // 보드를 채워서 블록을 배치할 수 없게 만듦
        Block firstBlock = new OBlock();
        board.placeBlock(new Point(0, 2), firstBlock);
        board.placeBlock(new Point(2, 2), firstBlock);

        Block newBlock = new IBlock();
        boolean result = board.setActiveBlock(newBlock);

        assertFalse(result); // 게임 오버
    }

    @Test
    public void testClearLines() {
        Board board = new Board(5, 5);

        // 첫 번째 줄을 완전히 채움
        for (int c = 0; c < 5; c++) {
            board.getBoard()[4][c] = 1;
        }

        int linesCleared = board.clearLines();

        assertEquals(1, linesCleared);

        String expected =
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n";

        assertEquals(expected, board.toString());
    }

    @Test
    public void testClearMultipleLines() {
        Board board = new Board(5, 5);

        // 두 줄을 완전히 채움
        for (int c = 0; c < 5; c++) {
            board.getBoard()[3][c] = 2;
            board.getBoard()[4][c] = 3;
        }

        int linesCleared = board.clearLines();

        assertEquals(2, linesCleared);

        String expected =
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n";

        assertEquals(expected, board.toString());
    }

    @Test
    public void testClearLinesWithBlocksAbove() {
        Board board = new Board(5, 5);

        // 위에 블록이 있는 상태에서 라인 클리어
        board.getBoard()[2][2] = 5;
        for (int c = 0; c < 5; c++) {
            board.getBoard()[4][c] = 1;
        }

        int linesCleared = board.clearLines();

        assertEquals(1, linesCleared);

        // 위의 블록이 아래로 내려와야 함
        String expected =
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 5 0 0 \n" +
            "0 0 0 0 0 \n";

        assertEquals(expected, board.toString());
    }

    @Test
    public void testHardDrop() {
        Board board = new Board(10, 5);
        Block block = new IBlock();

        board.setActiveBlock(block);

        int dropDistance = board.hardDrop();

        assertTrue(dropDistance > 0);
        
        // 블록이 바닥에 배치되었는지 확인
        int[][] boardArray = board.getBoard();
        boolean foundBlock = false;
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 5; c++) {
                if (boardArray[r][c] == 1) { // IBlock의 값
                    foundBlock = true;
                    break;
                }
            }
        }
        assertTrue(foundBlock);
    }

    @Test
    public void testReset() {
        Board board = new Board(5, 5);
        Block block = new ZBlock();

        board.setActiveBlock(block);
        board.moveDown();

        board.reset();

        String expected =
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n";

        assertEquals(expected, board.toString());
        assertNull(board.activeBlock);
    }

    @Test
    public void testAutoDownReturnsTrue() {
        Board board = new Board(10, 5);
        Block block = new IBlock();

        board.setActiveBlock(block);

        boolean moved = board.autoDown();

        assertTrue(moved);
    }

    @Test
    public void testAutoDownReturnsFalse() {
        Board board = new Board(5, 5);
        Block block = new OBlock();

        board.setActiveBlock(block);

        // 바닥까지 이동
        while (board.moveDown()) {
            // 계속 이동
        }

        boolean moved = board.autoDown();

        assertFalse(moved);
    }
}
