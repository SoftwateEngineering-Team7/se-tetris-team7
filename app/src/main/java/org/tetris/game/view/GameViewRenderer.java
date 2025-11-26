package org.tetris.game.view;

import java.util.List;

import org.tetris.game.model.blocks.Block;
import org.util.GameColor;
import org.util.Point;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.util.function.BiFunction;

public class GameViewRenderer {
    // 부모 Pane 레퍼런스 (레이아웃 설정용)
    // 레이아웃(위치/크기) 설정은 pane 기준으로 수행
    private final Pane boardPane;
    private final Pane nextBlockPane;

    // 메인 보드용
    private final Canvas boardCanvas;
    private final GraphicsContext boardGc; // 메인 보드 그래픽 컨텍스트 -> 캔버스에서 펜 역할

    // 다음 블록 프리뷰용
    private final Canvas previewCanvas;
    private final GraphicsContext previewGc; // 다음 블록 프리뷰 그래픽 컨텍스트 -> 캔버스에서 펜 역할

    private final Point boardSize;
    private final int cellSize;
    private final int previewCellSize;

    public GameViewRenderer(Pane boardPane, Pane nextBlockPane, Point boardSize, int cellSize, int previewCellSize) {
        this.boardPane = boardPane;
        this.nextBlockPane = nextBlockPane;
        this.boardSize = boardSize;
        this.cellSize = cellSize;
        this.previewCellSize = previewCellSize;

        // ===== 메인 보드 캔버스 생성 =====
        int canvasWidth = boardSize.c * cellSize;
        int canvasHeight = boardSize.r * cellSize;

        this.boardCanvas = new Canvas(canvasWidth, canvasHeight);
        this.boardGc = boardCanvas.getGraphicsContext2D();

        // Pane에 추가 (레이아웃/리스너는 나중에)
        boardPane.getChildren().clear();
        boardPane.getChildren().add(boardCanvas);

        // ===== 다음 블록 프리뷰 캔버스 생성 =====
        if (nextBlockPane != null) {
            double paneWidth = nextBlockPane.getPrefWidth();
            double paneHeight = nextBlockPane.getPrefHeight();

            if (paneWidth <= 0)
                paneWidth = 4 * previewCellSize;
            if (paneHeight <= 0)
                paneHeight = 4 * previewCellSize;

            this.previewCanvas = new Canvas(paneWidth, paneHeight);
            this.previewGc = previewCanvas.getGraphicsContext2D();

            nextBlockPane.getChildren().clear();
            nextBlockPane.getChildren().add(previewCanvas);
        } else {
            this.previewCanvas = null;
            this.previewGc = null;
        }
    }

    // ========================
    // 메인 보드 렌더링
    // ========================
    public void renderBoard(int[][] board, boolean[][] flashMask, boolean isFlashing, boolean flashOn) {
        if (board == null || boardGc == null)
            return;

        boardReset();

        for (int r = 0; r < boardSize.r; r++) {
            for (int c = 0; c < boardSize.c; c++) {
                boolean flashingThisCell = isFlashing &&
                        flashMask != null &&
                        flashMask[r][c] &&
                        flashOn;
                // 플래시 중이고 해당 셀이 플래시 대상인 경우 그리고 플래시가 켜진 상태일 때 -> 흰색으로 렌더링

                int cellValue = board[r][c];

                // 빈 셀이고 플래시 대상도 아니면 스킵
                if (cellValue == 0 && !flashingThisCell) {
                    continue;
                }

                Color fill = flashingThisCell ? Color.WHITE : getCellColor(cellValue); // 플래시 중이면 흰색

                // flashingThisCell 이면 cellValue==0 즉 비어있는 칸이라도 강제로 칠하도록 함.
                drawCell(
                        boardGc,
                        r, c,
                        0, 0,
                        cellSize,
                        cellValue,
                        fill,
                        flashingThisCell);
            }
        }
    }

    public void boardReset() {
        // 배경 초기화
        boardGc.setFill(Color.BLACK);
        boardGc.fillRect(0, 0, boardSize.c * cellSize, boardSize.r * cellSize);
    }

    // ========================
    // 싱글 플레이어 레이아웃 설정
    // ========================
    public void setupSinglePlayerLayout() {
        if (boardPane != null) {
            // (paneSize - canvasSize) / 2 → 가운데 정렬
            bindCanvasPosition(
                    boardPane,
                    boardCanvas,
                    (paneW, canvasW) -> (paneW - canvasW) / 2.0, // X
                    (paneH, canvasH) -> (paneH - canvasH) / 2.0 // Y
            );
        }

        if (nextBlockPane != null && previewCanvas != null) {
            // nextBlockPane 안에서 프리뷰 캔버스를 정중앙에 위치
            bindCanvasPosition(
                    nextBlockPane,
                    previewCanvas,
                    (paneW, canvasW) -> (paneW - canvasW) / 2.0, // X 중앙
                    (paneH, canvasH) -> (paneH - canvasH) / 2.0 // Y 중앙
            );
        }
    }

    // ========================
    // 다음 블록 프리뷰 렌더링
    // ========================
    public void renderNextBlock(Block nextBlock) {
        if (previewGc == null || previewCanvas == null)
            return;

        double canvasWidth = previewCanvas.getWidth();
        double canvasHeight = previewCanvas.getHeight();

        // 배경 초기화
        previewGc.clearRect(0, 0, canvasWidth, canvasHeight);

        if (nextBlock == null)
            return;

        int blockWidth = nextBlock.getSize().c;
        int blockHeight = nextBlock.getSize().r;

        // 1) 실제 블록 모양의 바운딩 박스 계산 (빈 셀 제외)
        int[] bounds = computeBlockBounds(nextBlock, blockWidth, blockHeight);
        if (bounds == null)
            return;

        int minR = bounds[0];
        int maxR = bounds[1];
        int minC = bounds[2];
        int maxC = bounds[3];

        int actualWidth = maxC - minC + 1;
        int actualHeight = maxR - minR + 1;

        // 2) 실제 모양 기준 중앙 정렬 오프셋 계산
        double offsetX = (canvasWidth - actualWidth * previewCellSize) / 2.0 - minC * previewCellSize;
        double offsetY = (canvasHeight - actualHeight * previewCellSize) / 2.0 - minR * previewCellSize;

        // 3) 셀 단위 렌더링 (헬퍼 조합)
        for (int r = 0; r < blockHeight; r++) {
            for (int c = 0; c < blockWidth; c++) {
                int cellValue = nextBlock.getCell(r, c);
                if (cellValue == 0) {
                    continue;
                }

                Color fillColor = getCellColor(cellValue);

                // 프리뷰는 빈 셀을 강제로 칠할 일은 없으니 drawEvenIfEmpty=false
                drawCell(
                        previewGc,
                        r, c,
                        offsetX, offsetY,
                        previewCellSize,
                        cellValue,
                        fillColor,
                        false);
            }
        }
    }

    // ========================
    // 플래시 마스크 생성
    // ========================
    public boolean[][] buildFlashMask(List<Integer> rows, List<Integer> cols, List<Point> cells) {
        boolean[][] mask = new boolean[boardSize.r][boardSize.c];
        markFlashRows(rows, mask);
        markFlashCols(cols, mask);
        markFlashCells(cells, mask);
        return mask;
    }

    // ========================
    // 내부 헬퍼 메서드들
    // ========================

    // 캔버스 위치를 부모 Pane 크기에 맞춰 동적으로 위치를 조정하는 헬퍼
    private void bindCanvasPosition(
            Pane pane,
            Canvas canvas,
            BiFunction<Double, Double, Double> layoutXFunc,
            BiFunction<Double, Double, Double> layoutYFunc) {

        // 처음 한 번 현재 크기로 즉시 정렬
        double w = pane.getWidth();
        double h = pane.getHeight();
        if (w > 0 && h > 0) {
            canvas.setLayoutX(layoutXFunc.apply(w, canvas.getWidth()));
            canvas.setLayoutY(layoutYFunc.apply(h, canvas.getHeight()));
        }

        // width 변화에 따라 X 위치 갱신
        pane.widthProperty().addListener((obs, oldVal, newVal) -> {
            double paneW = newVal.doubleValue();
            double canvasW = canvas.getWidth();
            canvas.setLayoutX(layoutXFunc.apply(paneW, canvasW));
        });

        // height 변화에 따라 Y 위치 갱신
        pane.heightProperty().addListener((obs, oldVal, newVal) -> {
            double paneH = newVal.doubleValue();
            double canvasH = canvas.getHeight();
            canvas.setLayoutY(layoutYFunc.apply(paneH, canvasH));
        });
    }

    // 보드/프리뷰 공통: 타일(사각형 + 아이템 텍스트) 하나 그리기
    private void drawCell(
            GraphicsContext gc,
            int row,
            int col,
            double offsetX,
            double offsetY,
            double size,
            int cellValue,
            Color fillColor,
            boolean flashingCell) {

        // 일반 모드에서는 0 그리고 플래시 모드가 아닌 경우 스킵
        if (cellValue == 0 && !flashingCell) {
            return;
        }

        final double strokeWidth = 1.0;
        final double cellInset = 1.0;

        double x = offsetX + col * size + cellInset;
        double y = offsetY + row * size + cellInset;

        double w = size - cellInset * 2;
        double h = size - cellInset * 2;

        // 바탕 사각형
        gc.setFill(fillColor);
        gc.fillRect(x, y, w, h);

        // 테두리
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(strokeWidth);
        gc.strokeRect(x, y, w, h);

        // 아이템 문자(L/W/V/B/C 등) 표시: 실제 블록이 있는 칸에만 표시
        if (cellValue != 0) {
            drawCellText(gc, x, y, size, cellValue);
        }
    }

    private void drawCellText(GraphicsContext gc, double x, double y, double size, int cellValue) {
        String cellText = getCellText(cellValue);
        if (cellText.isEmpty()) {
            return;
        }

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, size * 0.6));

        Text text = new Text(cellText);
        text.setFont(gc.getFont());

        double textWidth = text.getBoundsInLocal().getWidth();
        double textHeight = text.getBoundsInLocal().getHeight();

        double textX = x + (size - textWidth) / 2;
        double textY = y + (size + textHeight) / 2 - 2;

        gc.fillText(cellText, textX, textY);
    }

    // Block의 실제 바운딩 박스 계산 (minR, maxR, minC, maxC 반환)
    private int[] computeBlockBounds(Block block, int blockWidth, int blockHeight) {
        int minR = blockHeight, maxR = -1;
        int minC = blockWidth, maxC = -1;

        for (int r = 0; r < blockHeight; r++) {
            for (int c = 0; c < blockWidth; c++) {
                if (block.getCell(r, c) != 0) {
                    if (r < minR)
                        minR = r;
                    if (r > maxR)
                        maxR = r;
                    if (c < minC)
                        minC = c;
                    if (c > maxC)
                        maxC = c;
                }
            }
        }

        if (maxR == -1)
            return null; // 활성 셀이 하나도 없는 경우
        return new int[] { minR, maxR, minC, maxC };
    }

    private void markFlashRows(List<Integer> rows, boolean[][] mask) {
        if (rows == null)
            return;
        for (int r : rows) {
            if (r < 0 || r >= boardSize.r)
                continue;
            java.util.Arrays.fill(mask[r], true);
        }
    }

    private void markFlashCols(List<Integer> cols, boolean[][] mask) {
        if (cols == null)
            return;
        for (int c = 0; c < boardSize.c; c++) {
            if (!cols.contains(c))
                continue;
            for (int r = 0; r < boardSize.r; r++) {
                mask[r][c] = true;
            }
        }
    }

    private void markFlashCells(List<Point> cells, boolean[][] mask) {
        if (cells == null)
            return;
        for (Point p : cells) {
            if (p.r >= 0 && p.r < boardSize.r && p.c >= 0 && p.c < boardSize.c) {
                mask[p.r][p.c] = true;
            }
        }
    }

    private Color getCellColor(int cellValue) {
        switch (cellValue) {
            case 1:
                return GameColor.BLUE.getColor(); // IBlock
            case 2:
                return GameColor.ORANGE.getColor(); // JBlock
            case 3:
                return GameColor.YELLOW.getColor(); // LBlock
            case 4:
                return GameColor.GREEN.getColor(); // OBlock
            case 5:
                return GameColor.RED.getColor(); // SBlock
            case 6:
                return GameColor.PURPLE.getColor(); // TBlock
            case 7:
                return GameColor.CYAN.getColor(); // ZBlock
            case 8:
                return Color.GRAY;
            default:
                return Color.WHITE;
        }
    }

    private String getCellText(int cellValue) {
        switch (cellValue) {
            case 9:
                return "L";
            case 10:
                return "W";
            case 11:
                return "V";
            case 12:
                return "B";
            case 13:
                return "C";
            default:
                return "";
        }
    }
}
