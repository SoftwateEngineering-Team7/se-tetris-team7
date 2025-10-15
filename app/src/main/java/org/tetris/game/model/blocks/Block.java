package org.tetris.game.model.blocks;

import org.util.GameColor;
import org.util.Point;

public abstract class Block {
    public Point pivot;
    
    private int[][] shape;
    private GameColor color;
    private Point length;

    public Block(int[][] shape, Point pivot, GameColor color) {
        this.shape = shape;
        this.pivot = pivot;
        this.color = color;

        setLength();
    }

    private void setLength() {
        this.length = new Point(shape.length, shape[0].length);
    }

    public int getShape(int r, int c) {
        return shape[r][c];
    }

    public int height() {
        return length.r;
    }

    public int width() {
        return length.c;
    }

    public void rotateCW() {
        int[][] rotated = new int[length.c][length.r];

        for (int r = 0; r < length.r; r++) {
            for (int c = 0; c < length.c; c++) {
                rotated[c][length.r - 1 - r] = shape[r][c];
            }
        }

        int newRow = pivot.c;
        int newCol = length.r - 1 - pivot.r;
        pivot = new Point(newRow, newCol);

        shape = rotated;
        setLength();
    }

    public void rotateCCW() {
        int[][] rotated = new int[length.c][length.r];
        for (int r = 0; r < length.r; r++) {
            for (int c = 0; c < length.c; c++) {
                rotated[length.c - 1 - c][r] = shape[r][c];
            }
        }

        int newRow = length.c - 1 - pivot.c;
        int newCol = pivot.r;
        pivot = new Point(newRow, newCol);

        shape = rotated;
        setLength();
    }

    public GameColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < length.r; r++) {
            for (int c = 0; c < length.c; c++) {
                sb.append(shape[r][c]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
