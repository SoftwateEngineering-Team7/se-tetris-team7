package org.tetris.game.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.tetris.game.model.blocks.Block;
import org.util.Point;

public class BoardTest {
    private Board board;

    @Before
    public void setUp() {
        board = new Board(21, 12);
    }

    @Test
    public void testBoardInitialization() {
        // 보드의 경계선이 잘 초기화되었는지 확인
        assertEquals(1, board.getCell(20, 0)); // 왼쪽 아래 모서리
        assertEquals(1, board.getCell(20, 11)); // 오른쪽 아래 모서리
        assertEquals(1, board.getCell(20, 5)); // 바닥
        assertEquals(1, board.getCell(10, 0)); // 왼쪽 벽
        assertEquals(1, board.getCell(10, 11)); // 오른쪽 벽
    }

    @Test
    public void testIsInBound() {
        assertTrue(board.isInBound(0, 0));
        assertTrue(board.isInBound(19, 10));
        assertFalse(board.isInBound(-1, 5));
        assertFalse(board.isInBound(21, 5));
        assertFalse(board.isInBound(5, 12));
    }

    @Test
    public void testMoveDown() {
        // 초기 위치에서 아래로 이동 시도
        boolean moved = board.moveDown();
        assertTrue(moved);
    }

    @Test
    public void testMoveLeftAndRight() {
        // 왼쪽/오른쪽 이동 시 유효한지 확인
        boolean movedRight = board.moveRight();
        boolean movedLeft = board.moveLeft();

        // 적어도 한쪽 방향으로는 이동 가능해야 함
        assertTrue(movedRight || movedLeft);
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
    public void testSetActiveToStaticBlockCreatesNewBlock() {
        Block before = board.activeBlock;
        board.setActiveToStaticBlock();
        Block after = board.activeBlock;

        assertNotNull(after);
        assertNotEquals(before, after);
    }
}
