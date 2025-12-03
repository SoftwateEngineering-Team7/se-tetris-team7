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
        ItemController itemController = new ItemController();
        Item item = itemController.getRandomItem();

        assertNotNull(item);
        System.out.println("Randomly selected item ID\n" + item.getItemBlock(block));
    }

    @Test
    public void testGetRandomItemWithSeed() {
        Block block = new ZBlock();
        long seed = 12345L;
        ItemController itemController1 = new ItemController(seed);
        ItemController itemController2 = new ItemController();
        itemController2.resetWithSeed(seed);

        Item item1 = itemController1.getRandomItem();
        Item item2 = itemController2.getRandomItem();

        assertNotNull(item1);
        assertNotNull(item2);
        assertEquals(item1.getClass(), item2.getClass());

        System.out.println("Randomly selected item ID with seed\n" + item1.getItemBlock(block));
    }

    @Test
    public void testLItemGetItemBlock() {
        Block block = new ZBlock();
        Item item = new LItem();

        Block itemBlock = item.getItemBlock(block);

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

        Block itemBlock = item.getItemBlock(block);
        Point itemPos = item.getPosition();

        int itemValue = itemBlock.getCell(itemPos.r, itemPos.c);
        assertTrue(itemValue == 9);
    }
}
