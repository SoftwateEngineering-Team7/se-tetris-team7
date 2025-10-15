package org.tetris.game.model;

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

    public void hardDrop() {
        while (isValidPos(curPos.down()))
            curPos = curPos.down();
        placeBlock(curPos);
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