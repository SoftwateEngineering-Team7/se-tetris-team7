package org.tetris.game.model.blocks;

import org.util.GameColor;
import org.util.Point;

public class IBlock extends Block {

    private static final int[][] I_SHAPE = {
        { 1, 1, 1, 1 }
    };
    
    private static final Point I_POINT = new Point(0, 1);

    private static final GameColor I_COLOR = GameColor.BLUE;

    public IBlock() {
        super(I_SHAPE, I_POINT, I_COLOR);
    }
}
