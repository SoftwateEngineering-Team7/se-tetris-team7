package org.tetris.game.model.blocks;

import org.util.GameColor;
import org.util.Point;

public abstract class Block {
    public Point pivot;

    private int[][] shape;
    private GameColor color;
    private Point length;
    private int blockCount;

    public Block(int[][] shape, Point pivot, GameColor color) {
        this.shape = shape;
        this.pivot = pivot;
        this.color = color;

        setLength();
        setBlockCount();
    }

    public Point getLength() {
        return length;
    }

    private void setLength() {
        this.length = new Point(shape.length, shape[0].length);
    }

    public int height() {
        return size.r;
    }

    public int width() {
        return size.c;
    }

    public int getCell(int r, int c) {
        return shape[r][c];
    }

    /**
     * 모양만 바꿔서 새로운 블록을 반환하는 메서드
     * 
     * @param shape 변경할 모양
     * @return 새로운 블록
     */
    public Block reShape(int[][] shape) {
        Block block = new ConcreteBlock(shape, this.pivot, this.color);
        return block;
    }

    public Block reShape(int[][] shape, Point pivot) {
        Block block = new ConcreteBlock(shape, pivot, this.color);
        return block;
    }

    /**
     * 블록의 개수를 반환하는 메서드
     * 
     * @return 블록의 개수
     */
    public int getBlockCount() {
        return blockCount;
    }

    private void setBlockCount() {
        blockCount = 0;
        for (int r = 0; r < size.r; r++) {
            for (int c = 0; c < size.c; c++) {
                if (shape[r][c] == 0)
                    continue;
                blockCount += 1;
            }
        }
    }

    public int getShape(int r, int c) {
        return shape[r][c];
    }

    /**
     * 모양만 바꿔서 새로운 블록을 반환하는 메서드
     * @param shape 변경할 모양
     * @return 새로운 블록
     */
    public Block reShape(int[][] shape){
        Block block = new ConcreteBlock(shape, pivot, color);
        return block;
    }

    /**
     * 블록의 개수를 반환하는 메서드
     * @return 블록의 개수
     */
    public int getBlockCount() {
        return blockCount;
    }

    private void setBlockCount() {
        blockCount = 0;
        for (int r = 0; r < length.r; r++) {
            for (int c = 0; c < length.c; c++) {
                if (shape[r][c] == 0) continue;
                blockCount += 1;
            }
        }
    }

    public void rotateCW() {
        int[][] rotated = new int[size.c][size.r];

        for (int r = 0; r < size.r; r++) {
            for (int c = 0; c < size.c; c++) {
                rotated[c][size.r - 1 - r] = shape[r][c];
            }
        }

        int newRow = pivot.c;
        int newCol = size.r - 1 - pivot.r;
        pivot = new Point(newRow, newCol);

        shape = rotated;
        setSize();
    }

    public void rotateCCW() {
        int[][] rotated = new int[size.c][size.r];
        for (int r = 0; r < size.r; r++) {
            for (int c = 0; c < size.c; c++) {
                rotated[size.c - 1 - c][r] = shape[r][c];
            }
        }

        int newRow = size.c - 1 - pivot.c;
        int newCol = pivot.r;
        pivot = new Point(newRow, newCol);

        shape = rotated;
        setSize();
    }

    public GameColor getColor() {
        return color;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < size.r; r++) {
            for (int c = 0; c < size.c; c++) {
                sb.append(shape[r][c]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
