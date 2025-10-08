package org.tetris.game.model.blocks;

import org.util.Point;

public class OBlock extends Block {
    public OBlock() {
        shape = new int[][] {
                { 4, 4 },
                { 4, 4 }
        };
        pivot = new Point(0, 0);
    }
}
