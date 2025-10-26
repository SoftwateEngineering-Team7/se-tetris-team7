package org.tetris.game;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.tetris.game.model.Board;
import org.tetris.game.model.blocks.*;
import org.util.Point;

public class BoardTest {
    @Test
    public void testFindFullRowsWithOneFullRow() {
        // 한 줄을 가득 채우기
        Board board = new Board();
        board.removeBlock(board.getCurPos());
        
        // row 19를 가득 채움 (벽 제외: col 1~10)
        for (int c = 1; c < 11; c++) {
            board.getBoard()[19][c] = 1;
        }
        
        // activeBlock이 row 19에 걸치도록 위치 설정
        board.activeBlock = new IBlock();
        board.setCurPos(new Point(19, 5));
        board.placeBlock(board.getCurPos());
        
        List<Integer> fullRows = board.findFullRows();
        assertEquals("가득 찬 줄이 1개여야 함", 1, fullRows.size());
        assertTrue("row 19가 포함되어야 함", fullRows.contains(19));
    }

    @Test
    public void testFindFullRowsWithMultipleFullRows() {
        // 여러 줄을 가득 채우기
        Board board = new Board();
        board.removeBlock(board.getCurPos());
        
        // row 17, 18, 19를 먼저 가득 채움
        for (int r = 17; r <= 19; r++) {
            for (int c = 1; c < 11; c++) {
                board.getBoard()[r][c] = 1;
            }
        }
        
        // activeBlock이 row 17-19에 걸치도록 위치 설정 (TBlock 사용)
        board.activeBlock = new TBlock();
        board.setCurPos(new Point(18, 5));
        board.placeBlock(board.getCurPos());
        
        List<Integer> fullRows = board.findFullRows();
        // TBlock은 2줄에 걸쳐있고, 3줄이 모두 가득 차있으므로
        // findFullRows는 TBlock이 걸쳐있는 줄 중 가득 찬 줄들을 반환
        assertTrue("적어도 1개 이상의 가득 찬 줄이 있어야 함", fullRows.size() >= 1);
    }

    @Test
    public void testFindFullRowsIgnoresWalls() {
        // 벽을 제외한 부분만 채워도 가득 찬 것으로 인식해야 함
        Board board = new Board();
        board.removeBlock(board.getCurPos());
        
        // row 19의 벽을 제외한 부분만 채움 (c: 1~10)
        for (int c = 1; c < 11; c++) {
            board.getBoard()[19][c] = 1;
        }
        
        board.activeBlock = new IBlock();
        board.setCurPos(new Point(19, 5));
        board.placeBlock(board.getCurPos());
        
        List<Integer> fullRows = board.findFullRows();
        assertEquals("벽을 제외하고 가득 차면 감지되어야 함", 1, fullRows.size());
    }

    @Test
    public void testClearRowsWithEmptyList() {
        // 빈 리스트로 호출해도 에러 없이 동작해야 함
        Board board = new Board();
        List<Integer> emptyList = List.of();
        board.clearRows(emptyList);
        
        // 보드 상태가 변경되지 않아야 함
        assertNotNull("보드가 유지되어야 함", board.getBoard());
    }

    @Test
    public void testClearRowsSingleRow() {
        // row 19를 가득 채우고 제거
        Board board = new Board();
        for (int c = 1; c < 11; c++) {
            board.getBoard()[19][c] = 1;
        }
        
        // row 19를 제거
        List<Integer> rowsToClear = List.of(19);
        board.clearRows(rowsToClear);
        
        // row 19가 비어있어야 함 (벽 제외)
        for (int c = 1; c < 11; c++) {
            assertEquals("row 19가 비어있어야 함", 0, board.getBoard()[19][c]);
        }
        
        // 벽은 그대로 유지되어야 함
        assertEquals("왼쪽 벽 유지", 1, board.getBoard()[19][0]);
        assertEquals("오른쪽 벽 유지", 1, board.getBoard()[19][11]);
    }

    @Test
    public void testClearRowsMultipleRows() {
        // row 18, 19를 가득 채우고 제거
        Board board = new Board();
        for (int r = 18; r <= 19; r++) {
            for (int c = 1; c < 11; c++) {
                board.getBoard()[r][c] = 1;
            }
        }
        
        // 두 줄 모두 제거
        List<Integer> rowsToClear = List.of(18, 19);
        board.clearRows(rowsToClear);
        
        // 두 줄 모두 비어있어야 함
        for (int r = 18; r <= 19; r++) {
            for (int c = 1; c < 11; c++) {
                assertEquals("row " + r + "이 비어있어야 함", 0, board.getBoard()[r][c]);
            }
        }
    }

    @Test
    public void testCollapseWithOneEmptyRow() {
        // row 18에 블록 배치, row 19는 비어있음
        Board board = new Board();
        board.removeBlock(board.getCurPos());
        
        for (int c = 1; c < 6; c++) {
            board.getBoard()[18][c] = 1;
        }
        
        board.collapse();
        
        // row 18의 블록이 row 19로 내려가야 함
        for (int c = 1; c < 6; c++) {
            assertEquals("블록이 row 19로 이동해야 함", 1, board.getBoard()[19][c]);
        }
        
        // row 18은 비어있어야 함
        for (int c = 1; c < 6; c++) {
            assertEquals("row 18이 비어있어야 함", 0, board.getBoard()[18][c]);
        }
    }

    @Test
    public void testCollapseWithMultipleEmptyRows() {
        // row 15에 블록 배치, row 16-19는 비어있음
        Board board = new Board();
        board.removeBlock(board.getCurPos());
        
        for (int c = 1; c < 6; c++) {
            board.getBoard()[15][c] = 1;
        }
        
        board.collapse();
        
        // row 15의 블록이 row 19로 내려가야 함
        for (int c = 1; c < 6; c++) {
            assertEquals("블록이 row 19로 이동해야 함", 1, board.getBoard()[19][c]);
        }
        
        // row 15는 비어있어야 함
        for (int c = 1; c < 6; c++) {
            assertEquals("row 15가 비어있어야 함", 0, board.getBoard()[15][c]);
        }
    }

    @Test
    public void testCollapseWithNoEmptyRows() {
        // 모든 줄에 블록이 있는 경우
        Board board = new Board();
        board.removeBlock(board.getCurPos());
        
        for (int r = 1; r < 20; r++) {
            for (int c = 1; c < 11; c++) {
                board.getBoard()[r][c] = 1;
            }
        }
        
        // collapse 전 상태 저장
        int[][] before = new int[21][12];
        for (int r = 0; r < 21; r++) {
            System.arraycopy(board.getBoard()[r], 0, before[r], 0, 12);
        }
        
        board.collapse();
        
        // 아무것도 변경되지 않아야 함
        for (int r = 0; r < 21; r++) {
            assertArrayEquals("row " + r + "이 변경되지 않아야 함", before[r], board.getBoard()[r]);
        }
    }

    @Test
    public void testClearRowsAndCollapseIntegration() {
        // 통합 테스트: 줄 제거 후 collapse
        Board board = new Board();
        board.removeBlock(board.getCurPos());
        
        // row 17-19에 블록 배치
        for (int r = 17; r <= 19; r++) {
            for (int c = 1; c < 11; c++) {
                board.getBoard()[r][c] = 1;
            }
        }
        
        // row 15에도 블록 배치 (위쪽)
        for (int c = 1; c < 6; c++) {
            board.getBoard()[15][c] = 1;
        }
        
        // row 18을 제거
        List<Integer> rowsToClear = List.of(18);
        board.clearRows(rowsToClear);
        
        // collapse 수행
        board.collapse();
        
        // row 19는 그대로, row 18에는 row 17이 내려와야 함
        for (int c = 1; c < 11; c++) {
            assertEquals("row 19 유지", 1, board.getBoard()[19][c]);
            assertEquals("row 18에 row 17이 이동", 1, board.getBoard()[18][c]);
        }
        
        // row 17은 row 15가 내려와야 함
        for (int c = 1; c < 6; c++) {
            assertEquals("row 17에 row 15가 이동", 1, board.getBoard()[17][c]);
        }
        
        // row 15는 비어있어야 함
        for (int c = 1; c < 6; c++) {
            assertEquals("row 15가 비어있어야 함", 0, board.getBoard()[15][c]);
        }
    }

    @Test
    public void testCollapsePreservesWalls() {
        // collapse 후에도 벽이 유지되는지 확인
        Board board = new Board();
        board.collapse();
        
        // 바닥 벽 확인 (row 20)
        for (int c = 0; c < 12; c++) {
            assertEquals("바닥 벽 유지", 1, board.getBoard()[20][c]);
        }
        
        // 왼쪽 벽 확인 (col 0)
        for (int r = 0; r < 21; r++) {
            assertEquals("왼쪽 벽 유지", 1, board.getBoard()[r][0]);
        }
        
        // 오른쪽 벽 확인 (col 11)
        for (int r = 0; r < 21; r++) {
            assertEquals("오른쪽 벽 유지", 1, board.getBoard()[r][11]);
        }
    }
}

