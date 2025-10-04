package org.tetris.scoreboard.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;

public class ScoreBoard
{
    private final static String DEFAULT_HIGH_SCORE_PATH = "src/main/java/org/tetris/scoreboard/HighScore.csv";  

    private final String highScorePath;
    private final int maxScores;

    private ArrayList<ScoreInfo> highScore;
    
    /**
     * 10개의 점수를 저장하는 ScoreBoard 객체를 생성합니다.
     * 기본 경로로 설정합니다.
     */
    public ScoreBoard(){
        this(10, DEFAULT_HIGH_SCORE_PATH);
    }

    /**
     * maxScores 개수만큼의 점수를 저장하는 ScoreBoard 객체를 생성합니다.
     * @param maxScores 저장할 최대 점수 개수
     * @param highScorePath 최고 점수 파일 경로
     */
    public ScoreBoard(int maxScores, String highScorePath){
        this.highScorePath = highScorePath;
        this.maxScores = maxScores;
        highScore = new ArrayList<>();
    }

    public ArrayList<ScoreInfo> getHighScore(){
        return highScore;
    }

    /**
     * 새로운 점수를 삽입합니다. 점수는 내림차순으로 정렬됩니다.
     * @param scoreInfo 삽입할 점수 정보
     */
    public void insert(ScoreInfo scoreInfo){
        if(highScore.isEmpty()) {
            highScore.add(scoreInfo);
            return;
        }

        for(int i = 0; i < highScore.size(); i++) {
            if(scoreInfo.score() > highScore.get(i).score()) {
                highScore.add(i, scoreInfo);
                if(highScore.size() > maxScores) {
                    highScore.remove(maxScores);
                }
                return;
            }
        }
    }

    // region I/O
    /**
     * highScorePath 경로에서 최고 점수를 읽어옵니다.
     * @return 읽어온 최고 점수 리스트
     */
    public ArrayList<ScoreInfo> readHighScore()
    {
        ArrayList<ScoreInfo> scoreList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(highScorePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    int score = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    scoreList.add(new ScoreInfo(score, name));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scoreList;
    }

    /**
     * 현재 최고 점수 리스트를 highScorePath 경로에 저장합니다.
     */
    public void writeHighScore()
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(highScorePath))) {
            for (ScoreInfo scoreInfo : highScore) {
                bw.write(scoreInfo.score() + "," + scoreInfo.name());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // endregion
}