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

        block = itemController.spawnItem(block);
        assertNotNull(item);
        System.out.println("Randomly selected item ID\n" + block);
    }

    @Test
    public void testGetRandomItemWithSeed() {
        Block block1 = new ZBlock();
        Block block2 = new ZBlock();

        long seed = 12345L;
        ItemController itemController1 = new ItemController(seed);
        ItemController itemController2 = new ItemController();
        itemController2.resetWithSeed(seed);

        Item item1 = itemController1.getRandomItem();
        Item item2 = itemController2.getRandomItem();

        block1 = itemController1.spawnItem(block1);
        block2 = itemController2.spawnItem(block2);

        assertNotNull(item1);
        assertNotNull(item2);

        assertEquals(item1.getClass(), item2.getClass());
        assertEquals(block1.toString(), block2.toString());

        System.out.println("Randomly selected item ID with seed\n" + block1);
    }

    @Test
    public void testLItemGetItemBlock() {
        Block block = new ZBlock();
        Item item = new LItem();

        Block itemBlock = item.getItemBlock(block, new java.util.Random());

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

        Block itemBlock = item.getItemBlock(block, new java.util.Random());
        Point itemPos = item.getPosition();

        int itemValue = itemBlock.getCell(itemPos.r, itemPos.c);
        assertTrue(itemValue == 9);
    }
}
