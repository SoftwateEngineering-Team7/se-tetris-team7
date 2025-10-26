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
        Point beforePos = new Point(20, 5); // 거의 아래까지 이동시켜서 고정 유도

        // 강제로 curPos를 아래쪽으로 세팅해본 뒤 autoDown 호출
        board.removeBlock(new Point(-1, 5)); // 기존 블록 제거
        board.placeBlock(beforePos);
        board.autoDown();

        // 블록이 바뀌었는지 (새 activeBlock 생성 확인)
        assertNotNull(board.activeBlock);
        assertNotEquals(beforeBlock, board.activeBlock);
    }

    @Test
    public void testIsValidPos() {
        // 초기 블럭 위치는 항상 유효해야 함
        assertTrue(board.isValidPos(new Point(-1, 5)));

        // 경계 밖 좌표는 false
        assertFalse(board.isValidPos(new Point(25, 15)));
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
