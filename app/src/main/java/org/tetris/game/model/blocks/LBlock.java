package org.tetris.game.model.blocks;

import org.tetris.game.model.Point;

public class LBlock extends Block {
    public LBlock() {
        shape = new int[][] {
                { 1, 1, 1 },
                { 1, 0, 0 }
        };
        pivot = new Point(0, 1);
    }
}
