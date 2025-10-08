package org.tetris.game.model.blocks;

import org.tetris.game.model.utils.Point;

public class IBlock extends Block {

    public IBlock() {
        shape = new int[][] {
                { 1, 1, 1, 1 }
        };
        pivot = new Point(0, 1);
    }
}
