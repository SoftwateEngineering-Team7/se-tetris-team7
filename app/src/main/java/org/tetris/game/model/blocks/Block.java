package org.tetris.game.model.blocks;

import org.tetris.game.model.Point;

public abstract class Block {
    protected int[][] shape;
    public Point pivot;

    public Block() {
        this.shape = new int[][] {
                { 1, 1 },
                { 1, 1 }
        };
        this.pivot = new Point(0, 0);
    }

    public int getShape(int r, int c) {
        return shape[r][c];
    }

    public int height() {
        return shape.length;
    }

    public int width() {
        if (shape.length > 0)
            return shape[0].length;
        return 0;
    }

    public void rotateCW() {
        int rows = shape.length;
        int cols = shape[0].length;
        int[][] rotated = new int[cols][rows];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[c][rows - 1 - r] = shape[r][c];
            }
        }

        int newRow = pivot.c;
        int newCol = rows - 1 - pivot.r;
        pivot = new Point(newRow, newCol);

        shape = rotated;
    }

    public void rotateCCW() {
        int rows = shape.length;
        int cols = shape[0].length;
        int[][] rotated = new int[cols][rows];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                rotated[cols - 1 - c][r] = shape[r][c];
            }
        }

        int newRow = cols - 1 - pivot.c;
        int newCol = pivot.r;
        pivot = new Point(newRow, newCol);

        shape = rotated;
    }
}