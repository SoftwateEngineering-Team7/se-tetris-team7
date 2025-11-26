package org.tetris.game.model;

import java.util.ArrayList;
import java.util.List;

import org.tetris.game.view.GameViewRenderer;
import org.util.Point;

public class PlayerSlot {
    public final GameModel gameModel;
    public final Board boardModel;
    public final NextBlockModel nextBlockModel;
    public final ScoreModel scoreModel;
    public final AttackModel attackModel;
    public final GameViewRenderer renderer;

    // 게임 루프 관련 변수
    public long lastDropTime = 0;

    // 플래시 상태 관련 변수
    public boolean isFlashing = false;
    public boolean flashOn = false;
    public int flashToggleCount = 0;
    public long nextFlashAt = 0L;
    public boolean[][] flashMask;

    public final List<Integer> clearingRows = new ArrayList<>();
    public final List<Integer> clearingCols = new ArrayList<>();
    public final List<Point> clearingCells = new ArrayList<>();

    public PlayerSlot(
            GameModel gameModel,
            Board boardModel,
            NextBlockModel nextBlockModel,
            ScoreModel scoreModel,
            AttackModel attackModel,
            GameViewRenderer renderer) {
        this.gameModel = gameModel;
        this.boardModel = boardModel;
        this.nextBlockModel = nextBlockModel;
        this.scoreModel = scoreModel;
        this.attackModel = attackModel;
        this.renderer = renderer;
    }

    public void reset(){
        isFlashing = false;
        flashOn = false;
        flashToggleCount = 0;
        nextFlashAt = 0L;
        flashMask = null;

        lastDropTime = 0;

        clearingRows.clear();
        clearingCols.clear();
        clearingCells.clear();
        
        gameModel.reset();
        if(attackModel != null)
            attackModel.reset();
        renderer.boardReset();
    }
}
