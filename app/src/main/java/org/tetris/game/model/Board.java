package org.tetris.game.model;

import java.util.Random;
import org.tetris.game.model.blocks.*;

public class Board {
    private final int HEIGHT = 21;
    private final int WIDTH = 12;

    private int[][] board;
    public Block activeBlock;
    private Point curPos;
    private Point initialPos;

    public Board() {
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

    public boolean placeBlock(Point pos) {
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
        return true;
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

    public boolean isValidPos(Point pos) {
        for (int r = 0; r < activeBlock.height(); r++) {
            for (int c = 0; c < activeBlock.width(); c++) {
                if (activeBlock.getShape(r, c) == 1) {
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

    public boolean isInBound(int row, int col) {
        return row >= 0 && row < HEIGHT && col >= 0 && col < WIDTH;
    }

    public void setActiveToStaticBlock() {
        activeBlock = getRandomBlock();
        curPos = new Point(initialPos);
    }

    public boolean moveDown() {
        boolean result = false;
        Point downPos = curPos.down();
        removeBlock(curPos);

        if (isValidPos(downPos)) {
            curPos = downPos;
            result = true;
        }

        placeBlock(curPos);
        return result;
    }

    public void moveRight() {
        Point rightPos = curPos.right();
        removeBlock(curPos);

        if (isValidPos(rightPos)) {
            curPos = rightPos;
        }

        placeBlock(curPos);
    }

    public void moveLeft() {
        Point leftPos = curPos.left();
        removeBlock(curPos);

        if (isValidPos(leftPos)) {
            curPos = leftPos;
        }

        placeBlock(curPos);
    }

    public void autoDown() {
        boolean isDown = moveDown();
        if (!isDown)
            setActiveToStaticBlock();
    }

    public void rotate() {
        removeBlock(curPos);
        activeBlock.rotateCW();

        if (isValidPos(curPos)) {
            placeBlock(curPos);
        } else {
            activeBlock.rotateCCW();
            placeBlock(curPos);
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