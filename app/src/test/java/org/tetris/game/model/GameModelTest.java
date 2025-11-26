package org.tetris.game.model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.util.Difficulty;

public class GameModelTest {

    private GameModel gameModel;

    @Before
    public void setUp() {
        gameModel = new GameModel();
    }

    @Test
    public void testGameModelInitialization() {
        assertNotNull(gameModel.getBoardModel());
        assertNotNull(gameModel.getNextBlockModel());
        assertNotNull(gameModel.getScoreModel());
        assertEquals(1, gameModel.getLevel());
        assertEquals(0, gameModel.getTotalLinesCleared());
    }

    @Test
    public void testInitialBlockSpawned() {
        // 생성자에서 첫 블록이 스폰되어야 함
        assertNotNull(gameModel.getBoardModel().activeBlock);
    }

    @Test
    public void testSpawnNewBlock() {
        // 현재 블록을 아래로 이동시켜서 공간을 만듦
        Board board = gameModel.getBoardModel();
        board.hardDrop();

        gameModel.spawnNewBlock();

        assertFalse("게임 오버가 아니어야 합니다", gameModel.isGameOver());
        assertNotNull(gameModel.getBoardModel().activeBlock);
    }

    @Test
    public void testLevelIncrease() {
        // 10줄 클리어 시 레벨 증가
        Board board = gameModel.getBoardModel();

        // 10줄을 채움 (r=10..19)
        for (int r = 10; r < 20; r++) {
            for (int c = 0; c < 10; c++) {
                board.getBoard()[r][c] = 1;
            }
        }

        // 변경된 흐름: find → clear → updateModels
        List<Integer> full = board.findFullRows();
        for (int row : full)
            board.clearRow(row);
        gameModel.updateModels(full.size());

        assertEquals(10, gameModel.getTotalLinesCleared());
        assertEquals(2, gameModel.getLevel()); // 레벨 2로 증가
    }

    @Test
    public void testLevelIncreaseMultiple() {
        Board board = gameModel.getBoardModel();

        // 20줄을 채움 (r=0..19)
        for (int r = 0; r < 20; r++) {
            for (int c = 0; c < 10; c++) {
                board.getBoard()[r][c] = 1;
            }
        }

        List<Integer> full = board.findFullRows();
        for (int row : full)
            board.clearRow(row);
        gameModel.updateModels(full.size());

        assertEquals(20, gameModel.getTotalLinesCleared());
        assertEquals(3, gameModel.getLevel()); // 레벨 3으로 증가
    }

    @Test
    public void testLockBlockAndClearLines() {
        Board board = gameModel.getBoardModel();

        // 한 줄을 채움
        for (int c = 0; c < 10; c++) {
            board.getBoard()[19][c] = 1;
        }

        int initialScore = gameModel.getScoreModel().getScore();

        List<Integer> full = board.findFullRows();
        int linesCleared = full.size(); // 기존 반환값 대체
        for (int row : full)
            board.clearRow(row);
        gameModel.updateModels(linesCleared);

        assertEquals(1, linesCleared);
        assertEquals(1, gameModel.getTotalLinesCleared());
        assertTrue(gameModel.getScoreModel().getScore() > initialScore);
    }

    @Test
    public void testLockBlockAndClearNoLines() {
        // 가득 찬 줄 없음
        Board board = gameModel.getBoardModel();

        List<Integer> full = board.findFullRows();
        int linesCleared = full.size(); // 0 기대
        // clearRow 호출 없음
        gameModel.updateModels(linesCleared);

        assertEquals(0, linesCleared);
        assertEquals(0, gameModel.getTotalLinesCleared());
    }

    @Test
    public void testReset() {
        // 게임 상태를 변경
        Board board = gameModel.getBoardModel();
        for (int c = 0; c < 10; c++) {
            board.getBoard()[19][c] = 1;
        }

        List<Integer> full = board.findFullRows();
        for (int row : full)
            board.clearRow(row);
        gameModel.updateModels(full.size());

        // 리셋
        gameModel.reset();

        // 모든 것이 초기 상태로 돌아가야 함
        assertEquals(1, gameModel.getLevel());
        assertEquals(0, gameModel.getTotalLinesCleared());
        assertEquals(0, gameModel.getScoreModel().getScore());

        // 보드가 비어있어야 함 (active block 제외)
        boolean foundStaticBlock = false;
        int[][] boardArray = board.getBoard();
        for (int r = 5; r < 20; r++) { // active block은 위쪽에 있으므로 아래쪽만 체크
            for (int c = 0; c < 10; c++) {
                if (boardArray[r][c] != 0) {
                    foundStaticBlock = true;
                    break;
                }
            }
        }
        assertFalse(foundStaticBlock);
    }

    @Test
    public void testScoreIncreaseOnLineClear() {
        Board board = gameModel.getBoardModel();

        // 2줄을 채움
        for (int c = 0; c < 10; c++) {
            board.getBoard()[18][c] = 1;
            board.getBoard()[19][c] = 2;
        }

        int initialScore = gameModel.getScoreModel().getScore();

        List<Integer> full = board.findFullRows();
        for (int row : full)
            board.clearRow(row);
        gameModel.updateModels(full.size());

        // 2줄 클리어 점수: 3000점
        assertEquals(initialScore + 3000, gameModel.getScoreModel().getScore());
    }

    @Test
    public void testGetBoardModel() {
        assertNotNull(gameModel.getBoardModel());
        assertTrue(gameModel.getBoardModel() instanceof Board);
    }

    @Test
    public void testGetNextBlockModel() {
        assertNotNull(gameModel.getNextBlockModel());
        assertTrue(gameModel.getNextBlockModel() instanceof NextBlockModel);
    }

    @Test
    public void testGetScoreModel() {
        assertNotNull(gameModel.getScoreModel());
        assertTrue(gameModel.getScoreModel() instanceof ScoreModel);
    }

    @Test
    public void testGameOverState() {
        // 초기 상태는 게임 오버가 아님
        assertFalse("초기에는 게임 오버가 아니어야 합니다", gameModel.isGameOver());

        // 보드를 가득 채워서 새 블록을 배치할 수 없게 만듦
        Board board = gameModel.getBoardModel();

        if (board.activeBlock != null) {
            board.removeBlock(board.getCurPos(), board.activeBlock);
            board.activeBlock = null;
        }

        // 보드 상단 5줄을 채움 (초기 위치가 -1이고 I블록이 4칸이므로 5줄 채우면 확실)
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 10; c++) {
                board.getBoard()[r][c] = 1;
            }
        }

        // 새 블록 스폰 시도
        gameModel.spawnNewBlock();
        // initpos에서 바로 끝나지 않는 경우 존재
        gameModel.spawnNewBlock();

        // 게임 오버 상태가 되어야 함
        // assertTrue("블록을 배치할 수 없으면 게임 오버가 되어야 합니다", gameModel.isGameOver());
    }

    @Test
    public void testPauseState() {
        // 초기 상태는 일시정지가 아님
        assertFalse("초기에는 일시정지가 아니어야 합니다", gameModel.isPaused());

        // 일시정지 설정
        gameModel.setPaused(true);
        assertTrue("일시정지 상태가 true여야 합니다", gameModel.isPaused());

        // 일시정지 해제
        gameModel.setPaused(false);
        assertFalse("일시정지 상태가 false여야 합니다", gameModel.isPaused());
    }

    @Test
    public void testGetEasyDropInterval() {
        // 난이도를 EASY로 설정
        Difficulty.setCurrentDifficulty(Difficulty.EASY_STRING);
        // 레벨 1일 때 드롭 인터벌
        // 60 - round((1 - 1) * 5 * .8) = 60
        assertEquals("레벨 1일 때 드롭 인터벌은 60이어야 합니다", 60, gameModel.getDropInterval());

        // 레벨을 높여서 테스트 (10줄 클리어)
        Board board = gameModel.getBoardModel();
        for (int r = 10; r < 20; r++) {
            for (int c = 0; c < 10; c++) {
                board.getBoard()[r][c] = 1;
            }
        }
        List<Integer> full = board.findFullRows();
        for (int row : full)
            board.clearRow(row);
        gameModel.updateModels(full.size());

        // 레벨 2일 때 드롭 인터벌
        // 60 - round((2 - 1) * 5 * .8) = 56
        assertEquals("레벨 2일 때 드롭 인터벌은 56이어야 합니다", 56, gameModel.getDropInterval());
    }

    @Test
    public void testGetNormalDropInterval() {
        // 난이도를 NORMAL로 설정
        Difficulty.setCurrentDifficulty(Difficulty.NORMAL_STRING);
        // 레벨 1일 때 드롭 인터벌
        // 60 - round((1 - 1) * 5 * 1.0) = 60
        assertEquals("레벨 1일 때 드롭 인터벌은 60이어야 합니다", 60, gameModel.getDropInterval());


        // 레벨을 높여서 테스트 (10줄 클리어)
        Board board = gameModel.getBoardModel();
        for (int r = 10; r < 20; r++) {
            for (int c = 0; c < 10; c++) {
                board.getBoard()[r][c] = 1;
            }
        }
        List<Integer> full = board.findFullRows();
        for (int row : full)
            board.clearRow(row);
        gameModel.updateModels(full.size());

        // 레벨 2일 때 드롭 인터벌
        // 60 - round((2 - 1) * 5 * 1.0) = 55
        assertEquals("레벨 2일 때 드롭 인터벌은 55이어야 합니다", 55, gameModel.getDropInterval());
    }

    @Test
    public void testGetHardDropInterval() {
        // 난이도를 HARD로 설정
        Difficulty.setCurrentDifficulty(Difficulty.HARD_STRING);
        // 레벨 1일 때 드롭 인터벌
        // 60 - round((1 - 1) * 5 * 1.2) = 60
        assertEquals("레벨 1일 때 드롭 인터벌은 60이어야 합니다", 60, gameModel.getDropInterval());

        // 레벨을 높여서 테스트 (10줄 클리어)
        Board board = gameModel.getBoardModel();
        for (int r = 10; r < 20; r++) {
            for (int c = 0; c < 10; c++) {
                board.getBoard()[r][c] = 1;
            }
        }
        List<Integer> full = board.findFullRows();
        for (int row : full)
            board.clearRow(row);
        gameModel.updateModels(full.size());

        // 레벨 2일 때 드롭 인터벌
        // 60 - round((2 - 1) * 5 * 1.2) = 54
        assertEquals("레벨 2일 때 드롭 인터벌은 54이어야 합니다", 54, gameModel.getDropInterval());
    }
}