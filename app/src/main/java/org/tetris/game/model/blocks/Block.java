package org.tetris.game.model.blocks;

import java.util.ArrayList;

import org.util.GameColor;
import org.util.Point;

import javafx.scene.paint.Color;

public abstract class Block {
    public Point pivot;

    private int[][] shape;
    private GameColor color;
    private Point size;
    private int blockCount;

    private boolean canMove;
    private boolean canRotate;

    private boolean isItemBlock = false;

    public Block(int[][] shape, Point pivot, GameColor color) {
        this.shape = shape;
        this.pivot = pivot;
        this.color = color;
        canMove = true;
        canRotate = true;

        setSize();
        setBlockCount();
        getBlockPoints();
    }

    public Point getSize() {
        return size;
    }

    private void setSize() {
        this.size = new Point(shape.length, shape[0].length);
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

    public int getCell(Point p) {
        return shape[p.r][p.c];
    }

    public boolean getCanRotate() {
        return canRotate;
    }

    public boolean getCanMove() {
        return canMove;
    }

    public void setCanRotate(boolean canRotate) {
        this.canRotate = canRotate;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public void setIsItemBlock(boolean isItemBlock) {
        this.isItemBlock = isItemBlock;
    }

    public boolean isItemBlock() {
        return isItemBlock;
    }

    /**
     * 블럭 좌표 리스트 반환 메서드
     * 
     * @return 블럭 좌표 리스트
     */
    public ArrayList<Point> getBlockPoints() {
        ArrayList<Point> blockPoints = new ArrayList<>();
        for (int r = 0; r < size.r; r++) {
            for (int c = 0; c < size.c; c++) {
                if (shape[r][c] != 0) {
                    blockPoints.add(new Point(r, c));
                }
            }
        }
        return blockPoints;
    }

    /**
     * 피벗 기준 좌표 변환 메서드
     * pos - pivot
     * 
     * @param pos 기준 좌표
     * @return 변환된 좌표
     */
    public Point toPivot(Point pos) {
        return pos.subtract(pivot);
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
        int count = 0;
        for (int r = 0; r < size.r; r++) {
            for (int c = 0; c < size.c; c++) {
                if (shape[r][c] != 0) {
                    count++;
                }
            }
        }
        this.blockCount = count;
    }

    private void rotate(boolean clockwise) {
        int[][] rotated = new int[size.c][size.r];

        for (int r = 0; r < size.r; r++) {
            for (int c = 0; c < size.c; c++) {
                if (clockwise) {
                    rotated[c][size.r - 1 - r] = shape[r][c];
                } else {
                    rotated[size.c - 1 - c][r] = shape[r][c];
                }
            }
        }

        int newRow = clockwise ? pivot.c : size.c - 1 - pivot.c;
        int newCol = clockwise ? size.r - 1 - pivot.r : pivot.r;
        pivot = new Point(newRow, newCol);

        shape = rotated;
        setSize();
    }

    public void rotateCW() {
        rotate(true);
    }

    public void rotateCCW() {
        rotate(false);
    }

    public Color getColor() {
        return color.getColor();
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
