package org.tetris.game.model.items;

import java.util.Random;

import org.tetris.game.model.Board;
import org.tetris.game.model.blocks.*;
import org.util.Point;

public class HItem extends Item {

    private final static int HItemID = 11;

    public HItem() {
        super(HItemID);
    }

    @Override
    public Block getItemBlock(Block block, Random random) {
        Point size = block.getSize();
        int[][] shape = new int[size.r][size.c];
        
        var blockPoints = block.getBlockPoints();

        for(Point p : blockPoints){
            shape[p.r][p.c] = block.getCell(p);
        }
        
        int itemIndex = random.nextInt(blockPoints.size());
        Point itemPoint = blockPoints.get(itemIndex);
        shape[itemPoint.r][itemPoint.c] = itemID;
        
        itemBlock = block.reShape(shape);
        return itemBlock;
    }

    @Override
    public void activate(Board board, ItemActivation context) {
        Point blockPos = board.getCurPos();
        Point itemPos = getPosition();

        int col = blockPos.c - itemBlock.pivot.c + itemPos.c;
        context.addClearingCol(col);
    }
    
}
