package org.tetris.game.model.blocks;

import org.util.GameColor;
import org.util.Point;

public class JBlock extends Block {

    private static final int[][] J_SHAPE = {
        { 2, 2, 2 },
        { 0, 0, 2 }
    };
    
    private static final Point J_POINT = new Point(0, 1);

    private static final GameColor J_COLOR = GameColor.ORANGE;

    public JBlock() {
        super(J_SHAPE, J_POINT, J_COLOR);
    }
}
