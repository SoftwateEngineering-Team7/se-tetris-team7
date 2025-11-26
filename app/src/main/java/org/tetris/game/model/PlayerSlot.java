package org.tetris.game.model;

import java.util.ArrayList;
import java.util.List;

import org.tetris.game.view.GameViewRenderer;
import org.util.Point;

public class PlayerSlot {
    public final Board boardModel;
    public final NextBlockModel nextBlockModel;
    public final ScoreModel scoreModel;
    public final GameViewRenderer renderer;

    public boolean isFlashing = false;
    public boolean flashOn = false;
    public int flashToggleCount = 0;
    public long nextFlashAt = 0L;
    public boolean[][] flashMask;

    public final List<Integer> clearingRows = new ArrayList<>();
    public final List<Integer> clearingCols = new ArrayList<>();
    public final List<Point> clearingCells = new ArrayList<>();

    public PlayerSlot(Board boardModel,
            NextBlockModel nextBlockModel,
            ScoreModel scoreModel,
            GameViewRenderer renderer) {
        this.boardModel = boardModel;
        this.nextBlockModel = nextBlockModel;
        this.scoreModel = scoreModel;
        this.renderer = renderer;
    }
}
