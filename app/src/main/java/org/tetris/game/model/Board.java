package org.tetris.game.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tetris.game.model.blocks.*;
import org.tetris.shared.BaseModel;
import org.util.Point;

public class Board extends BaseModel {
    private final int height;
    private final int width;

    private int[][] board;
    public Block activeBlock;
    public Point curPos;
    private Point initialPos;

    // Board 생성자
    public Board() {
        this(20, 10);
    }

    public Board(int h, int w) {
        this.height = h;
        this.width = w;

        board = new int[height][width];
        activeBlock = null;

        initialPos = new Point(0, width / 2);
        curPos = new Point(initialPos);
    }

    public int[][] getBoard() {
        return board;
    }

    /**
     * 보드 상태를 직접 설정 (P2P 동기화용)
     * 기존 보드를 덮어쓰고 activeBlock은 null로 설정
     */
    public void setBoard(int[][] newBoard) {
        for (int r = 0; r < height; r++) {
            if (newBoard[r] != null && newBoard[r].length == width) {
                System.arraycopy(newBoard[r], 0, board[r], 0, width);
            }
        }
    }

    public Point getSize() {
        return new Point(height, width);
    }

    // 블럭 배치
    public void placeBlock(Point pos, Block block) {
        for (Point p : block.getBlockPoints()) {
            Point blockPoint = pos.add(block.toPivot(p));
            if (isInBound(blockPoint))
                board[blockPoint.r][blockPoint.c] = block.getCell(p);
        }
    }

    public void removeBlock(Point pos, Block block) {
        for (Point p : block.getBlockPoints()) {
            Point blockPoint = pos.add(block.toPivot(p));
            if (isInBound(blockPoint))
                board[blockPoint.r][blockPoint.c] = 0;
        }
    }

    public void removeCurrentBlock() {
        removeBlock(curPos, activeBlock);
    }

    // 블럭이 해당 위치에 배치 가능한지 확인
    public boolean isValidPos(Point pos, Block block, boolean force) {
        for (Point bp : block.getBlockPoints()) {
            Point blockPoint = pos.add(block.toPivot(bp));

            if (isOutBound(blockPoint))
                return false;

            if (blockPoint.r < 0)
                continue;

            if (!force && hasBlock(blockPoint))
                return false;
        }
        return true;
    }

    public boolean isValidPos(Point pos, Block block) {
        return isValidPos(pos, block, false);
    }

    private boolean isInBound(Point pos) {
        return pos.r >= 0 && pos.r < height && pos.c >= 0 && pos.c < width;
    }

    private boolean isOutBound(Point pos) {
        return pos.c < 0 || pos.c >= width || pos.r >= height;
    }

    private boolean hasBlock(Point pos) {
        return board[pos.r][pos.c] != 0;
    }

    // 새로운 블럭을 활성 블럭으로 설정 (배치 가능 여부 반환)
    public boolean setActiveBlock(Block block) {
        activeBlock = block;
        curPos = new Point(initialPos);

        // 초기 위치에 배치 가능한지 확인
        if (!isValidPos(curPos, activeBlock)) {
            return false; // 게임 오버
        }

        placeBlock(curPos, activeBlock);
        return true;
    }

    // 현재 블럭 위치 반환
    public Point getCurPos() {
        return curPos;
    }

    public boolean getIsForceDown() {
        return activeBlock.isForceDown();
    }

    // -------------------- 이동 관련 함수들 --------------------
    // (이동할 좌표 p'을 생성하고 기존 블럭은 제거, p'에 배치가 가능하다면 curPos에 p'을 할당, 마지막으로 curPos에 블럭 배치
    // 후 이동 여부 반환)

    private boolean tryMove(Point newPos, boolean force) {
        removeBlock(curPos, activeBlock);

        if (isValidPos(newPos, activeBlock, force)) {
            curPos = newPos;
            placeBlock(curPos, activeBlock);
            return true;
        }

        placeBlock(curPos, activeBlock);
        return false;
    }

    // 아래로 한칸 이동 함수
    public boolean moveDown() {

        return tryMove(curPos.down(), false);
    }

    public boolean moveDown(boolean force) {

        return tryMove(curPos.down(), force);
    }

    public boolean autoDown() {
        return moveDown(activeBlock.isForceDown());
    }

    // 오른쪽 한칸 이동 함수
    public boolean moveRight() {
        if (!activeBlock.getCanMove())
            return false;

        return tryMove(curPos.right(), false);
    }

    // 왼쪽 한칸 이동 함수
    public boolean moveLeft() {
        if (!activeBlock.getCanMove())
            return false;

        return tryMove(curPos.left(), false);
    }

    public int hardDrop() {

        int dropDistance = 0;
        while (tryMove(curPos.down(), getIsForceDown())) {
            dropDistance++;
        }

        // placeBlock(curPos, activeBlock); // 중복 호출 제거됨
        return dropDistance;
    }

    // 시계방향 90도 회전 함수
    public boolean rotate() {
        if (!activeBlock.getCanRotate())
            return false;

        boolean isMoved = false;
        removeBlock(curPos, activeBlock);

        activeBlock.rotateCW();

        if (isValidPos(curPos, activeBlock)) {
            isMoved = true;
        } else {
            activeBlock.rotateCCW();
        }

        placeBlock(curPos, activeBlock);
        return isMoved;
    }

    public List<Integer> findFullRows() {
        List<Integer> fullRows = new java.util.ArrayList<>();

        for (int r = 0; r < height; r++) {
            boolean isFull = true;
            for (int c = 0; c < width; c++) {
                if (board[r][c] == 0) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                fullRows.add(r);
            }
        }

        return fullRows;
    }

    public void clearColumn(int index) {
        for (int r = 0; r < height; r++) {
            board[r][index] = 0;
        }
    }

    public void clearRow(int index) {
        for (int c = 0; c < width; c++) {
            board[index][c] = 0;
        }
    }

    public List<Point> clearBomb(Point center) {
        List<Point> targets = new ArrayList<>(9);

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {

                Point dp = new Point(dr, dc).add(center);
                if (!isInBound(dp))
                    continue;

                // 비어있는 칸(0)은 굳이 추가하지 않으면 불필요한 작업을 줄일 수 있습니다.
                // 만약 빈칸도 플래시하고 싶다면 아래 조건을 제거하세요.
                if (board[dp.r][dp.c] == 0)
                    continue;

                targets.add(dp);
            }
        }

        return targets;
    }

    private boolean isRowEmpty(int r) {
        for (int c = 0; c < width; c++) {
            if (board[r][c] != 0)
                return false;
        }
        return true;
    }

    // 보드 한번에 압축
    public void collapse() {
        int write = height - 1; // 내려앉힐 위치(아래에서 위로)
        for (int read = height - 1; read >= 0; read--) { // 위에서 가져올 행 스캐닝
            if (!isRowEmpty(read)) {
                if (write != read) {
                    System.arraycopy(board[read], 0, board[write], 0, width);
                }
                write--;
            }
        }
        // 남은 상단 구간을 전부 0으로
        for (int r = write; r >= 0; r--) {
            Arrays.fill(board[r], 0);
        }
    }

    public boolean pushUp(int[] newRow) {
        boolean validPushUp = true;
        // 1. 맨 윗줄 검사 (이미 블럭을 지웠으므로 순수 장애물만 검사됨)
        if (!isRowEmpty(0)) {
            // 공간 부족 시, 지웠던 블럭을 다시 그려주고 게임오버 처리할 수도 있지만,
            // 어차피 게임오버이므로 false 리턴
            validPushUp = false;
        }

        // 2. 전체 보드 shift
        for (int r = 0; r < height - 1; r++) {
            System.arraycopy(board[r + 1], 0, board[r], 0, width);
        }

        // 3. 새 줄 추가
        System.arraycopy(newRow, 0, board[height - 1], 0, width);

        return validPushUp;
    }
    /**
     * 특정 행(rowIndex)의 데이터를 가져와 공격용 라인을 생성합니다.
     * 1. 블록이 채워져 있던 칸은 모두 회색(8)으로 변환합니다. [cite: 54]
     * 2. 현재 활성 블럭(activeBlock)이 있던 위치는 빈칸(0)으로 구멍을 뚫습니다. [cite: 23]
     */
    public int[] getRowForAttack(int rowIndex) {
        int[] originalRow = board[rowIndex];
        int[] attackRow = new int[width];

        // 1. 해당 줄의 데이터를 복사하되, 블럭이 있는 곳은 회색(8)으로 변환
        for (int c = 0; c < width; c++) {
            if (originalRow[c] != 0) {
                attackRow[c] = 8; // 8 = Garbage Block Color (Gray)
            } else {
                attackRow[c] = 0; // 원래 빈 칸은 그대로 빈 칸
            }
        }

        // 2. 현재 활성 블럭(이번에 줄을 지운 블럭)이 위치한 곳은 0으로 구멍 뚫기
        if (activeBlock != null) {
            for (Point p : activeBlock.getBlockPoints()) {
                // 블럭의 로컬 좌표를 보드 전체 좌표로 변환
                Point globalP = curPos.add(activeBlock.toPivot(p));

                // 해당 블럭 조각이 지금 처리 중인 행(rowIndex)에 있다면 구멍(0) 처리
                if (globalP.r == rowIndex && isInBound(globalP)) {
                    attackRow[globalP.c] = 0;
                }
            }
        }
        return attackRow;
    }

    /**
     * 보드를 초기 상태로 재설정합니다.
     */
    public void reset() {
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                board[r][c] = 0;
            }
        }
        activeBlock = null;
        curPos = new Point(initialPos);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int[][] boardCopy = getBoard();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                sb.append(boardCopy[r][c]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
