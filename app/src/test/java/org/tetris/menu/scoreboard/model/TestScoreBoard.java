package org.tetris.menu.scoreboard.model;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import org.tetris.scoreboard.model.ScoreBoard;
import org.tetris.scoreboard.model.ScoreInfo;

public class TestScoreBoard {

    private ScoreBoard scoreBoard;
    private final String testReadFilePath = "src/test/java/org/tetris/menu/scoreboard/TestReadScore.csv";
    private final String testWriteFilePath = "src/test/java/org/tetris/menu/scoreboard/TestWriteScore.csv";

    @Test
    public void testInsert() {
        scoreBoard = new ScoreBoard(2, testReadFilePath);
        scoreBoard.insert(new ScoreInfo(100, "TEST1"));
        scoreBoard.insert(new ScoreInfo(200, "TEST2"));
        scoreBoard.insert(new ScoreInfo(150, "TEST3"));

        var highScore = scoreBoard.getHighScore();

        assertEquals(2, highScore.size());
        assertEquals(200, highScore.get(0).score());
        assertEquals("TEST2", highScore.get(0).name());
        assertEquals(150, highScore.get(1).score());
        assertEquals("TEST3", highScore.get(1).name());
    }

    @Test
    public void testReadHighScore() {
        scoreBoard = new ScoreBoard(2, testReadFilePath);
        var scores = scoreBoard.readHighScore();

        assertEquals(100, scores.get(0).score());
        assertEquals("TEST", scores.get(0).name());
    }

    @Test
    public void testWriteHighScore() {
        scoreBoard = new ScoreBoard(2, testWriteFilePath);
        var randomScore = new Random().nextInt(1000);
        var playerName = "TEST_WRITE";

        scoreBoard.insert(new ScoreInfo(randomScore, playerName));
        scoreBoard.writeHighScore();

        var scores = scoreBoard.readHighScore();

        assertEquals(randomScore, scores.get(0).score());
        assertEquals(playerName, scores.get(0).name());
    }
}
