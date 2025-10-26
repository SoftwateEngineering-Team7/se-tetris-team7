package org.tetris.game.model.items;

import java.util.Random;

import org.tetris.game.model.blocks.*;
import org.util.Point;

public class LItem extends Item {

    private final static int itemNum = 9;

    @Override
    public Block GetItemBlock(Block block) {
        Point length = block.getLength();
        int[][] shape = new int[length.r][length.c];

        Random rand = new Random();
        int itemIndex = rand.nextInt(block.getBlockCount());
        int count = 0;        

        for(int r = 0; r < length.r; r++){
            for(int c = 0; c < length.c; c++){
                shape[r][c] = block.getShape(r, c);
                if(shape[r][c] == 0) continue;

                count ++;
                if (count == itemIndex){
                    shape[r][c] = itemNum;
                    position = new Point(r, c);
                }
            }
        }

        return block.reShape(shape);
    }

    @Override
    public void Activate() {
        // Activate the item (e.g., apply its effect)
    }
    
}
