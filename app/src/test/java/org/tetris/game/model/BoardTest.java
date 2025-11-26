package org.tetris.game.model;

import static org.junit.Assert.*;

import org.junit.Test;

import org.tetris.game.model.blocks.*;

import org.util.Point;

public class BoardTest {

    @Test
    public void testBoardInitialization() {
        Board board = new Board(5, 5);
        String expected = "0 0 0 0 0 \n" +
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

        String expected = "7 7 0 0 0 \n" +
                "0 7 7 0 0 \n" +
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n";

        System.err.println(board);

        assertEquals(expected, board.toString());

    }

    @Test
    public void testBoardRemoveBlock() {
        Board board = new Board(5, 5);
        Block block = new ZBlock();
        Point position = new Point(1, 1);

        board.placeBlock(position, block);
        board.removeBlock(position, block);

        String expected = "0 0 0 0 0 \n" +
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

        // ZBlock pivot (1, 1), Point(0, 0)에서 시작하면 실제 위치 (-1, -1)
        // 열이 -1이므로 왼쪽 경계 벗어남 -> false
        assertEquals(false, board.isValidPos(new Point(0, 0), block));

        // Point(1, 2)에서 시작하면 실제 위치 (0, 1)
        // ZBlock shape가 2x3이므로 (0,1)~(1,3) 범위, 경계 내 -> true
        assertEquals(true, board.isValidPos(new Point(1, 2), block));
    }

    @Test
    public void testBoardMoveDown() {
        Board board = new Board(5, 5);
        Block block = new ZBlock();

        board.setActiveBlock(block);

        board.moveDown();

        String expected = 
                "0 7 7 0 0 \n" +
                "0 0 7 7 0 \n" +
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
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

        // IBlock은 initialPos (-1, 2)에서 pivot (0, 1)을 빼면 (-1, 1)부터 시작
        // 1x4 크기이므로 -1행의 열 1,2,3,4를 차지하지만 -1행은 무시됨
        // 따라서 0행의 일부 열을 막아서 게임 오버 유도
        int[][] boardArray = board.getBoard();
        
        // 0행과 1행을 막아서 확실하게 게임 오버 유도
        for (int c = 0; c < 5; c++) {
            boardArray[0][c] = 1;
            boardArray[1][c] = 1;
        }

        Block block = new OBlock();
        boolean result = board.setActiveBlock(block);

        assertFalse(result); // 게임 오버
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

        boolean moved = board.moveDown();

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

        boolean moved = board.moveDown();

        assertFalse(moved);
    }

    // ==================== findFullRows, clearRow, collapse 테스트
    // ====================

    @Test
    public void testFindFullRowsEmpty() {
        Board board = new Board(5, 5);
        Block block = new IBlock();
        board.setActiveBlock(block);

        java.util.List<Integer> fullRows = board.findFullRows();

        assertTrue(fullRows.isEmpty());
    }

    @Test
    public void testFindFullRowsWithOneFullRow() {
        Board board = new Board(5, 5);
        Block block = new IBlock();
        board.setActiveBlock(block);

        for (int c = 0; c < 5; c++) {
            board.getBoard()[4][c] = 1;
        }

        java.util.List<Integer> fullRows = board.findFullRows();
        assertEquals(1, fullRows.size());
        assertTrue(fullRows.contains(4));
    }

    @Test
    public void testFindFullRowsWithMultipleFullRows() {
        Board board = new Board(5, 5);
        Block block = new IBlock();
        board.setActiveBlock(block);

        for (int c = 0; c < 5; c++) {
            board.getBoard()[3][c] = 1;
            board.getBoard()[4][c] = 1;
        }

        java.util.List<Integer> fullRows = board.findFullRows();
        assertEquals(2, fullRows.size());
        assertTrue(fullRows.contains(3));
        assertTrue(fullRows.contains(4));
    }

    @Test
    public void testClearRowSingleRow() {
        Board board = new Board(5, 5);

        // 4번째 줄을 완전히 채움
        for (int c = 0; c < 5; c++) {
            board.getBoard()[4][c] = 1;
        }

        board.clearRow(4);

        String expected = 
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n";

        assertEquals(expected, board.toString());
    }

    @Test
    public void testCollapse_NoClearedRows() {
        Board board = new Board(5, 5);

        // 각 행을 서로 다른 값으로 채워서 "비어있는 행(전부 0)"이 없게 만듦
        for (int c = 0; c < 5; c++)
            board.getBoard()[0][c] = 1;
        for (int c = 0; c < 5; c++)
            board.getBoard()[1][c] = 2;
        for (int c = 0; c < 5; c++)
            board.getBoard()[2][c] = 3;
        for (int c = 0; c < 5; c++)
            board.getBoard()[3][c] = 4;
        for (int c = 0; c < 5; c++)
            board.getBoard()[4][c] = 5;

        board.collapse(); // 비어있는 행이 없으므로 변화 없음

        String expected = 
                "1 1 1 1 1 \n" +
                "2 2 2 2 2 \n" +
                "3 3 3 3 3 \n" +
                "4 4 4 4 4 \n" +
                "5 5 5 5 5 \n";
        assertEquals(expected, board.toString());
    }

    @Test
    public void testCollapse_OneClearedRow() {
        Board board = new Board(5, 5);

        // 초기 상태(위→아래): 1행, 2행, 3행, 4행, 5행
        for (int c = 0; c < 5; c++)
            board.getBoard()[0][c] = 1;
        for (int c = 0; c < 5; c++)
            board.getBoard()[1][c] = 2;
        for (int c = 0; c < 5; c++)
            board.getBoard()[2][c] = 3;
        for (int c = 0; c < 5; c++)
            board.getBoard()[3][c] = 4;
        for (int c = 0; c < 5; c++)
            board.getBoard()[4][c] = 5;

        // 2번 인덱스(3번째 줄)를 clear했다고 가정
        board.clearRow(2);

        board.collapse();

        // 예상(위→아래): 0행(빈), 1행(1), 2행(2), 3행(4), 4행(5)
        String expected = 
                "0 0 0 0 0 \n" +
                "1 1 1 1 1 \n" +
                "2 2 2 2 2 \n" +
                "4 4 4 4 4 \n" +
                "5 5 5 5 5 \n";
        assertEquals(expected, board.toString());
    }

    @Test
    public void testCollapse_TwoClearedRows() {
        Board board = new Board(5, 5);

        // 초기 상태(위→아래): 1, 2, 3, 4, 5
        for (int c = 0; c < 5; c++)
            board.getBoard()[0][c] = 1;
        for (int c = 0; c < 5; c++)
            board.getBoard()[1][c] = 2;
        for (int c = 0; c < 5; c++)
            board.getBoard()[2][c] = 3;
        for (int c = 0; c < 5; c++)
            board.getBoard()[3][c] = 4;
        for (int c = 0; c < 5; c++)
            board.getBoard()[4][c] = 5;

        // 1번, 3번 인덱스(2번째, 4번째 줄) clear
        board.clearRow(1);
        board.clearRow(3);

        board.collapse();

        // 예상(위→아래): 0행(빈), 1행(빈), 2행(1), 3행(3), 4행(5)
        String expected = 
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
                "1 1 1 1 1 \n" +
                "3 3 3 3 3 \n" +
                "5 5 5 5 5 \n";
        assertEquals(expected, board.toString());
    }

    @Test
    public void testPushUp_Success() {
        // 5x5 보드 생성
        Board board = new Board(5, 5);
        int[][] boardData = board.getBoard();

        // 1. 초기 상태 설정: 맨 아랫줄(4행)에 데이터 존재
        // [0, 0, 0, 0, 0]
        // ...
        // [1, 1, 1, 1, 1]
        for (int c = 0; c < 5; c++) {
            boardData[4][c] = 1;
        }

        // 2. 새로운 줄 데이터 준비 (공격받은 줄: 9로 가득 참)
        int[] newRow = { 9, 9, 9, 9, 9 };

        // 3. pushUp 실행
        boolean result = board.pushUp(newRow);

        // 4. 검증
        assertTrue("맨 윗줄이 비어있다면 true를 반환해야 합니다.", result);

        // 기존 4행의 데이터가 3행으로 올라갔는지 확인
        assertEquals("기존 데이터가 위로 한 칸 이동해야 합니다.", 1, boardData[3][0]);

        // 4행(바닥)에 새로운 줄이 들어왔는지 확인
        assertEquals("맨 아래에 새로운 줄이 추가되어야 합니다.", 9, boardData[4][0]);
    }

    @Test
    public void testPushUp_GameOver_WhenTopRowNotEmpty() {
        Board board = new Board(5, 5);
        int[][] boardData = board.getBoard();

        // 1. 맨 윗줄(0번 행)에 블럭 배치 (장애물)
        boardData[0][2] = 1;

        // 2. pushUp 실행
        boolean result = board.pushUp(new int[] { 9, 9, 9, 9, 9 });

        // 3. 검증
        assertFalse("맨 윗줄에 블럭이 있으면 밀어올릴 공간이 없어 false(게임오버)여야 합니다.", result);
    }

    @Test
    public void testGetRowForAttack_BasicConversion() {
        Board board = new Board(5, 5);
        int[][] boardData = board.getBoard();

        // 1. 테스트할 행(3번 행) 설정: [1, 0, 2, 0, 3]
        // activeBlock이 없는 상황 가정
        boardData[3][0] = 1;
        boardData[3][1] = 0;
        boardData[3][2] = 2;
        boardData[3][3] = 0;
        boardData[3][4] = 3;

        // 2. 메서드 호출
        int[] attackRow = board.getRowForAttack(3);

        // 3. 검증: 블럭이 있던 곳(0이 아닌 곳)은 8(회색), 빈 곳은 0
        // 예상 결과: [8, 0, 8, 0, 8]
        assertArrayEquals(new int[] { 8, 0, 8, 0, 8 }, attackRow);
    }

    @Test
    public void testGetRowForAttack_WithActiveBlockHole() {
        Board board = new Board(5, 5);
        int[][] boardData = board.getBoard();

        // 1. ActiveBlock 설정 (IBlock)
        Block block = new IBlock();
        board.setActiveBlock(block);

        // [수정됨] 블럭이 3번 행까지 오도록 3번 내립니다.
        // 초기(0) -> 1 -> 2 -> 3
        board.moveDown();
        board.moveDown();
        board.moveDown(); // <--- 한 번 더 이동!

        // 2. 3번 행을 '꽉 채운 상태'로 설정
        for (int c = 0; c < 5; c++) {
            boardData[3][c] = 1;
        }

        // 3. 메서드 호출 (3번 행 기준으로 공격 줄 생성)
        int[] attackRow = board.getRowForAttack(3);

        // 4. 검증
        boolean hasHole = false;
        boolean hasGrayBlock = false;

        for (int val : attackRow) {
            if (val == 0)
                hasHole = true;
            if (val == 8)
                hasGrayBlock = true;
        }

        assertTrue("ActiveBlock이 겹치는 위치에는 구멍(0)이 뚫려야 합니다.", hasHole);
        assertTrue("ActiveBlock이 없는 나머지 부분은 회색(8)으로 변환되어야 합니다.", hasGrayBlock);
    }
}