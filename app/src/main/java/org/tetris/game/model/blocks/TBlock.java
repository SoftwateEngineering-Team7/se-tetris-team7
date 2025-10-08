package org.tetris.game.model.blocks;

import org.util.Point;

public class TBlock extends Block {
    public TBlock() {
        shape = new int[][] {
                { 0, 6, 0 },
                { 6, 6, 6 }
        };
        pivot = new Point(1, 1);
    }
}
