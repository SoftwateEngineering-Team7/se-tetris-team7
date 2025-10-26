package org.tetris.scoreboard.model;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Random;

import org.junit.Test;

public class TestScoreBoard {

    private ScoreBoard scoreBoard;
    // 절대 경로 또는 작업 디렉토리 기반 경로 사용
    private final String testReadFilePath = "src/test/java/org/tetris/scoreboard/TestReadScore.csv";
    private final String testWriteFilePath = "src/test/java/org/tetris/scoreboard/TestWriteScore.csv";

    @Test
    public void testInsert() {
        scoreBoard = new ScoreBoard(2, testReadFilePath);
        scoreBoard.insert(new ScoreInfo(100, "TEST1"));
        scoreBoard.insert(new ScoreInfo(200, "TEST2"));
        scoreBoard.insert(new ScoreInfo(150, "TEST3"));

        var highScore = scoreBoard.getHighScoreList();

        assertEquals(2, highScore.size());
        assertEquals(200, highScore.get(0).score());
        assertEquals("TEST2", highScore.get(0).name());
        assertEquals(150, highScore.get(1).score());
        assertEquals("TEST3", highScore.get(1).name());
    }

    @Test
    public void testReadHighScore() {
        // 파일 존재 확인을 위한 디버그 로깅
        File file = new File(testReadFilePath);
        System.out.println("Reading file: " + file.getAbsolutePath());
        System.out.println("File exists: " + file.exists());
        
        scoreBoard = new ScoreBoard(2, testReadFilePath);
        var scores = scoreBoard.readHighScoreList();

        assertNotNull("점수 리스트가 null이 아니어야 합니다", scores);
        assertFalse("점수 리스트가 비어있지 않아야 합니다", scores.isEmpty());
        assertTrue("점수 리스트에 최소 1개 이상의 항목이 있어야 합니다", scores.size() > 0);
        
        assertEquals(100, scores.get(0).score());
        assertEquals("TEST", scores.get(0).name());
    }

    @Test
    public void testWriteHighScore() {
        // 파일 존재 확인을 위한 디버그 로깅
        File file = new File(testWriteFilePath);
        System.out.println("Writing file: " + file.getAbsolutePath());
        
        // 테스트용 임시 파일 생성 (기존 내용 초기화)
        String tempPath = "src/test/java/org/tetris/scoreboard/TestWriteScoreTemp.csv";
        File tempFile = new File(tempPath);
        
        // 새로운 ScoreBoard 생성 (빈 상태)
        scoreBoard = new ScoreBoard(2, tempPath);
        var randomScore = new Random().nextInt(1000);
        var playerName = "TEST_WRITE";

        scoreBoard.insert(new ScoreInfo(randomScore, playerName));
        scoreBoard.writeHighScoreList();

        var scores = scoreBoard.readHighScoreList();

        assertNotNull("점수 리스트가 null이 아니어야 합니다", scores);
        assertFalse("점수 리스트가 비어있지 않아야 합니다", scores.isEmpty());
        assertTrue("점수 리스트에 최소 1개 이상의 항목이 있어야 합니다", scores.size() > 0);
        
        assertEquals(randomScore, scores.get(0).score());
        assertEquals(playerName, scores.get(0).name());
        
        // 임시 파일 삭제
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}
