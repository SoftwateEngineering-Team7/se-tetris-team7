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
        activeBlock = null;

        initialPos = new Point(-1, width / 2);
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
    public boolean isValidPos(Point pos, Block activeBlock) {
        for (int r = 0; r < activeBlock.height(); r++) {
            for (int c = 0; c < activeBlock.width(); c++) {
                if (activeBlock.getCell(r, c) != 0) {
                    int row = pos.r - activeBlock.pivot.r + r;
                    int col = pos.c - activeBlock.pivot.c + c;

                    // 위쪽 경계를 벗어나는 셀은 무시 (화면 위쪽에서 블록이 시작할 수 있음)
                    if (row < 0) {
                        continue;
                    }
                    
                    // 좌, 우, 하 경계를 벗어나면 false 반환
                    if (col < 0 || col >= width || row >= height) {
                        return false;
                    }
                    
                    // 경계 안에 있고 충돌이 발생하면 false 반환
                    if (board[row][col] != 0) {
                        System.out.println("Collision occurred.");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    // 보드 크기 에 있는지 확인
    public boolean isInBound(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width;
    }

    // 새로운 블럭을 활성 블럭으로 설정 (배치 가능 여부 반환)
    public boolean setActiveBlock(Block block) {
        activeBlock = block;
        curPos = new Point(initialPos);
        
        // 초기 위치에 배치 가능한지 확인
        if (!isValidPos(curPos, activeBlock)) {
            return false; // 게임 오버
        }
        
        placeBlock(curPos, activeBlock);
        return true;
    }
    
    // 현재 블럭 위치 반환
    public Point getCurPos() {
        return curPos;
    }

    // -------------------- 이동 관련 함수들 --------------------
    // (이동할 좌표 p'을 생성하고 기존 블럭은 제거, p'에 배치가 가능하다면 curPos에 p'을 할당, 마지막으로 curPos에 블럭 배치
    // 후 이동 여부 반환)

    // 아래로 한칸 이동 함수
    public boolean moveDown() {
        boolean isMoved = false;
        Point downPos = curPos.down();
        removeBlock(curPos, activeBlock);

        if (isValidPos(downPos, activeBlock)) {
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

        if (isValidPos(rightPos, activeBlock)) {
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

        if (isValidPos(leftPos, activeBlock)) {
            curPos = leftPos;
            isMoved = true;
        }

        placeBlock(curPos, activeBlock);
        return isMoved;
    }

    // 매 임의의 시간마다 아래로 한칸 이동하는 함수 (이동이 불가하면 false 반환)
    public boolean autoDown() {
        return moveDown();
    }

    public int hardDrop() {
        removeBlock(curPos, activeBlock);
        int dropDistance = 0;
        while (isValidPos(curPos.down(), activeBlock)) {
            curPos = curPos.down();
            dropDistance++;
        }
        placeBlock(curPos, activeBlock);
        return dropDistance;
    }

    // 시계방향 90도 회전 함수
    public boolean rotate() {
        boolean isMoved = false;
        removeBlock(curPos, activeBlock);
        activeBlock.rotateCW();

        if (isValidPos(curPos, activeBlock)) {
            isMoved = true;
        } else {
            activeBlock.rotateCCW();
        }

        placeBlock(curPos, activeBlock);
        return isMoved;
    }

    // 라인 클리어 체크 및 처리
    public int clearLines() {
        int linesCleared = 0;
        
        for (int r = height - 1; r >= 0; r--) {
            boolean isLineFull = true;
            for (int c = 0; c < width; c++) {
                if (board[r][c] == 0) {
                    isLineFull = false;
                    break;
                }
            }
            
            if (isLineFull) {
                linesCleared++;
                // 해당 라인 삭제하고 위의 라인들을 아래로 이동
                for (int row = r; row > 0; row--) {
                    for (int col = 0; col < width; col++) {
                        board[row][col] = board[row - 1][col];
                    }
                }
                // 최상단 라인 초기화
                for (int col = 0; col < width; col++) {
                    board[0][col] = 0;
                }
                r++; // 같은 라인을 다시 체크
            }
        }
        
        return linesCleared;
    }
    
    
    // 보드 초기화
    public void reset() {
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                board[r][c] = 0;
            }
        }
        activeBlock = null;
        curPos = new Point(initialPos);
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
}