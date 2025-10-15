package org.tetris.game.model.blocks;

import org.util.GameColor;
import org.util.Point;

public class ZBlock extends Block {

    private static final int[][] Z_SHAPE = {
        { 7, 7, 0 },
        { 0, 7, 7 }
    };
    
    private static final Point Z_POINT = new Point(1, 1);

    private static final GameColor Z_COLOR = GameColor.CYAN;

    public ZBlock() {
        super(Z_SHAPE, Z_POINT, Z_COLOR);
    }
}
