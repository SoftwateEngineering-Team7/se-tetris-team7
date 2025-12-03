package org.tetris.game.model;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;
import org.tetris.game.controller.ItemController;
import org.tetris.game.model.blocks.*;
import org.tetris.game.model.items.*;
import org.util.Point;

public class ItemActivateTest {
    private ItemActivation itemActivation = new ItemActivation() {
        @Override
        public void addClearingRow(int row) {}

        @Override
        public void addClearingCol(int col) {}

        @Override
        public void addClearingCells(java.util.List<Point> cells) {}
    };

    private Random random = new Random(0L);

    @Test
    public void testActivateWItem() {
        Board board = new Board(8, 8);
        Block block = new IBlock();
        Item item = new WItem();

        int[][] currentBoard = board.getBoard();
        for (int i = 2; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                currentBoard[i][j] = 1;
            }
        }
        
        block = item.getItemBlock(block, random);
        board.activeBlock = block;
        board.setCurPos(new Point(1, 3));
        board.hardDrop();
        item.activate(board, itemActivation);
        board.hardDrop();
        String expectedBoard =  "0 0 0 0 0 0 0 0 \n" +
                                "0 0 0 0 0 0 0 0 \n" +
                                "1 1 0 0 0 0 1 1 \n" +
                                "1 1 0 0 0 0 1 1 \n" +
                                "1 1 0 0 0 0 1 1 \n" +
                                "1 1 0 0 0 0 1 1 \n" +
                                "1 1 0 10 10 0 1 1 \n" +
                                "1 1 10 10 10 10 1 1 \n";

        assertEquals(expectedBoard, board.toString());
    }

    @Test
    public void testActivateLItem() {
    }

    @Test
    public void testActivateBItem() {
    }

    @Test
    public void testActivateHItem() {
    }
    
    @Test
    public void testActivateCItem() {
    }

}
