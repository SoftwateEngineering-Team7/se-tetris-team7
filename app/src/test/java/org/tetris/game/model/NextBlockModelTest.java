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
        NextBlockModel model = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 5);
        
        // peekNext는 큐에서 블록을 제거하지 않아야 함
        Block firstPeek = model.peekNext();
        assertNotNull("첫 번째 peekNext는 null이 아니어야 합니다", firstPeek);
        
        Block secondPeek = model.peekNext();
        assertNotNull("두 번째 peekNext는 null이 아니어야 합니다", secondPeek);
        
        // peekNext는 같은 블록을 반환해야 함 (타입만 비교)
        assertEquals("peekNext는 같은 타입의 블록을 반환해야 합니다", 
                    firstPeek.getClass(), secondPeek.getClass());
    }

    @Test
    public void testPeekNextDoesNotRemove() {
        NextBlockModel model = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 5);
        
        // peekNext로 블록 확인
        Block peeked = model.peekNext();
        assertNotNull("peekNext는 블록을 반환해야 합니다", peeked);
        
        // getBlock으로 실제 블록 가져오기
        Block gotten = model.getBlock();
        assertNotNull("getBlock은 블록을 반환해야 합니다", gotten);
        
        // peekNext로 본 블록과 getBlock으로 가져온 블록의 타입이 같아야 함
        assertEquals("peekNext와 getBlock은 같은 타입의 블록을 반환해야 합니다",
                    peeked.getClass(), gotten.getClass());
    }

    @Test
    public void testPeekNextWithEmptyQueue() {
        // fillCount가 0이면 큐가 비어있고, peekNext가 자동으로 채워야 함
        NextBlockModel model = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 1);
        
        // 큐를 비우기
        model.getBlock();
        
        // peekNext는 자동으로 큐를 채워야 함
        Block peeked = model.peekNext();
        assertNotNull("peekNext는 빈 큐를 자동으로 채워야 합니다", peeked);
        assertTrue("peekNext는 Block 인스턴스를 반환해야 합니다", peeked instanceof Block);
    }

    @Test
    public void testPeekNextMultipleTimes() {
        NextBlockModel model = new NextBlockModel(NextBlockModel.DEFAULT_BLOCK_PROB_LIST, 5);
        
        // 여러 번 peekNext 호출
        Block peek1 = model.peekNext();
        Block peek2 = model.peekNext();
        Block peek3 = model.peekNext();
        
        // 모두 같은 타입이어야 함
        assertEquals("여러 번 peekNext를 호출해도 같은 블록을 반환해야 합니다",
                    peek1.getClass(), peek2.getClass());
        assertEquals("여러 번 peekNext를 호출해도 같은 블록을 반환해야 합니다",
                    peek2.getClass(), peek3.getClass());
    }
}
