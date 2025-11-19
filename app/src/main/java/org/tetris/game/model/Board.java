package org.tetris.game.model;

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
    private Point curPos;
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

    public Point getSize() {
        return new Point(height, width);
    }

    // 블럭 배치
    public void placeBlock(Point pos, Block block) {
        for (int r = 0; r < block.height(); r++) {
            for (int c = 0; c < block.width(); c++) {
                if (block.getCell(r, c) != 0) {
                    int row = pos.r - block.pivot.r + r;
                    int col = pos.c - block.pivot.c + c;

                    if (isInBound(row, col))
                        board[row][col] = block.getCell(r, c);
                }
            }
        }
    }

    public void removeBlock(Point pos, Block block) {
        for (int r = 0; r < block.height(); r++) {
            for (int c = 0; c < block.width(); c++) {
                if (block.getCell(r, c) != 0) {
                    int row = pos.r - block.pivot.r + r;
                    int col = pos.c - block.pivot.c + c;

                    if (isInBound(row, col))
                        board[row][col] = 0;
                }
            }
        }
    }

    // 블럭이 해당 위치에 배치 가능한지 확인
    public boolean isValidPos(Point pos, Block activeBlock) {
        for (int r = 0; r < activeBlock.height(); r++) {
            for (int c = 0; c < activeBlock.width(); c++) {
                if (activeBlock.getCell(r, c) == 0)
                    continue;

                int row = pos.r + (r - activeBlock.pivot.r); // pos == pivot 좌표 전제
                int col = pos.c + (c - activeBlock.pivot.c);

                // 1) 좌우 경계는 항상 강제
                if (col < 0 || col >= width)
                    return false;

                // 2) 위쪽은 스킵, 아래쪽은 차단
                if (row < 0)
                    continue;
                    
                if (row >= height)
                    return false;

                // 3) 충돌
                if (board[row][col] != 0)
                    return false;
            }
        }
        return true;
    }

    public boolean isInBound(Point pos, Block activeBlock) {
        for (int r = 0; r < activeBlock.height(); r++) {
            for (int c = 0; c < activeBlock.width(); c++) {
                if (activeBlock.getCell(r, c) == 0)
                    continue;

                int row = pos.r + (r - activeBlock.pivot.r); // pos == pivot 좌표 전제
                int col = pos.c + (c - activeBlock.pivot.c);

                // 1) 좌우 경계는 항상 강제
                if (col < 0 || col >= width)
                    return false;

                // 2) 위쪽은 스킵, 아래쪽은 차단
                if (row < 0)
                    continue;
                if (row >= height)
                    return false;
            }
        }
        return true;
    }

    // 보드 크기 에 있는지 확인
    public boolean isInBound(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
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

    // -------------------- 이동 관련 함수들 --------------------
    // (이동할 좌표 p'을 생성하고 기존 블럭은 제거, p'에 배치가 가능하다면 curPos에 p'을 할당, 마지막으로 curPos에 블럭 배치
    // 후 이동 여부 반환)

    // 아래로 한칸 이동 함수
    public boolean moveDown() {
        if (activeBlock.getCanMove() == false)
            return false;

        boolean isMoved = false;
        Point downPos = curPos.down();
        removeBlock(curPos, activeBlock);

        if (isValidPos(downPos, activeBlock)) {
            curPos = downPos;
            isMoved = true;
        }

        placeBlock(curPos, activeBlock);
        return isMoved;
    }

    private boolean isForceDown;

    public boolean getIsForceDown() {
        return isForceDown;
    }

    public void setIsForceDown(boolean isForceDown) {
        this.isForceDown = isForceDown;
    }

    public boolean moveDownForce() {
        if (!isForceDown)
            return false;

        isForceDown = false;
        Point downPos = curPos.down();
        removeBlock(curPos, activeBlock);

        if (isInBound(downPos, activeBlock)) {
            isForceDown = true;
            curPos = downPos;
            placeBlock(curPos, activeBlock);

            return true;

        }
        return false;
    }

    // 오른쪽 한칸 이동 함수
    public boolean moveRight() {
        if (activeBlock.getCanMove() == false)
            return false;

        boolean isMoved = false;
        Point rightPos = curPos.right();
        removeBlock(curPos, activeBlock);

        if (isValidPos(rightPos, activeBlock)) {
            curPos = rightPos;
            isMoved = true;
        }

        placeBlock(curPos, activeBlock);
        return isMoved;
    }

    // 왼쪽 한칸 이동 함수
    public boolean moveLeft() {
        if (activeBlock.getCanMove() == false)
            return false;

        boolean isMoved = false;
        Point leftPos = curPos.left();
        removeBlock(curPos, activeBlock);

        if (isValidPos(leftPos, activeBlock)) {
            curPos = leftPos;
            isMoved = true;
        }

        placeBlock(curPos, activeBlock);
        return isMoved;
    }

    public int hardDrop() {
        if (activeBlock.getCanMove() == false)
            return 0;

        removeBlock(curPos, activeBlock);
        int dropDistance = 0;
        while (isValidPos(curPos.down(), activeBlock)) {
            curPos = curPos.down();
            dropDistance++;
        }
        placeBlock(curPos, activeBlock);
        return dropDistance;
    }

    // 시계방향 90도 회전 함수
    public boolean rotate() {
        if (activeBlock.getCanRotate() == false)
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

    public java.util.List<Point> clearBomb(Point center) {
        java.util.List<Point> targets = new java.util.ArrayList<>(9);

        int r0 = center.r;
        int c0 = center.c;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int r = r0 + dr;
                int c = c0 + dc;

                if (!isInBound(r, c))
                    continue;

                // 비어있는 칸(0)은 굳이 추가하지 않으면 불필요한 작업을 줄일 수 있습니다.
                // 만약 빈칸도 플래시하고 싶다면 아래 조건을 제거하세요.
                if (board[r][c] == 0)
                    continue;

                targets.add(new Point(r, c));
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

    /**
     * 보드를 한 칸 위로 밀고, 새로운 행을 맨 아래에 추가합니다.
     * @param newRow 추가할 새로운 행
     */
    public void pushUp(int[] newRow)
    {
        for (int r = 0; r < height - 1; r++) {
            System.arraycopy(board[r + 1], 0, board[r], 0, width);
        }
        
        System.arraycopy(newRow, 0, board[height - 1], 0, width);
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
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                sb.append(board[r][c]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
