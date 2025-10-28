package org.tetris.game.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.util.Point;

import org.tetris.game.model.blocks.*;

public class BlockTest {

    @Test
    public void testNewBlock() {
        // 블럭 생성 테스트
        Block block = new IBlock();
        String expectedBlock = 
            "1 1 1 1 \n";

        assertEquals(expectedBlock, block.toString());
    }

    @Test
    public void testGetCell()
    {
        Block block = new TBlock();
        assertEquals(6, block.getCell(1, 1));
        assertEquals(0, block.getCell(0, 0));
    }

    @Test
    public void testBlockCount()
    {
        Block block = new OBlock();
        assertEquals(4, block.getBlockCount());
    }
    
    @Test
    public void testBlockShape()
    {
        // 블럭 모양 테스트
        Block block = new LBlock();
        assertEquals(3, block.width());
        assertEquals(2, block.height());
    }

    @Test
    public void testBlockPositions()
    {
        // 블럭 좌표 리스트 테스트
        Block block = new TBlock();
        var positions = block.getBlockPositions();

        assertEquals(4, positions.size());

        assertTrue(positions.contains(new Point(0,1)));
        assertTrue(positions.contains(new Point(1,0)));
        assertTrue(positions.contains(new Point(1,1)));
        assertTrue(positions.contains(new Point(1,2)));
    }

    @Test
    public void testToPivot() {
        // 피벗 기준 좌표 변환 테스트
        Block block = new SBlock();
        Point pos = new Point(1, 2);
        Point expectedPos = new Point(0, 1);

        assertEquals(expectedPos, block.toPivot(pos));
    }

    @Test
    public void testRotateBlock() {
        // 블럭 회전 테스트
        Block block = new TBlock();
        block.rotateCW();
        String expectedBlock = 
            "6 0 \n" +
            "6 6 \n" +
            "6 0 \n";

        assertEquals(expectedBlock, block.toString());
        assertEquals(block.pivot, new Point(1, 0));

        block = new TBlock();
        block.rotateCCW();
        expectedBlock = 
            "0 6 \n" +
            "6 6 \n" +
            "0 6 \n";

        assertEquals(expectedBlock, block.toString());
        assertEquals(block.pivot, new Point(1, 1));
    }
}
