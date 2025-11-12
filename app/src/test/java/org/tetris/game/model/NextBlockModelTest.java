package org.tetris.game.model;

import static org.junit.Assert.*;

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

    @Test
    public void testPeekNext() {
        NextBlockModel model = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 1);
        
        // peekNext는 null이 아닌 Block을 반환해야 함
        Block peeked = model.peekNext();
        assertNotNull("peekNext는 null이 아니어야 합니다", peeked);
        assertTrue("peekNext는 Block 인스턴스를 반환해야 합니다", peeked instanceof Block);
        
        model.getBlock();
        
        // peekNext는 자동으로 채워짐
        Block firstPeeked = model.peekNext();
        assertNotNull("peekNext는 null이 아니어야 합니다", firstPeeked);
        assertTrue("peekNext는 Block 인스턴스를 반환해야 합니다", firstPeeked instanceof Block);

        // peekNext는 여러 번 호출해도 같은 블록을 반환해야 함
        Block secondPeek = model.peekNext();
        assertEquals("peekNext를 여러 번 호출해도 같은 블록을 반환해야 합니다",
                    firstPeeked.getClass(), secondPeek.getClass());

        // peekNext 후 getBlock으로 가져온 블록이 같아야 함 (큐에서 제거되지 않았음을 확인)
        Block gotten = model.getBlock();
        assertEquals("peekNext와 getBlock은 같은 블록을 반환해야 합니다",
                    firstPeeked.getClass(), gotten.getClass());
    }

    @Test
    public void testSwapNext()
    {
        NextBlockModel model = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 5);
        
        // 원래 다음 블록 확인
        Block originalNext = model.peekNext();
        assertNotNull("원래 다음 블록이 null이 아니어야 합니다", originalNext);
        
        // 새로운 블록으로 교체
        Block newBlock = new IBlock();
        model.swapNext(newBlock);
        
        // 교체된 블록이 peekNext로 반환되는지 확인
        Block swappedNext = model.peekNext();
        assertEquals("swapNext 후 peekNext는 교체된 블록을 반환해야 합니다", 
                    newBlock.getClass(), swappedNext.getClass());
        
        // getBlock으로도 교체된 블록이 반환되는지 확인
        Block gotten = model.getBlock();
        assertEquals("swapNext 후 getBlock은 교체된 블록을 반환해야 합니다",
                    newBlock.getClass(), gotten.getClass());
        
        // 빈 큐에서 swapNext 테스트
        NextBlockModel emptyModel = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 1);
        emptyModel.getBlock();
        
        Block testBlock = new TBlock();
        emptyModel.swapNext(testBlock); // 빈 큐에서도 정상 동작해야 함
        
        Block result = emptyModel.peekNext();
        assertEquals("빈 큐에서 swapNext 후 peekNext는 교체된 블록을 반환해야 합니다",
                    testBlock.getClass(), result.getClass());
    }
}
