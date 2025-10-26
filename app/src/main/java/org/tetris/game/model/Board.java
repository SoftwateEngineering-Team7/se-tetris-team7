package org.tetris.game.model;

import org.tetris.game.model.blocks.*;
import org.tetris.game.model.items.*;
import org.tetris.shared.BaseModel;
import org.util.Point;

public class Board extends BaseModel{
    private final int height;
    private final int width;

    private int[][] board;
    public Block activeBlock;
    private Point curPos;
    private Point initialPos;
    
    private NextBlockModel nextBlockModel;

    private boolean isItemMode = false;
    private Item activeItem = null;

    // Board 생성자
    public Board() {
        this(20, 10);
    }

    public Board(int h, int w) {
        this.height = h;
        this.width = w;

        board = new int[height][width];
        nextBlockModel = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 5);
        activeBlock = nextBlockModel.getBlock();

        initialPos = new Point(0, width / 2);
        curPos = new Point(initialPos);
    }

    public int[][] getBoard() {
        return board;
    }

    public Point getSize(){
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

    // 배치 시도 후 가능 여부 반환
    public boolean isValidPos(Point pos) {
        boolean hasInBoundCell = false;
        
        for (int r = 0; r < activeBlock.height(); r++) {
            for (int c = 0; c < activeBlock.width(); c++) {
                if (activeBlock.getCell(r, c) != 0) {
                    int row = pos.r - activeBlock.pivot.r + r;
                    int col = pos.c - activeBlock.pivot.c + c;

                    if (isInBound(row, col)) {
                        hasInBoundCell = true;
                        // 경계 안에 있고 충돌이 발생하면 false 반환
                        if (board[row][col] != 0) {
                            System.out.println("Collision occurred.");
                            return false;
                        }
                    }
                    // 경계 밖 셀은 무시 (화면 위쪽에서 블록이 시작할 수 있음)
                }
            }
        }
        
        // 최소 한 개의 셀이 경계 안에 있어야 함
        return hasInBoundCell;
    }

    // 보드 크기 에 있는지 확인
    public boolean isInBound(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    // 기존 활성 블럭은 고정되고 활성 블럭에 새로운 랜덤블럭 반환
    public void setActiveToStaticBlock() {
        activeBlock = nextBlockModel.getBlock();
        curPos = new Point(initialPos);
    }

    // -------------------- 이동 관련 함수들 --------------------
    // (이동할 좌표 p'을 생성하고 기존 블럭은 제거, p'에 배치가 가능하다면 curPos에 p'을 할당, 마지막으로 curPos에 블럭 배치
    // 후 이동 여부 반환)

    // 아래로 한칸 이동 함수
    public boolean moveDown() {
        boolean isMoved = false;
        Point downPos = curPos.down();
        removeBlock(curPos, activeBlock);

        if (isValidPos(downPos)) {
            curPos = downPos;
            isMoved = true;
        }

        placeBlock(curPos, activeBlock);
        return isMoved;
    }

    // 오른쪽 한칸 이동 함수
    public boolean moveRight() {
        boolean isMoved = false;
        Point rightPos = curPos.right();
        removeBlock(curPos, activeBlock);

        if (isValidPos(rightPos)) {
            curPos = rightPos;
            isMoved = true;
        }

        placeBlock(curPos, activeBlock);
        return isMoved;
    }

    // 왼쪽 한칸 이동 함수
    public boolean moveLeft() {
        boolean isMoved = false;
        Point leftPos = curPos.left();
        removeBlock(curPos, activeBlock);

        if (isValidPos(leftPos)) {
            curPos = leftPos;
            isMoved = true;
        }

        placeBlock(curPos, activeBlock);
        return isMoved;
    }

    // 매 임의의 시간마다 아래로 한칸 이동하는 함수 (이동이 불가하면 활성 블럭은 고정되고 새로운 블럭으로 반환)
    public void autoDown() {
        boolean isDown = moveDown();
        if (!isDown)
            setActiveToStaticBlock();
    }

    public void hardDrop() {
        while (isValidPos(curPos.down())) {
            curPos = curPos.down();
        }
        placeBlock(curPos, activeBlock);
        setActiveToStaticBlock();
    }

    // 시계방향 90도 회전 함수
    public boolean rotate() {
        boolean isMoved = false;
        removeBlock(curPos, activeBlock);
        activeBlock.rotateCW();

        if (isValidPos(curPos)) {
            isMoved = true;
        } else {
            activeBlock.rotateCCW();
        }

        placeBlock(curPos, activeBlock);
        return isMoved;
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

    public int getCell(int row, int col) {
        if (isInBound(row, col)) {
            return board[row][col];
        }
        throw new IndexOutOfBoundsException("Row or Column out of bounds");
    }
}