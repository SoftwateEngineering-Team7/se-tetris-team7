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

        board.activeBlock = block;

        board.moveDown();

        String expected =
            "0 7 7 0 0 \n" +
            "0 0 7 7 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n";

        System.out.println(board);

        assertEquals(expected, board.toString());

        board.moveDown();

        expected =
            "0 0 0 0 0 \n" +
            "0 7 7 0 0 \n" +
            "0 0 7 7 0 \n" +
            "0 0 0 0 0 \n" +
            "0 0 0 0 0 \n";

        System.out.println(board);

        assertEquals(expected, board.toString());
    }
}