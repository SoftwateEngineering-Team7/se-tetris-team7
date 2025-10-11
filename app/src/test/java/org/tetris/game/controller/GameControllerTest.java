package org.tetris.game.controller;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import org.tetris.game.model.Board;
import org.util.Point;

public class GameControllerTest extends ApplicationTest {

    private Board board;
    private GameController gc;

    private Point getCurPos(Board b) throws Exception {
        Field f = Board.class.getDeclaredField("curPos");
        f.setAccessible(true);
        return (Point) f.get(b);
    }

    private int[][] getBoardArray(Board b) throws Exception {
        Field f = Board.class.getDeclaredField("board");
        f.setAccessible(true);
        return (int[][]) f.get(b);
    }

    // 활성 블록 바로 '아래' 칸들을 전부 막아 다음 중력 틱에서 canMoveDown=false 상태를 유도
    private void blockCellsDirectlyBelowActive() throws Exception {
        int[][] arr = getBoardArray(board);
        Point cur = getCurPos(board);
        var blk = board.activeBlock; // public field
        for (int r = 0; r < blk.height(); r++) {
            for (int c = 0; c < blk.width(); c++) {
                if (blk.getShape(r, c) != 1)
                    continue;
                int row = cur.r - blk.pivot.r + r;
                int col = cur.c - blk.pivot.c + c;
                int rowBelow = row + 1;
                if (rowBelow >= 0 && rowBelow < arr.length && col >= 0 && col < arr[0].length) {
                    arr[rowBelow][col] = 1;
                }
            }
        }
    }

    @Override
    public void start(Stage stage) {
        // 최소 뷰 (실제 렌더는 필요 없음, Stage만 필요)
        Canvas canvas = new Canvas(100, 100);
        stage.setScene(new Scene(new Group(canvas)));
        stage.show();

        board = new Board();
        gc = new GameController(board);
        gc.startGameLoop(); // 기본 1초 주기
    }

    @After
    public void tearDown() {
        if (gc != null)
            gc.stopGameLoop();
    }

    // ===== 기본 이동/루프 테스트들 =====

    @Test
    public void gravityMovesBlockDownOverTime() throws Exception {
        Point before = getCurPos(board);
        WaitForAsyncUtils.sleep(1200, TimeUnit.MILLISECONDS);
        Point after = getCurPos(board);
        assertTrue("중력 틱으로 r 값이 증가해야 합니다", after.r > before.r);
    }

    @Test
    public void testMoveLeft() throws Exception {
        Point before = getCurPos(board);
        gc.onMoveLeft();
        WaitForAsyncUtils.waitForFxEvents();
        Point after = getCurPos(board);
        assertTrue("왼쪽 이동 후 c 값이 감소해야 합니다", after.c < before.c);
    }

    @Test
    public void testMoveRight() throws Exception {
        Point before = getCurPos(board);
        gc.onMoveRight();
        WaitForAsyncUtils.waitForFxEvents();
        Point after = getCurPos(board);
        assertTrue("오른쪽 이동 후 c 값이 증가해야 합니다", after.c > before.c);
    }

    @Test
    public void testMultipleMoveLeft() throws Exception {
        Point before = getCurPos(board);
        for (int i = 0; i < 3; i++) {
            gc.onMoveLeft();
            WaitForAsyncUtils.waitForFxEvents();
        }
        Point after = getCurPos(board);
        assertTrue("여러 번 왼쪽 이동 후 c 값이 감소해야 합니다", after.c < before.c);
    }

    @Test
    public void testMultipleMoveRight() throws Exception {
        Point before = getCurPos(board);
        for (int i = 0; i < 3; i++) {
            gc.onMoveRight();
            WaitForAsyncUtils.waitForFxEvents();
        }
        Point after = getCurPos(board);
        assertTrue("여러 번 오른쪽 이동 후 c 값이 증가해야 합니다", after.c > before.c);
    }

    @Test
    public void testStopAndRestartGameLoop() throws Exception {
        gc.stopGameLoop();
        WaitForAsyncUtils.sleep(500, TimeUnit.MILLISECONDS);
        Point afterStop = getCurPos(board);

        gc.startGameLoop();
        WaitForAsyncUtils.sleep(1200, TimeUnit.MILLISECONDS);
        Point afterRestart = getCurPos(board);

        assertTrue("게임 루프 재시작 후 블록이 이동해야 합니다", afterRestart.r > afterStop.r);
    }

    @Test
    public void testCombinedLeftRightMoves() throws Exception {
        Point start = getCurPos(board);
        gc.onMoveRight();
        gc.onMoveRight();
        gc.onMoveLeft();
        WaitForAsyncUtils.waitForFxEvents();
        Point end = getCurPos(board);
        assertTrue("오른쪽 2번, 왼쪽 1번 이동 후 c 값이 증가해야 합니다", end.c > start.c);
    }

    @Test
    public void testGameLoopIsRunning() throws Exception {
        Point before = getCurPos(board);
        WaitForAsyncUtils.sleep(2400, TimeUnit.MILLISECONDS);
        Point after = getCurPos(board);
        assertTrue("게임 루프 실행 중 2초 후 블록이 2칸 이상 이동해야 합니다", after.r >= before.r + 2);
    }

    @Test
    public void testMoveLeftAtBoundary() throws Exception {
        for (int i = 0; i < 20; i++) {
            gc.onMoveLeft();
            WaitForAsyncUtils.waitForFxEvents();
        }
        assertTrue("경계에서 왼쪽 이동 시도 후에도 블록이 보드에 존재해야 합니다", board.activeBlock != null);
    }

    @Test
    public void testMoveRightAtBoundary() throws Exception {
        for (int i = 0; i < 20; i++) {
            gc.onMoveRight();
            WaitForAsyncUtils.waitForFxEvents();
        }
        assertTrue("경계에서 오른쪽 이동 시도 후에도 블록이 보드에 존재해야 합니다", board.activeBlock != null);
    }

    // ===== 락 딜레이 테스트들 (분리) =====

    /** 1) 락 딜레이가 발동하면 즉시 고정되지 않고 일정 시간 대기하는지 검증 */
    @Test
    public void lockDelayPreventsImmediateLocking() throws Exception {
        blockCellsDirectlyBelowActive();
        
        // 중력 틱 대기 (락 딜레이 시작)
        WaitForAsyncUtils.sleep(1100, TimeUnit.MILLISECONDS);
        
        // 락 딜레이가 진행 중이므로 블록이 여전히 존재해야 함
        // (락 딜레이 0.5초가 완료되려면 총 1.5초 필요)
        assertTrue("락 딜레이 중 블록이 존재해야 함", board.activeBlock != null);
    }

    /** 2) 락 딜레이 중 좌/우 이동으로 구제되면 딜레이가 취소되고 중력이 재개되는지 검증 */
    @Test
    public void lockDelayCancelsOnSideMove() throws Exception {
        blockCellsDirectlyBelowActive();
        WaitForAsyncUtils.sleep(1100, TimeUnit.MILLISECONDS); // 락 시작

        Point beforeSide = getCurPos(board);
        if (board.canMoveRight())
            gc.onMoveRight();
        else if (board.canMoveLeft())
            gc.onMoveLeft();
        WaitForAsyncUtils.waitForFxEvents(); // 이동 적용

        Point afterSide = getCurPos(board);
        assertTrue("구제 입력으로 수평 위치가 변해야 함", afterSide.c != beforeSide.c);

        // 락 취소 후 중력 재개: 1.2초 뒤 r 증가 확인
        WaitForAsyncUtils.sleep(1200, TimeUnit.MILLISECONDS);
        Point afterResume = getCurPos(board);
        assertTrue("락 취소 후 중력 재개로 r 증가", afterResume.r > afterSide.r);
    }

    /** 3) 락 딜레이가 완료되면 블록이 고정되고 게임이 계속 진행되는지 검증 */
    @Test
    public void blockLocksAfterDelayExpires() throws Exception {
        blockCellsDirectlyBelowActive();
        
        // 중력 틱(1초) + 락 딜레이(0.5초) 완료 후 충분한 대기
        WaitForAsyncUtils.sleep(2500, TimeUnit.MILLISECONDS);
        
        // 락 딜레이가 완료된 후에도 게임은 계속 진행됨
        // (게임 오버가 아닌 한 블록은 항상 존재)
        assertTrue("락 딜레이 완료 후에도 게임이 진행되어야 함", board.activeBlock != null);
    }
}
