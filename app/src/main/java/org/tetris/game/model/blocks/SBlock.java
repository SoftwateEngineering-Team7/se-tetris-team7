package org.tetris.game.model.blocks;

import org.tetris.game.model.utils.Point;

public class SBlock extends Block {
    public SBlock() {
        shape = new int[][] {
                { 0, 5, 5 },
                { 5, 5, 0 }
        };
        pivot = new Point(1, 1);
    }
}
