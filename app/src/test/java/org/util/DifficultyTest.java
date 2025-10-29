package org.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.tetris.game.model.NextBlockModel;

public class DifficultyTest {
    
    @Before
    public void setUp() {
        // 각 테스트 전에 기본값으로 초기화
        Difficulty.setCurrentDifficulty(Difficulty.EASY_STRING);
    }
    
    @Test
    public void testDefaultDifficulty() {
        assertEquals("기본 난이도는 EASY여야 합니다", 
                    Difficulty.EASY_STRING, Difficulty.getCurrentDifficulty());
    }
    
    @Test
    public void testSetCurrentDifficultyToEasy() {
        Difficulty.setCurrentDifficulty(Difficulty.EASY_STRING);
        assertEquals("현재 난이도는 EASY여야 합니다", 
                    Difficulty.EASY_STRING, Difficulty.getCurrentDifficulty());
        assertEquals("EASY의 속도 배율은 0.8이어야 합니다", 
                    0.8, Difficulty.getSpeedMultiplier(), 0.001);
    }
    
    @Test
    public void testSetCurrentDifficultyToNormal() {
        Difficulty.setCurrentDifficulty(Difficulty.NORMAL_STRING);
        assertEquals("현재 난이도는 NORMAL이어야 합니다", 
                    Difficulty.NORMAL_STRING, Difficulty.getCurrentDifficulty());
        assertEquals("NORMAL의 속도 배율은 1.0이어야 합니다", 
                    1.0, Difficulty.getSpeedMultiplier(), 0.001);
    }
    
    @Test
    public void testSetCurrentDifficultyToHard() {
        Difficulty.setCurrentDifficulty(Difficulty.HARD_STRING);
        assertEquals("현재 난이도는 HARD여야 합니다", 
                    Difficulty.HARD_STRING, Difficulty.getCurrentDifficulty());
        assertEquals("HARD의 속도 배율은 1.2이어야 합니다", 
                    1.2, Difficulty.getSpeedMultiplier(), 0.001);
    }

    @Test
    public void testGetEasyProbList(){
        Difficulty.setCurrentDifficulty(Difficulty.EASY_STRING);
        int[] easyProb = Difficulty.getBlockProbList();
        assertArrayEquals("EASY 난이도의 확률 리스트와 같아야 합니다.", 
                          NextBlockModel.EASY_BLOCK_PROB_LIST, easyProb);
    }

    @Test
    public void testGetNormalProbList(){
        Difficulty.setCurrentDifficulty(Difficulty.NORMAL_STRING);
        int[] normalProb = Difficulty.getBlockProbList();
        assertArrayEquals("NORMAL 난이도의 확률 리스트와 같아야 합니다.", 
                          NextBlockModel.DEFAULT_BLOCK_PROB_LIST, normalProb);
    }

    @Test
    public void testGetHardProbList(){
        Difficulty.setCurrentDifficulty(Difficulty.HARD_STRING);
        int[] hardProb = Difficulty.getBlockProbList();
        assertArrayEquals("HARD 난이도의 확률 리스트와 같아야 합니다.", 
                          NextBlockModel.HARD_BLOCK_PROB_LIST, hardProb);
    }

}
