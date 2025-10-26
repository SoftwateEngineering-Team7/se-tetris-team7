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
        Point size = block.getLength();
        int[][] shape = new int[size.r][size.c];

        Random rand = new Random();
        int itemIndex = rand.nextInt(block.getBlockCount());
        int count = 0;

        for(int r = 0; r < size.r; r++){
            for(int c = 0; c < size.c; c++){
                shape[r][c] = block.getShape(r, c);
                if(shape[r][c] == 0) continue;

                count++;
                if (count == itemIndex) {
                    shape[r][c] = itemID;
                    setPosition(new Point(r, c));
                }
            }
        }

        return block.reShape(shape);
    }

    @Override
    public void Activate(Board board) {
        // Activate the item (e.g., apply its effect)
    }
    
}
