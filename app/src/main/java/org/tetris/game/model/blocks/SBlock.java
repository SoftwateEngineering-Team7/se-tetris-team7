package org.tetris.game.model.blocks;

import org.tetris.game.model.Point;

public class SBlock extends Block {
    public SBlock() {
        shape = new int[][] {
                { 0, 1, 1 },
                { 1, 1, 0 }
        };
        pivot = new Point(1, 1);
    }
}
