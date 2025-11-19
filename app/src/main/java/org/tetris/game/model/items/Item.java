package org.tetris.game.model.items;

import org.tetris.game.model.Board;
import org.tetris.game.model.blocks.Block;

import org.util.Point;

public abstract class Item {

    protected int itemID;
    protected Block itemBlock;

    protected Item(int id) {
        this.itemID = id;
    }

    public abstract Block getItemBlock(Block block);

    public abstract void activate(Board board, ItemActivation context);

    /**
     * 아이템 블럭 내 아이템 좌표 반환 메서드
     * 
     * @return 아이템 좌표
     */
    public Point getPosition() {
        Point position = new Point(0, 0);

        for (Point p : itemBlock.getBlockPoints()) {
            if (itemBlock.getCell(p) == itemID) {
                position = new Point(p);
                break;
            }
        }

        return position;
    }
}