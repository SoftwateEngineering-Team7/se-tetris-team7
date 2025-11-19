package org.tetris.game.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.tetris.game.controller.ItemController;
import org.tetris.game.model.blocks.*;
import org.tetris.game.model.items.*;
import org.util.Point;

public class ItemTest {
    
    @Test
    public void testGetRandomItem() {
        Block block = new ZBlock();
        Item item = ItemController.getRandomItem();

        assertNotNull(item);
        System.out.println("Randomly selected item ID\n" + item.GetItemBlock(block));
    }

    @Test
    public void testLItemGetItemBlock() {
        Block block = new ZBlock();
        Item item = new LItem();

        Block itemBlock = item.GetItemBlock(block);

        System.out.println("Item Block\n" + itemBlock);

        Point itemPos = item.getPosition();
        System.out.println("Item Position: " + itemPos);

        int itemValue = itemBlock.getCell(itemPos.r, itemPos.c);
        assertTrue(itemValue == 9);
    }

    @Test
    public void testGetPosition() {
        Block block = new TBlock();
        Item item = new LItem();

        Block itemBlock = item.GetItemBlock(block);
        Point itemPos = item.getPosition();

        int itemValue = itemBlock.getCell(itemPos.r, itemPos.c);
        assertTrue(itemValue == 9);
    }
}
