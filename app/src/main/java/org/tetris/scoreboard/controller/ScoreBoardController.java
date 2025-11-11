package org.tetris.scoreboard.controller;

import org.tetris.scoreboard.model.ScoreBoard;
import org.tetris.scoreboard.model.ScoreInfo;
import org.tetris.shared.BaseController;

import java.util.List;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class ScoreBoardController extends BaseController<ScoreBoard> {

    private int finishScore;
    private String currentDifficulty;
    private int insertedIndex = -1; // 삽입된 인덱스를 저장

    public ScoreBoardController(ScoreBoard scoreBoard)
    {
        super( scoreBoard );
    }

    public int getFinishScore(){
        return finishScore;
    }

    public ScoreBoard getScoreBoard(){
        return model;
    }

    private void submitCurrentScore(int score, String name)
    {
        insertedIndex = model.insert(new ScoreInfo(score, name, currentDifficulty != null ? currentDifficulty : "EASY"));
        model.writeHighScoreList();
    }

    @FXML private TableView<ScoreInfo> scoreTable;
    @FXML private TableColumn<ScoreInfo, String> nameColumn;
    @FXML private TableColumn<ScoreInfo, Integer> scoreColumn;
    @FXML private TableColumn<ScoreInfo, String> difficultyColumn;

    @FXML private VBox inputPane;
    @FXML private Text scoreText;
    @FXML private TextField nameField;
    @FXML private Button submitButton;

    @Override
    public void initialize()
    {
        // name, score, difficulty <-> ScoreInfo
        nameColumn.setCellValueFactory(cellData 
            -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().name()));
        scoreColumn.setCellValueFactory(cellData 
            -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().score()).asObject());
        difficultyColumn.setCellValueFactory(cellData 
            -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().difficulty()));
        
        // TableRow 팩토리 설정 - 삽입된 행의 배경을 노랗게 변경
        scoreTable.setRowFactory(tv -> {
            TableRow<ScoreInfo> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null && row.getIndex() == insertedIndex) {
                    row.setStyle("-fx-background-color: #6b6b2aff;");
                } else {
                    row.setStyle("");
                }
            });
            return row;
        });
        
        scoreText.setText(String.valueOf(getFinishScore()));

        updateScoreTable();
    }

    private void updateScoreTable()
    {
        scoreTable.setItems(getScoreBoard().getScoreList());
        // 테이블 갱신 후 행 스타일 다시 적용
        scoreTable.refresh();
    }

    @FXML
    private void onSubmitPressed()
    {
        String playerName = nameField.getText();

        submitCurrentScore(finishScore, playerName);

        inputPane.setVisible(false);

        updateScoreTable();
    }

    public void setFromGame(boolean fromGame, int score)
    {
        this.finishScore = score;
        scoreText.setText(String.valueOf(finishScore));

        resetInsertedIndex();
        submitButton.setDisable(false);

        if (fromGame) {
            inputPane.setVisible(true);
        } else {
            inputPane.setVisible(false);
        }
    }

    public void setDifficulty(String difficulty)
    {
        this.currentDifficulty = difficulty;
    }

    public void setItemMode(boolean isItemMode){
        model.setHighScorePath(isItemMode);
        updateScoreTable();
    }

    public void clearScoreBoard(boolean isItemMode){
        setItemMode(isItemMode);
        model.clear();
        updateScoreTable();
    }

    /**
     * 삽입된 인덱스를 리셋합니다.
     */
    public void resetInsertedIndex() {
        insertedIndex = -1;
        updateScoreTable();
    }
}