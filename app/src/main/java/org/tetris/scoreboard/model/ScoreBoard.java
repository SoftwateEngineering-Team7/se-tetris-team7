package org.tetris.scoreboard.model;

import java.io.BufferedReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;

import org.tetris.shared.BaseModel;

import javafx.collections.ObservableList;

public class ScoreBoard extends BaseModel
{
    private final static String DEFAULT_HIGH_SCORE_LIST_PATH = "src/main/java/org/tetris/scoreboard/DefaultHighScore.csv";
    private final static String ITEM_HIGH_SCORE_LIST_PATH = "src/main/java/org/tetris/scoreboard/ItemHighScore.csv";

    private String highScorePath;
    private final int maxScores;

    private ArrayList<ScoreInfo> highScoreList;
    
    /**
     * 10개의 점수를 저장하는 ScoreBoard 객체를 생성합니다.
     * 기본 경로로 설정합니다.
     */
    public ScoreBoard(){
        this(10, DEFAULT_HIGH_SCORE_LIST_PATH);
    }

    /**
     * maxScores 개수만큼의 점수를 저장하는 ScoreBoard 객체를 생성합니다.
     * @param maxScores 저장할 최대 점수 개수
     * @param highScorePath 최고 점수 파일 경로
     */
    public ScoreBoard(int maxScores, String highScorePath){
        this.highScorePath = highScorePath;
        this.maxScores = maxScores;
        highScoreList = readHighScoreList();
    }

    public ArrayList<ScoreInfo> getHighScoreList(){
        return highScoreList;
    }

    /**
     * 최고 점수 파일 경로를 설정합니다.
     * @param path 새로운 최고 점수 파일 경로
     */
    public void setHighScorePath(boolean isItemMode){
        writeHighScoreList();

        if(isItemMode){
            this.highScorePath = ITEM_HIGH_SCORE_LIST_PATH;
        }
        else{
            this.highScorePath = DEFAULT_HIGH_SCORE_LIST_PATH;
        }

        highScoreList = readHighScoreList();
    }

    /**
     * 최고 점수 리스트를 초기화합니다.
     */
    public void clear(){
        highScoreList.clear();
        writeHighScoreList();
    }

    /**
     * 새로운 점수를 삽입합니다. 점수는 내림차순으로 정렬됩니다.
     * @param scoreInfo 삽입할 점수 정보
     */
    public void insert(ScoreInfo scoreInfo){
        if(highScoreList.isEmpty()) {
            highScoreList.add(scoreInfo);
            return;
        }

        for(int i = 0; i < highScoreList.size(); i++) {
            if(scoreInfo.score() > highScoreList.get(i).score()) {
                highScoreList.add(i, scoreInfo);
                if(highScoreList.size() > maxScores) {
                    highScoreList.remove(maxScores);
                }
                return;
            }
        }
        
        if(highScoreList.size() < maxScores) {
            highScoreList.add(scoreInfo);
        }
    }

    // region I/O
    /**
     * highScorePath 경로에서 최고 점수를 읽어옵니다.
     * @return 읽어온 최고 점수 리스트
     */
    public ArrayList<ScoreInfo> readHighScoreList()
    {
        ArrayList<ScoreInfo> scoreList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(highScorePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    // 형식 (score, name, difficulty)
                    int score = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String difficulty = parts[2];
                    scoreList.add(new ScoreInfo(score, name, difficulty));
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
    public void writeHighScoreList()
    {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(highScorePath))) {
            for (ScoreInfo scoreInfo : highScoreList) {
                bw.write(scoreInfo.score() + "," + scoreInfo.name() + "," + scoreInfo.difficulty());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // endregion

    public ObservableList<ScoreInfo> getScoreList() {
        return javafx.collections.FXCollections.observableArrayList(highScoreList);
    }
}