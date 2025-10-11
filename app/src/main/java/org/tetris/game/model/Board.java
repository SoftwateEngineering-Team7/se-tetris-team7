package org.tetris.game.model;

import java.util.List;
import java.util.Random;
import org.tetris.game.model.blocks.*;
import org.util.Point;

public class Board {
    private final int HEIGHT;
    private final int WIDTH;

    private int[][] board;
    public Block activeBlock;
    private Point curPos;
    private Point initialPos;

    // Board 생성자
    public Board() {
        this(21, 12);
    }

    public Board(int height, int width) {
        HEIGHT = height;
        WIDTH = width;
        board = new int[HEIGHT][WIDTH];
        for (int r = 0; r < HEIGHT; r++) {
            for (int c = 0; c < WIDTH; c++) {
                if (r == 20 || c == 0 || c == 11)
                    board[r][c] = 1;
            }
        }
        activeBlock = getRandomBlock();
        initialPos = new Point(-1, 5);
        curPos = new Point(initialPos);
        placeBlock(curPos);
    }

    // 랜덤블럭 반환
    public Block getRandomBlock() {
        Random rnd = new Random();
        int block = rnd.nextInt(7);
        switch (block) {
            case 0:
                return new IBlock();
            case 1:
                return new JBlock();
            case 2:
                return new LBlock();
            case 3:
                return new ZBlock();
            case 4:
                return new SBlock();
            case 5:
                return new TBlock();
            case 6:
                return new OBlock();
        }
        return new LBlock();

    }

    // 블럭 배치
    public void placeBlock(Point pos) {
        for (int r = 0; r < activeBlock.height(); r++) {
            for (int c = 0; c < activeBlock.width(); c++) {
                if (activeBlock.getShape(r, c) == 1) {
                    int row = pos.r - activeBlock.pivot.r + r;
                    int col = pos.c - activeBlock.pivot.c + c;

                    if (isInBound(row, col))
                        board[row][col] = 1;
                }
            }
        }
    }

    public void removeBlock(Point pos) {
        for (int r = 0; r < activeBlock.height(); r++) {
            for (int c = 0; c < activeBlock.width(); c++) {
                if (activeBlock.getShape(r, c) == 1) {
                    int row = pos.r - activeBlock.pivot.r + r;
                    int col = pos.c - activeBlock.pivot.c + c;

                    if (isInBound(row, col))
                        board[row][col] = 0;
                }
            }
        }
    }

    // 배치 시도 후 가능 여부 반환
    public boolean isValidPos(Point pos) {
        for (int r = 0; r < activeBlock.height(); r++) {
            for (int c = 0; c < activeBlock.width(); c++) {
                if (activeBlock.getShape(r, c) != 0) {
                    int row = pos.r - activeBlock.pivot.r + r;
                    int col = pos.c - activeBlock.pivot.c + c;

                    if (isInBound(row, col) && board[row][col] != 0) {
                        System.out.println("Collision occurred.");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // 보드 크기 (22 * 11) 에 있는지 확인
    public boolean isInBound(int row, int col) {
        return row >= 0 && row < HEIGHT && col >= 0 && col < WIDTH;
    }

    // 기존 활성 블럭은 고정되고 활성 블럭에 새로운 랜덤블럭 반환
    public void setActiveToStaticBlock() {
        activeBlock = getRandomBlock();
        curPos = new Point(initialPos);
    }
    
    // 새로운 블럭을 생성하고 배치, 배치 불가시 false 반환 (게임오버)
    public boolean spawnNextBlock() {
        activeBlock = getRandomBlock();
        curPos = new Point(initialPos);
        if (!isValidPos(curPos))
            return false; // 게임오버
        placeBlock(curPos);
        return true;
    }

    // 전제 활성 블록이 null이 아니여야 하고, curPos도 활성 블록 놓인 위치가 유지되어야 함
    // 현재 활성 블록이 놓인 위치에서 가득 찬 줄들을 찾아서 반환
    // 블록의 shape가 걸쳐있는 row들만 검사
    public List<Integer> findFullRows() {
        List<Integer> fullRows = new java.util.ArrayList<>();

        // 현재 블록이 차지하는 row 범위 계산
        int minRow = curPos.r - activeBlock.pivot.r;
        int maxRow = minRow + activeBlock.height() - 1;
        
        // 블록이 걸쳐있는 row들을 검사
        for (int r = minRow; r <= maxRow; r++) {
            // 범위를 벗어나거나 바닥(row 20)이면 스킵
            if (r < 0 || r >= HEIGHT - 1) {
                continue;
            }
            
            // 해당 row가 가득 찼는지 검사
            boolean isFull = true;
            for (int c = 1; c < WIDTH - 1; c++) {
                if (board[r][c] == 0) {
                    isFull = false;
                    break;
                }
            }
            
            if (isFull) {
                fullRows.add(r);
            }
        }
        
        // 아이템의 경우 L이 포함된 경우를 추가하면 됨.
        // 중복 제거 -> 아래는 아이템의 경우를 위해서 추가된 함수.
        java.util.Set<Integer> uniqueRows = new java.util.HashSet<>(fullRows);
        fullRows = new java.util.ArrayList<>(uniqueRows);

        return fullRows;
    }

    // 가득 찬 줄들을 제거하고 위의 블록들을 아래로 내림
    public void clearRowsAndCollapse(List<Integer> rows) {
        if (rows.isEmpty()) {
            return;
        }
        
        // 각 가득 찬 줄을 제거하고 위의 줄들을 아래로 이동
        for (int fullRow : rows) {
            // fullRow부터 위로 올라가면서 한 칸씩 아래로 복사
            for (int r = fullRow; r > 0; r--) {
                for (int c = 1; c < WIDTH - 1; c++) {
                    board[r][c] = board[r - 1][c];
                }
            }
            
            // 맨 위 줄(row 0)은 비움 (벽 제외)
            for (int c = 1; c < WIDTH - 1; c++) {
                board[0][c] = 0;
            }
        }
    }

    // -------------------- 이동 관련 함수들 --------------------
    // (이동할 좌표 p'을 생성하고 기존 블럭은 제거, p'에 배치가 가능하다면 curPos에 p'을 할당, 마지막으로 curPos에 블럭 배치
    // 후 이동 여부 반환)

    // 아래로 한칸 이동 함수
    public boolean moveDown() {
        boolean isMoved = false;
        Point downPos = curPos.down();
        removeBlock(curPos);

        if (isValidPos(downPos)) {
            curPos = downPos;
            isMoved = true;
        }

        placeBlock(curPos);
        return isMoved;
    }

    // 아래로 한칸 이동 함수
    // 반드시 canMoveDown()이 true일 때만 호출되어야 함
    public void moveToDown() {
        Point downPos = curPos.down();
        removeBlock(curPos);
        curPos = downPos;
        placeBlock(curPos);
    }

    // 아래 이동 가능한지만 검사하는 함수
    public boolean canMoveDown() {
        removeBlock(curPos);
        Point downPos = curPos.down();
        boolean isMovable = isValidPos(downPos); // 아래 위치가 유효한지 검사
        placeBlock(curPos);
        return isMovable;
    }

    // 오른쪽 한칸 이동 함수
    public boolean moveRight() {
        boolean isMoved = false;
        Point rightPos = curPos.right();
        removeBlock(curPos);

        if (isValidPos(rightPos)) {
            curPos = rightPos;
            isMoved = true;
        }

        placeBlock(curPos);
        return isMoved;
    }

    // 오른쪽 한칸 이동 함수
    // 반드시 canMoveRight()이 true일 때만 호출되어야 함
    public void moveToRight() {
        Point rightPos = curPos.right();
        removeBlock(curPos);
        curPos = rightPos;
        placeBlock(curPos);
    }

    // 오른쪽 이동 가능한지만 검사하는 함수
    public boolean canMoveRight() {
        removeBlock(curPos);
        Point rightPos = curPos.right();
        boolean isMovable = isValidPos(rightPos); // 오른쪽 위치가 유효한지 검사
        placeBlock(curPos);
        return isMovable;
    }

    // 왼쪽 한칸 이동 함수
    public boolean moveLeft() {
        boolean isMoved = false;
        Point leftPos = curPos.left();
        removeBlock(curPos);

        if (isValidPos(leftPos)) {
            curPos = leftPos;
            isMoved = true;
        }

        placeBlock(curPos);
        return isMoved;
    }

    // 왼쪽 한칸 이동 함수
    public void moveToLeft() {
        Point leftPos = curPos.left();
        removeBlock(curPos);
        curPos = leftPos;
        placeBlock(curPos);
    }

    // 왼쪽 이동 가능한지만 검사하는 함수
    public boolean canMoveLeft() {
        removeBlock(curPos);
        Point leftPos = curPos.left();
        boolean isMovable = isValidPos(leftPos); // 왼쪽 위치가 유효한지 검사
        placeBlock(curPos);
        return isMovable;
    }

    // 매 임의의 시간마다 아래로 한칸 이동하는 함수 (이동이 불가하면 활성 블럭은 고정되고 새로운 블럭으로 반환)
    public void autoDown() {
        boolean isDown = moveDown();
        if (!isDown)
            setActiveToStaticBlock();
    }

    // 시계방향 90도 회전 함수
    public boolean rotate() {
        boolean isMoved = false;
        removeBlock(curPos);
        activeBlock.rotateCW();

        if (isValidPos(curPos)) {
            isMoved = true;
        } else {
            activeBlock.rotateCCW();
        }

        placeBlock(curPos);
        return isMoved;
    }

    public void rotateToClockwise(boolean isClockwise) {
        removeBlock(curPos);
        if(isClockwise)
            activeBlock.rotateCW();
        else
            activeBlock.rotateCCW();

        placeBlock(curPos);
    }

    // rotate가 가능한지 검사
    public boolean canRotate(boolean isClockwise) {
        removeBlock(curPos);
        if(isClockwise) {
            activeBlock.rotateCW();
            boolean isValid = isValidPos(curPos);
            activeBlock.rotateCCW();
            placeBlock(curPos);
            return isValid;
            } else {
                activeBlock.rotateCCW();
                boolean isValid = isValidPos(curPos);
                activeBlock.rotateCW();
                placeBlock(curPos);
                return isValid;
            }
    }



    public void printBoard() {
        for (int r = 0; r < HEIGHT; r++) {
            for (int c = 0; c < WIDTH; c++) {
                System.out.print(board[r][c] + " ");
            }
            System.out.println();
        }
    }
}