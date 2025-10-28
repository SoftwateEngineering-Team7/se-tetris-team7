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

    public Point getItemPositionOnBoard(Point blockPos) {
        return new Point(blockPos.subtract(position));
    }

    private final static Item[] itemPool = {
        new LItem(),
        new HItem(),
        new BItem(),
        new CItem()
    };

    public static Item getRandomItem() {
        int poolSize = itemPool.length;
        int randomIndex = (int)(Math.random() * poolSize);
        return itemPool[randomIndex];
    }
}