package org.tetris.game.controller;

import org.tetris.game.model.Board;
import org.tetris.game.model.blocks.Block;
import org.tetris.game.model.items.*;

public class ItemController {
    private static final int ITEM_LINE_THRESHOLD = 5;

    private Item currentItem;
    private int lineClearedCount;

    public ItemController() {
        this.lineClearedCount = 0;
        resetCurrentItem();
    }

    private void resetCurrentItem() {
        currentItem = ItemController.getRandomItem();
    }

    /**
     * 아이템 생성 가능 여부 확인 메서드
     * @return 아이템 생성 가능 여부
     */
    public boolean canSpawnItem() {
        return lineClearedCount >= ITEM_LINE_THRESHOLD;
    }

    public boolean onLineCleared(int linesCleared) {
        lineClearedCount += linesCleared;
        return canSpawnItem();
    }

    /**
     * 아이템 스폰 메서드
     * @param block 바꿀 블럭
     * @return 아이템 블럭
     */
    public Block spawnItem(Block block) {
        lineClearedCount %= ITEM_LINE_THRESHOLD;

        resetCurrentItem();

        Block itemBlock = currentItem.getItemBlock(block);
        itemBlock.setIsItemBlock(true);

        return itemBlock;
    }

    public void activateItem(Board board, ItemActivation context) {
        currentItem.activate(board, context);
    }

    //region Static Functions
    private final static Item[] itemPool = {
            new LItem(),
            new BItem(),
            new CItem(),
            new HItem(),
            new WItem(),
    };

    /**
     * 랜덤 아이템 반환 메서드
     * 
     * @return 랜덤 아이템
     */
    public static Item getRandomItem() {
        int poolSize = itemPool.length;
        int randomIndex = (int) (Math.random() * poolSize);
        return itemPool[randomIndex];
    }
    //endregion
}
