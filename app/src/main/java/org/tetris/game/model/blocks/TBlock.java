package org.tetris.game.model.blocks;

import org.util.GameColor;
import org.util.Point;

public class TBlock extends Block {

    private static final int[][] T_SHAPE = {
        { 0, 6, 0 },
        { 6, 6, 6 }
    };
    
    private static final Point T_POINT = new Point(1, 1);

    private static final GameColor T_COLOR = GameColor.PURPLE;

    public TBlock() {
        super(T_SHAPE, T_POINT, T_COLOR);
    }
}
