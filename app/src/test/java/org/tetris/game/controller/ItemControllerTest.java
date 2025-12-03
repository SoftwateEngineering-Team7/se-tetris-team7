package org.tetris.game.controller;

import static org.junit.Assert.*;

import org.junit.Test;
import org.tetris.game.model.blocks.*;

public class ItemControllerTest {
    private ItemController itemController = new ItemController();

    @Test
    public void testInitClass()
    {
        itemController = new ItemController();
        assertNotNull(itemController);
    }

    @Test
    public void testCanSpawnItem() {
        itemController = new ItemController();
        assertFalse(itemController.canSpawnItem());
    }

    @Test
    public void testOnLineCleared() {
        itemController = new ItemController();

        itemController.onLineCleared(8);
        assertFalse(itemController.canSpawnItem());

        itemController.onLineCleared(4);
        assertTrue(itemController.canSpawnItem());
    }

    @Test
    public void testSpawnItem()
    {
        itemController = new ItemController();

        itemController.onLineCleared(12);
        assertTrue(itemController.canSpawnItem());

        Block itemBlock = new IBlock();
        itemBlock = itemController.spawnItem(itemBlock);

        assertTrue(itemBlock.isItemBlock());
        assertFalse(itemController.canSpawnItem());

        itemController.onLineCleared(10);
        assertTrue(itemController.canSpawnItem());
    }
}
