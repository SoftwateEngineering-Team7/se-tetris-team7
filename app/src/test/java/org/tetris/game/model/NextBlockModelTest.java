package org.tetris.game.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.tetris.game.model.blocks.*;

public class NextBlockModelTest {

    private int[] countBlockDistribution(int[] probList, int iterations) {
        NextBlockModel model = new NextBlockModel(probList, iterations);
        int[] counts = new int[7]; // {I, J, L, O, S, T, Z}

        for (int i = 0; i < iterations; i++) {
            Block block = model.getBlock();
            if (block instanceof IBlock) counts[0]++;
            else if (block instanceof JBlock) counts[1]++;
            else if (block instanceof LBlock) counts[2]++;
            else if (block instanceof OBlock) counts[3]++;
            else if (block instanceof SBlock) counts[4]++;
            else if (block instanceof TBlock) counts[5]++;
            else if (block instanceof ZBlock) counts[6]++;
        }
        
        return counts;
    }

    private void assertBlockDistribution(int[] actual, int[] expected) {
        for (int i = 0; i < actual.length; i++) {
            assertTrue("Block " + i + " distribution failed: expected=" + expected[i] + ", actual=" + actual[i], 
                      Math.abs(actual[i] - expected[i]) < expected[i] * 0.05); // 5% 허용 오차
        }
    }

    @Test
    public void testGetBlock() {
        NextBlockModel model = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 5);
        Block block = model.getBlock();
        assertNotNull(block);
        assertTrue(block instanceof Block);
    }

    @Test
    public void testGetBlockDistribution() {
        int iterations = 21000;
        int[] actual = countBlockDistribution(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, iterations);
        int[] expected = new int[] {3000, 3000, 3000, 3000, 3000, 3000, 3000};
        
        assertBlockDistribution(actual, expected);
    }

    @Test
    public void testGetBlockDistributionEasy() {
        int iterations = 21000;
        int[] actual = countBlockDistribution(NextBlockModel.EASY_BLOCK_PROB_LIST, iterations);
        int[] expected = new int[] {3600, 2900, 2900, 2900, 2900, 2900, 2900};
        
        assertBlockDistribution(actual, expected);
    }

    @Test
    public void testGetBlockDistributionHard() {
        int iterations = 21000;
        int[] actual = countBlockDistribution(NextBlockModel.HARD_BLOCK_PROB_LIST, iterations);
        int[] expected = new int[] {2400, 3100, 3100, 3100, 3100, 3100, 3100};
        
        assertBlockDistribution(actual, expected);
    }
}
