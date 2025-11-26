package org.tetris.game.model.items;

import org.tetris.game.model.Board;
import org.tetris.game.model.blocks.*;
import org.util.Point;

public class WItem extends Item {

    private static final int[][] W_SHAPE = {
            { 0, 10, 10, 0 },
            { 10, 10, 10, 10 }
    };

    public WItem() {
        super(10);
    }

    @Override
    public Block getItemBlock(Block block) {
        int[][] shape = W_SHAPE;
        Point pivot = new Point(1, 1);
        itemBlock = block.reShape(shape, pivot);
        itemBlock.setCanRotate(false);
        return itemBlock;
    }

    @Override
    public void activate(Board board, ItemActivation context) {
        itemBlock.setForceDown(true);
    }

}
