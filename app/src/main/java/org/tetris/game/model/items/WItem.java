package org.tetris.game.model.items;

import org.tetris.game.model.Board;
import org.tetris.game.model.blocks.*;
import org.util.Point;

public class WItem extends Item {

    private static final int[][] W_SHAPE = {
            { 0, 10, 10, 0 },
            { 10, 10, 10, 10 }
    };

    protected WItem() {
        super(10);
    }

    @Override
    public Block GetItemBlock(Block block) {
        int[][] shape = W_SHAPE;
        Point pivot = new Point(1, 1);
        return block.reShape(shape, pivot);
    }

    @Override
    public void Activate(Board board) {
        
    }

}
