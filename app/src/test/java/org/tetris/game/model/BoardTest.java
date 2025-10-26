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
    public void testRotate() {
        // 회전이 가능한 위치라면 true 반환
        boolean rotated = board.rotate();
        assertTrue(rotated);
    }

    @Test
    public void testAutoDownSetsNewBlock() {
        Block beforeBlock = board.activeBlock;

        // 블록을 바닥까지 이동시켜서 고정 유도
        // 최대 21번 이동 (HEIGHT만큼)
        for (int i = 0; i < 21; i++) {
            if (!board.moveDown()) {
                // 더 이상 이동할 수 없으면 autoDown을 호출하여 새 블록 생성
                board.setActiveToStaticBlock();
                break;
            }
        }

        // 블록이 바뀌었는지 (새 activeBlock 생성 확인)
        assertNotNull(board.activeBlock);
        assertNotEquals(beforeBlock, board.activeBlock);
    }

    @Test
    public void testIsValidPos() {
        // 블록을 제거하고 경계 안의 빈 위치가 유효한지 확인
        board.removeBlock(new Point(-1, 5)); // 초기 블록 제거
        
        // (5, 5)는 경계 안의 빈 공간이므로 유효해야 함
        assertTrue("경계 안의 빈 위치는 유효해야 합니다.", board.isValidPos(new Point(5, 5)));
        
        // 경계 밖 좌표는 false (블록의 모든 셀이 경계 밖에 있는 위치)
        // HEIGHT=21, WIDTH=12이므로 (100, 100)은 어떤 블록이든 모든 셀이 경계 밖
        assertFalse("완전히 경계 밖의 위치는 무효해야 합니다.", board.isValidPos(new Point(100, 100)));
        assertFalse("완전히 경계 밖의 위치는 무효해야 합니다.", board.isValidPos(new Point(-100, -100)));
        
        // 원래 위치로 복원
        board.placeBlock(new Point(-1, 5));
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
