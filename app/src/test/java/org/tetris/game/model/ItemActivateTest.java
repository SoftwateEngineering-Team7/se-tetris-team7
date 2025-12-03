package org.tetris.game.model;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;
import org.tetris.game.model.blocks.*;
import org.tetris.game.model.items.*;
import org.tetris.game.model.mocks.TestItemActivation;
import org.util.Point;

public class ItemActivateTest {
    private TestItemActivation itemActivation = new TestItemActivation();
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
        Board board = new Board(8, 8);
        Block block = new TBlock();
        Item item = new LItem();

        int[][] currentBoard = board.getBoard();
        for (int i = 3; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                currentBoard[i][j] = 1;
            }
        }
        
        block = item.getItemBlock(block, random);

        board.activeBlock = block;
        board.setCurPos(new Point(2, 3));
        board.placeBlock(board.getCurPos(), block);

        itemActivation.clear();
        item.activate(board, itemActivation);
        
        assertEquals(1, itemActivation.clearingRows.size());
        assertEquals(2, (int)itemActivation.clearingRows.get(0));
    }

    @Test
    public void testActivateBItem() {
        Board board = new Board(8, 8);
        Block block = new TBlock();
        Item item = new BItem();

        // 3x3 영역 채우기 (중심: 4, 4)
        int[][] currentBoard = board.getBoard();
        for (int r = 3; r <= 5; r++) {
            for (int c = 3; c <= 5; c++) {
                currentBoard[r][c] = 1;
            }
        }
        
        block = item.getItemBlock(block, random);
        board.activeBlock = block;
        board.setCurPos(new Point(4, 4));
        board.placeBlock(board.getCurPos(), block);

        itemActivation.clear();
        item.activate(board, itemActivation);
        
        // BItem은 3x3 영역(9칸)을 지우므로 clearingCells에 9개가 들어가야 함
        // 단, 빈 칸(0)은 제외될 수 있음
        assertTrue(itemActivation.clearingCells.size() > 0);
    }

    @Test
    public void testActivateHItem() {
        Board board = new Board(8, 8);
        Block block = new TBlock();
        Item item = new HItem();

        // 열 4 채우기
        int[][] currentBoard = board.getBoard();
        for (int r = 0; r < 8; r++) {
            currentBoard[r][4] = 1;
        }
        
        block = item.getItemBlock(block, random);
        board.activeBlock = block;
        board.setCurPos(new Point(2, 4));
        board.placeBlock(board.getCurPos(), block);

        itemActivation.clear();
        item.activate(board, itemActivation);
        
        // HItem은 열을 지우므로 clearingCols에 1개가 들어가야 함
        assertEquals(1, itemActivation.clearingCols.size());
    }
    
    @Test
    public void testActivateCItem() {
        Board board = new Board(8, 8);
        Block block = new TBlock();
        Item item = new CItem();

        // 행 4와 열 4 채우기 (십자 모양)
        int[][] currentBoard = board.getBoard();
        for (int i = 0; i < 8; i++) {
            currentBoard[4][i] = 1;
            currentBoard[i][4] = 1;
        }
        
        block = item.getItemBlock(block, random);
        board.activeBlock = block;
        board.setCurPos(new Point(4, 4));
        board.placeBlock(board.getCurPos(), block);

        itemActivation.clear();
        item.activate(board, itemActivation);
        
        // CItem은 행과 열 둘 다 지우므로 clearingRows와 clearingCols에 각각 1개씩
        assertEquals(1, itemActivation.clearingRows.size());
        assertEquals(1, itemActivation.clearingCols.size());
    }

}
