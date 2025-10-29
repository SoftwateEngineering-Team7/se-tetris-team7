package org.tetris.game.model.items;

import java.util.Random;

import org.tetris.game.model.Board;
import org.tetris.game.model.blocks.*;
import org.util.Point;

public class LItem extends Item {

    private final static int LItemID = 9;

    public LItem() {
        super(LItemID);
    }

    @Override
    public Block GetItemBlock(Block block) {
        Point size = block.getSize();
        int[][] shape = new int[size.r][size.c];
        
        var blockPoints = block.getBlockPoints();

        for(Point p : blockPoints){
            shape[p.r][p.c] = block.getCell(p);
        }

        Random rand = new Random();
        int itemIndex = rand.nextInt(blockPoints.size());
        Point itemPoint = blockPoints.get(itemIndex);
        shape[itemPoint.r][itemPoint.c] = itemID;
        
        itemBlock = block.reShape(shape);
        return itemBlock;
    }

    @Override
    public void Activate(Board board) {
        Point blockPos = board.getCurPos();
        Point itemPos = getPosition();

        int row = blockPos.r - itemBlock.pivot.r + itemPos.r;
        board.clearALine(row);
    }
}