package org.tetris.game.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class ScoreModelTest {
    
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