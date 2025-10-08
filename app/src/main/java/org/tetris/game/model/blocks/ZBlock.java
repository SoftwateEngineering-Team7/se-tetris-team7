package org.tetris.game.model.blocks;

import org.tetris.game.model.utils.Point;

public class ZBlock extends Block {
    public ZBlock() {
        shape = new int[][] {
                { 7, 7, 0 },
                { 0, 7, 7 }
        };
        pivot = new Point(1, 1);
    }
}
