package org.tetris.game.model.blocks;

import org.util.GameColor;
import org.util.Point;

public class OBlock extends Block {

    private static final int[][] O_SHAPE = {
        { 4, 4 },
        { 4, 4 }
    };
    
    private static final Point O_POINT = new Point(0, 0);

    private static final GameColor O_COLOR = GameColor.GREEN;

    public OBlock() {
        super(O_SHAPE, O_POINT, O_COLOR);
    }

    @Override
    public void rotateCW() {
        return;
    }

    @Override
    public void rotateCCW() {
        return;
    }
}
