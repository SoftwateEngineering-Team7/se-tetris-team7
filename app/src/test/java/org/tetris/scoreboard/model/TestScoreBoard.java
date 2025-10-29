package org.tetris.scoreboard.model;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class TestScoreBoard {

    private ScoreBoard scoreBoard;
    private final String testReadFilePath = "src/test/java/org/tetris/scoreboard/TestReadScore.csv";
    private final String testWriteFilePath = "src/test/java/org/tetris/scoreboard/TestWriteScore.csv";

    @Test
    public void testInsert() {
        scoreBoard = new ScoreBoard(2, testReadFilePath);
        scoreBoard.insert(new ScoreInfo(100, "TEST1", "EASY"));
        scoreBoard.insert(new ScoreInfo(200, "TEST2", "HARD"));
        scoreBoard.insert(new ScoreInfo(150, "TEST3", "MEDIUM"));

        var highScore = scoreBoard.getHighScoreList();

        assertEquals(2, highScore.size());
        assertEquals(200, highScore.get(0).score());
        assertEquals("TEST2", highScore.get(0).name());
        assertEquals("HARD", highScore.get(0).difficulty());
        assertEquals(150, highScore.get(1).score());
        assertEquals("TEST3", highScore.get(1).name());
        assertEquals("MEDIUM", highScore.get(1).difficulty());
    }

    @Test
    public void testReadHighScore() {
        scoreBoard = new ScoreBoard(2, testReadFilePath);
        var scores = scoreBoard.readHighScoreList();

        assertEquals(100, scores.get(0).score());
        assertEquals("TEST", scores.get(0).name());
        assertEquals("EASY", scores.get(0).difficulty());
    }

    @Test
    public void testWriteHighScore() {
        scoreBoard = new ScoreBoard(2, testWriteFilePath);
        var randomScore = new Random().nextInt(1000);
        var playerName = "TEST_WRITE";
        var difficulty = "HARD";

        scoreBoard.insert(new ScoreInfo(randomScore, playerName, difficulty));
        scoreBoard.writeHighScoreList();

        var scores = scoreBoard.readHighScoreList();

        assertEquals(randomScore, scores.get(0).score());
        assertEquals(playerName, scores.get(0).name());
        assertEquals(difficulty, scores.get(0).difficulty());
    }
}
