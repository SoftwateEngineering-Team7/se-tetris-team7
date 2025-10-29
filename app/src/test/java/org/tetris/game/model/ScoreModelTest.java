package org.tetris.game.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.util.Difficulty;

public class ScoreModelTest {
    
    @Before
    public void setUp() {
        // 모든 테스트 전에 난이도를 EASY로 설정
        Difficulty.setCurrentDifficulty(Difficulty.EASY_STRING);
    }
    
    @Test
    public void testInitialScore() {
        ScoreModel scoreModel = new ScoreModel();
        assertEquals(0, scoreModel.getScore());
    }

    @Test
    public void testAddScore() {
        ScoreModel scoreModel = new ScoreModel();
        scoreModel.add(500);
        assertEquals(500, scoreModel.getScore());
        scoreModel.add(300);
        assertEquals(800, scoreModel.getScore());
    }

    @Test
    public void testLineCleared() {
        ScoreModel scoreModel = new ScoreModel();
        scoreModel.lineCleared(0);
        assertEquals(0, scoreModel.getScore());

        scoreModel.lineCleared(1);
        assertEquals(1000, scoreModel.getScore());

        scoreModel.lineCleared(2);
        assertEquals(4000, scoreModel.getScore());

        scoreModel.lineCleared(3);
        assertEquals(9000, scoreModel.getScore());

        scoreModel.lineCleared(4);
        assertEquals(17000, scoreModel.getScore());

        scoreModel.lineCleared(5);
        assertEquals(25000, scoreModel.getScore());
    }

    @Test
    public void testReset() {
        ScoreModel scoreModel = new ScoreModel();
        scoreModel.add(5000);
        scoreModel.lineCleared(3);
        
        assertEquals(10000, scoreModel.getScore());
        
        scoreModel.reset();
        
        assertEquals(0, scoreModel.getScore());
    }

    @Test
    public void testItemActivated() {
        ScoreModel scoreModel = new ScoreModel();
        scoreModel.itemActivated();
        assertEquals(1000, scoreModel.getScore());
        
        scoreModel.itemActivated();
        assertEquals(2000, scoreModel.getScore());
    }

    @Test
    public void testBlockDroppedEasy() {
        Difficulty.setCurrentDifficulty(Difficulty.EASY_STRING);
        ScoreModel scoreModel = new ScoreModel();
        // EASY 난이도: easyBlockDropScore(1) * softDropMultiplier(1) * gravityMultiplier(1) = 1
        scoreModel.blockDropped();
        assertEquals(1, scoreModel.getScore());
        
        scoreModel.blockDropped();
        scoreModel.blockDropped();
        assertEquals(3, scoreModel.getScore());
    }

    @Test
    public void testBlockDroppedNormal() {
        Difficulty.setCurrentDifficulty(Difficulty.NORMAL_STRING);
        ScoreModel scoreModel = new ScoreModel();
        // NORMAL 난이도: mediumBlockDropScore(2) * softDropMultiplier(1) * gravityMultiplier(1) = 2
        scoreModel.blockDropped();
        assertEquals(2, scoreModel.getScore());
        
        scoreModel.blockDropped();
        scoreModel.blockDropped();
        assertEquals(6, scoreModel.getScore());
    }

    @Test
    public void testBlockDroppedHard() {
        Difficulty.setCurrentDifficulty(Difficulty.HARD_STRING);
        ScoreModel scoreModel = new ScoreModel();
        // HARD 난이도: hardBlockDropScore(3) * softDropMultiplier(1) * gravityMultiplier(1) = 3
        scoreModel.blockDropped();
        assertEquals(3, scoreModel.getScore());
        
        scoreModel.blockDropped();
        scoreModel.blockDropped();
        assertEquals(9, scoreModel.getScore());
    }

    @Test
    public void testSoftDropEasy() {
        Difficulty.setCurrentDifficulty(Difficulty.EASY_STRING);
        ScoreModel scoreModel = new ScoreModel();
        // EASY 난이도: easyBlockDropScore(1) * softDropMultiplier(1) * distance * gravityMultiplier(1)
        scoreModel.softDrop(5);
        assertEquals(5, scoreModel.getScore()); // 1 * 1 * 5 * 1 = 5
        
        scoreModel.softDrop(3);
        assertEquals(8, scoreModel.getScore()); // 5 + (1 * 1 * 3 * 1) = 8
    }

    @Test
    public void testSoftDropNormal() {
        Difficulty.setCurrentDifficulty(Difficulty.NORMAL_STRING);
        ScoreModel scoreModel = new ScoreModel();
        // NORMAL 난이도: mediumBlockDropScore(2) * softDropMultiplier(1) * distance * gravityMultiplier(1)
        scoreModel.softDrop(5);
        assertEquals(10, scoreModel.getScore()); // 2 * 1 * 5 * 1 = 10
        
        scoreModel.softDrop(3);
        assertEquals(16, scoreModel.getScore()); // 10 + (2 * 1 * 3 * 1) = 16
    }

    @Test
    public void testSoftDropHard() {
        Difficulty.setCurrentDifficulty(Difficulty.HARD_STRING);
        ScoreModel scoreModel = new ScoreModel();
        // HARD 난이도: hardBlockDropScore(3) * softDropMultiplier(1) * distance * gravityMultiplier(1)
        scoreModel.softDrop(5);
        assertEquals(15, scoreModel.getScore()); // 3 * 1 * 5 * 1 = 15
        
        scoreModel.softDrop(3);
        assertEquals(24, scoreModel.getScore()); // 15 + (3 * 1 * 3 * 1) = 24
    }

    @Test
    public void testToString() {
        ScoreModel scoreModel = new ScoreModel();
        assertEquals("00000000", scoreModel.toString());
        
        scoreModel.add(1234);
        assertEquals("00001234", scoreModel.toString());
        
        scoreModel.add(98766);
        assertEquals("00100000", scoreModel.toString());
    }

    @Test
    public void testAddNegativeScore() {
        ScoreModel scoreModel = new ScoreModel();
        scoreModel.add(1000);
        scoreModel.add(-500); // 음수는 무시되어야 함
        
        assertEquals(1000, scoreModel.getScore());
    }
}