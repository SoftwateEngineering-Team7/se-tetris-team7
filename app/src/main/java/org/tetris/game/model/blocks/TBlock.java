package org.tetris.game.model.blocks;

import org.tetris.game.model.Point;

public class TBlock extends Block {
    public TBlock() {
        shape = new int[][] {
                { 0, 1, 0 },
                { 1, 1, 1 }
        };
        pivot = new Point(1, 1);
    }
}
