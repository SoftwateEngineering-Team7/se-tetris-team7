package org.tetris.game.model;

import java.util.List;

import org.tetris.game.model.blocks.*;
import org.util.Point;

public class Board {
    private final int HEIGHT;
    private final int WIDTH;

    private int[][] board;
    public Block activeBlock;
    private Point curPos;
    private Point initialPos;
    private NextBlockModel nextBlockModel;

    // Getter/Setter 메서드 (테스트용)
    public int[][] getBoard() {
        return board;
    }

    public Point getCurPos() {
        return curPos;
    }

    public void setCurPos(Point pos) {
        this.curPos = pos;
    }
    
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
        nextBlockModel = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 5);
        activeBlock = nextBlockModel.getBlock();

        initialPos = new Point(-1, 5);
        curPos = new Point(initialPos);
        
        placeBlock(curPos);
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
        activeBlock = nextBlockModel.getBlock();
        curPos = new Point(initialPos);
    }

    // -------------------- 줄 삭제 관련 함수들 --------------------

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
        return fullRows;
    }

    public void clearRows(List<Integer> rows) {
        if (rows.isEmpty()) {
            return;
        }

        // 각 가득 찬 줄을 제거
        for (int fullRow : rows) {
            for (int c = 1; c < WIDTH - 1; c++) {
                board[fullRow][c] = 0;
            }
        }
    }

    public void collapse() {
        // 아래에서부터 위로 올라가면서 처리
        int r = HEIGHT - 2; // HEIGHT-1은 바닥이므로 HEIGHT-2부터 시작
        while (r > 0) {
            // 현재 줄이 비어있는지 확인
            boolean isEmpty = true;
            for (int c = 1; c < WIDTH - 1; c++) {
                if (board[r][c] != 0) {
                    isEmpty = false;
                    break;
                }
            }

            if (isEmpty) {
                // 위에 블록이 있는지 확인
                boolean hasBlockAbove = false;
                for (int i = r - 1; i >= 0; i--) {
                    for (int c = 1; c < WIDTH - 1; c++) {
                        if (board[i][c] != 0) {
                            hasBlockAbove = true;
                            break;
                        }
                    }
                    if (hasBlockAbove) break;
                }

                // 위에 블록이 있으면 아래로 이동
                if (hasBlockAbove) {
                    for (int i = r; i > 0; i--) {
                        for (int c = 1; c < WIDTH - 1; c++) {
                            board[i][c] = board[i - 1][c];
                        }
                    }
                    // 맨 위 줄은 비움 (벽 제외)
                    for (int c = 1; c < WIDTH - 1; c++) {
                        board[0][c] = 0;
                    }
                    // 같은 위치를 다시 검사 (위에서 내려온 줄도 비어있을 수 있음)
                } else {
                    // 위에 블록이 없으면 다음 줄로
                    r--;
                }
            } else {
                // 비어있지 않으면 다음 위치로
                r--;
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

    public void printBoard() {
        for (int r = 0; r < HEIGHT; r++) {
            for (int c = 0; c < WIDTH; c++) {
                System.out.print(board[r][c] + " ");
            }
            System.out.println();
        }
    }
}