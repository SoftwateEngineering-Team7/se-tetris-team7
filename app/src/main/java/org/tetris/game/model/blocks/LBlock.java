package org.tetris.game.model.blocks;

import org.util.Point;

public class LBlock extends Block {
    public LBlock() {
        shape = new int[][] {
                { 3, 3, 3 },
                { 3, 0, 0 }
        };
        pivot = new Point(0, 1);
    }
}
