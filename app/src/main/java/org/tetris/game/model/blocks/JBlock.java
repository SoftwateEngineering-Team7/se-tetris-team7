package org.tetris.game.model.blocks;

import org.tetris.game.model.utils.Point;

public class JBlock extends Block {
    public JBlock() {
        shape = new int[][] {
                { 2, 2, 2 },
                { 0, 0, 2 }
        };
        pivot = new Point(0, 1);
    }
}
