package org.tetris.game.model.blocks;

import org.util.GameColor;
import org.util.Point;

public class SBlock extends Block {

    private static final int[][] S_SHAPE = {
        { 0, 5, 5 },
        { 5, 5, 0 }
    };
    
    private static final Point S_POINT = new Point(1, 1);

    private static final GameColor S_COLOR = GameColor.RED;

    public SBlock() {
        super(S_SHAPE, S_POINT, S_COLOR);
    }
}
