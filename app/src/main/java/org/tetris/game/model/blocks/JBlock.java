package org.tetris.game.model.blocks;

import org.tetris.game.model.Point;

public class JBlock extends Block {
    public JBlock() {
        shape = new int[][] {
                { 1, 1, 1 },
                { 0, 0, 1 }
        };
        pivot = new Point(0, 1);
    }
}
