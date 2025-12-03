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

    // ==================== pushUp 테스트 ====================

    @Test
    public void testPushUp_EmptyList() {
        Board board = new Board(5, 5);
        java.util.List<int[]> emptyRows = new java.util.ArrayList<>();
        
        boolean result = board.pushUp(emptyRows);
        
        assertTrue("빈 리스트를 전달하면 true를 반환해야 합니다.", result);
        
        // 보드가 변경되지 않았는지 확인
        String expected = 
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n";
        assertEquals(expected, board.toString());
    }

    @Test
    public void testPushUp_SingleRow_Success() {
        Board board = new Board(5, 5);
        int[][] boardData = board.getBoard();
        
        // 맨 아래 줄에 데이터 설정
        for (int c = 0; c < 5; c++) {
            boardData[4][c] = 1;
        }
        
        // 새로운 줄 추가
        java.util.List<int[]> newRows = new java.util.ArrayList<>();
        newRows.add(new int[]{8, 0, 8, 8, 8});
        
        boolean result = board.pushUp(newRows);
        
        assertTrue("맨 윗줄이 비어있으면 true를 반환해야 합니다.", result);
        
        // 기존 데이터가 위로 올라가고, 새 줄이 맨 아래에 추가되었는지 확인
        String expected = 
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
                "1 1 1 1 1 \n" +
                "8 0 8 8 8 \n";
        assertEquals(expected, board.toString());
    }

    @Test
    public void testPushUp_MultipleRows_Success() {
        Board board = new Board(5, 5);
        
        // 3개의 새로운 줄 추가
        java.util.List<int[]> newRows = new java.util.ArrayList<>();
        newRows.add(new int[]{8, 0, 8, 8, 8}); // 첫 번째로 추가됨 -> 가장 위에 위치
        newRows.add(new int[]{8, 8, 0, 8, 8}); // 두 번째로 추가됨
        newRows.add(new int[]{8, 8, 8, 0, 8}); // 마지막으로 추가됨 -> 맨 아래에 위치
        
        boolean result = board.pushUp(newRows);
        
        assertTrue("맨 윗줄이 비어있으면 true를 반환해야 합니다.", result);
        
        // 새 줄들이 아래에서부터 쌓이는지 확인
        String expected = 
                "0 0 0 0 0 \n" +
                "0 0 0 0 0 \n" +
                "8 0 8 8 8 \n" +
                "8 8 0 8 8 \n" +
                "8 8 8 0 8 \n";
        assertEquals(expected, board.toString());
    }

    @Test
    public void testPushUp_Overflow_ReturnsFalse() {
        Board board = new Board(5, 5);
        int[][] boardData = board.getBoard();
        
        // 0번 행(맨 윗줄)에 블럭 배치
        boardData[0][2] = 1;
        
        // 새로운 줄 추가
        java.util.List<int[]> newRows = new java.util.ArrayList<>();
        newRows.add(new int[]{8, 0, 8, 8, 8});
        
        boolean result = board.pushUp(newRows);
        
        assertFalse("맨 윗줄에 블럭이 있으면 false를 반환해야 합니다.", result);
        
        // 새 줄은 여전히 추가됨 (다 넣고 false 반환)
        assertEquals(8, boardData[4][0]);
    }

    @Test
    public void testPushUp_MultipleRows_Overflow() {
        Board board = new Board(5, 5);
        int[][] boardData = board.getBoard();
        
        // 2번 행에 블럭 배치 (3줄 추가 시 넘침)
        for (int c = 0; c < 5; c++) {
            boardData[2][c] = 1;
        }
        
        // 4줄 추가 시도 -> 기존 데이터가 밀려 올라가면서 넘침
        java.util.List<int[]> newRows = new java.util.ArrayList<>();
        newRows.add(new int[]{8, 0, 8, 8, 8});
        newRows.add(new int[]{8, 8, 0, 8, 8});
        newRows.add(new int[]{8, 8, 8, 0, 8});
        newRows.add(new int[]{8, 8, 8, 8, 0});
        
        boolean result = board.pushUp(newRows);
        
        assertFalse("오버플로우 시 false를 반환해야 합니다.", result);
        
        // 모든 줄이 추가되었는지 확인 (다 넣고 false 반환하는 의도)
        assertEquals(0, newRows.size()); // 리스트가 비워졌는지 확인
    }

    @Test
    public void testPushUp_ShiftExistingBlocks() {
        Board board = new Board(5, 5);
        int[][] boardData = board.getBoard();
        
        // 3번, 4번 행에 데이터 설정
        for (int c = 0; c < 5; c++) {
            boardData[3][c] = 3;
            boardData[4][c] = 4;
        }
        
        // 2줄 추가
        java.util.List<int[]> newRows = new java.util.ArrayList<>();
        newRows.add(new int[]{9, 9, 9, 9, 9});
        newRows.add(new int[]{7, 7, 7, 7, 7});
        
        boolean result = board.pushUp(newRows);
        
        assertTrue(result);
        
        // 기존 데이터가 위로 밀리고 새 줄이 아래에 추가되었는지 확인
        String expected = 
                "0 0 0 0 0 \n" +
                "3 3 3 3 3 \n" +
                "4 4 4 4 4 \n" +
                "9 9 9 9 9 \n" +
                "7 7 7 7 7 \n";
        assertEquals(expected, board.toString());
    }

    // ==================== setCurPos 테스트 ====================

    @Test
    public void testSetCurPos() {
        Board board = new Board(5, 5);
        Block block = new IBlock();
        board.setActiveBlock(block);
        
        Point newPos = new Point(2, 3);
        board.setCurPos(newPos);
        
        assertEquals(newPos, board.getCurPos());
    }
}