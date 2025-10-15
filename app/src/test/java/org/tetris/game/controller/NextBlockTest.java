package org.tetris.game.controller;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import org.tetris.game.model.blocks.*;

public class NextBlockTest {
    // 테스트를 위한 블럭 리스트
    private ArrayList<Block> blockList;

    @Before
    public void setUp() {
        blockList = new ArrayList<Block>();
        // blockList.add(new Block(...)); // 필요한 블록들을 추가
        blockList.add(new IBlock());
    }

    @Test
    public void testNextBlockController() {
        NextBlockController controller = new NextBlockController(blockList, 3);

        // 초기 블럭 큐 상태 확인
        assertNotNull(controller.getBlock());
        assertNotNull(controller.getBlock());
        assertNotNull(controller.getBlock());

        // 블럭이 소진된 후 새로운 블럭이 생성되는지 확인
        assertNotNull(controller.getBlock());
    }
}
