package org.tetris.game.model.blocks;

import org.tetris.game.model.Point;

public class ZBlock extends Block {
    public ZBlock() {
        shape = new int[][] {
                { 1, 1, 0 },
                { 0, 1, 1 }
        };
        pivot = new Point(1, 1);
    }
}
