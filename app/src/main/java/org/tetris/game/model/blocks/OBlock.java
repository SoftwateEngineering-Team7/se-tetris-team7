package org.tetris.game.model.blocks;

import org.tetris.game.model.Point;

public class OBlock extends Block {
    public OBlock() {
        shape = new int[][] {
                { 1, 1 },
                { 1, 1 }
        };
        pivot = new Point(0, 0);
    }
}
