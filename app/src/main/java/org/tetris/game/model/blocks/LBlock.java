package org.tetris.game.model.blocks;

import org.util.GameColor;
import org.util.Point;

public class LBlock extends Block {

    private static final int[][] L_SHAPE = {
        { 3, 3, 3 },
        { 3, 0, 0 }
    };
    
    private static final Point L_POINT = new Point(0, 1);

    private static final GameColor L_COLOR = GameColor.YELLOW;

    public LBlock() {
        super(L_SHAPE, L_POINT, L_COLOR);
    }
}
