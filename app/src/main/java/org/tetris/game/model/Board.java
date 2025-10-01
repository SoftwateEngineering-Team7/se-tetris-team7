package org.tetris.game.model;

import org.extension.Point;

public class Board {
    private final int HEIGHT = 20;
    private final int WIDTH = 10;

    private int[][] board;

    public Board() {
        board = new int[HEIGHT][WIDTH];
    }

    public void placeBlock(Block block, Point pos) {
        int blockHeight = block.height();
        int blockWidth = block.width();

        for (int r = 0; r < blockHeight; r++) {
            for (int c = 0; c < blockWidth; c++) {
                if (block.getShape(r, c) == 1) {
                    int row = pos.r - block.pivot.r + r;
                    int col = pos.c - block.pivot.c + c;

                    board[row][col] = 1;
                }
            }
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