package org.tetris.game.model.items;

import org.tetris.game.model.Board;
import org.tetris.game.model.blocks.Block;

import org.util.Point;

public abstract class Item {

    protected Point position = new Point(0, 0);
    protected int itemID;

    protected Item(int id) {
        this.itemID = id;
    }

    public abstract Block GetItemBlock(Block block);

    public abstract void Activate(Board board);

    protected void setPosition(Point position) {
        this.position = position;
    }

    public Point getPosition() {
        return position;
    }
}