package org.tetris.game.model.items;

import org.tetris.game.model.Board;
import org.tetris.game.model.blocks.Block;

import org.util.Point;

public abstract class Item {

    protected Point position;

    public abstract Block GetItemBlock(Block block);

    public abstract void Activate(Board board);

    public Point getPosition() {
        return position;
    }
}