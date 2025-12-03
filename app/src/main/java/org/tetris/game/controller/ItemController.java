package org.tetris.game.controller;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Supplier;

import org.tetris.game.model.Board;
import org.tetris.game.model.blocks.Block;
import org.tetris.game.model.items.*;

public class ItemController {
    private static final int ITEM_LINE_THRESHOLD = 1;

    private Item currentItem;
    private int lineClearedCount;
    private Random random;

    public ItemController() {
        this.lineClearedCount = 0;
        this.random = new Random();
        resetCurrentItem();
    }

    public ItemController(long seed) {
        this.lineClearedCount = 0;
        this.random = new Random(seed);
        resetCurrentItem();
    }

    public void resetWithSeed(long seed) {
		this.random = new Random(seed);
        resetCurrentItem();
    }

    private void resetCurrentItem() {
        currentItem = getRandomItem();
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

        Block itemBlock = currentItem.getItemBlock(block, random);
        itemBlock.setIsItemBlock(true);

        return itemBlock;
    }

    public void activateItem(Board board, ItemActivation context) {
        currentItem.activate(board, context);
    }

    // region Static Functions
    private static final ArrayList<Supplier<Item>> itemPoolList = new ArrayList<Supplier<Item>>() {{
		add(() -> new LItem());
        add(() -> new BItem());
        add(() -> new CItem());
        add(() -> new HItem());
        add(() -> new WItem());
	}};

    /**
     * 랜덤 아이템 반환 메서드
     * 
     * @return 랜덤 아이템
     */
    public Item getRandomItem() {
        int poolSize = itemPoolList.size();
        int randomIndex = random.nextInt(poolSize);
        return itemPoolList.get(randomIndex).get();
    }
    //endregion
}
