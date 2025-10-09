package org.tetris.scoreboard.controller;

import org.tetris.scoreboard.model.ScoreBoard;
import org.tetris.scoreboard.model.ScoreInfo;
import org.tetris.shared.BaseController;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class ScoreBoardController extends BaseController<ScoreBoard> {

    private int finishScore;

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

    /**
     * 제출 버튼 클릭 시 호출되는 메서드입니다.
     * @param name 플레이어 이름
     */
    public void OnSubmitClick(String name)
    {
        submitCurrentScore(finishScore, name);
    }

    private void submitCurrentScore(int score, String name)
    {
        model.insert(new ScoreInfo(score, name));
        model.writeHighScoreList();
    }

    @FXML private TableView<ScoreInfo> scoreTable;
    @FXML private TableColumn<ScoreInfo, String> nameColumn;
    @FXML private TableColumn<ScoreInfo, Integer> scoreColumn;

    @FXML private AnchorPane inputPane;
    @FXML private Text scoreText;
    @FXML private TextField nameField;
    @FXML private Button submitButton;


    @Override
    public void initialize()
    {
        // name, score <-> ScoreInfo
        nameColumn.setCellValueFactory(cellData 
            -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().name()));
        scoreColumn.setCellValueFactory(cellData 
            -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().score()).asObject());
        
        scoreText.setText(String.valueOf(getFinishScore()));

        scoreTable.setItems(getScoreBoard().getScoreList());
    }

    @FXML
    private void onSubmitPressed()
    {
        String playerName = nameField.getText();
        OnSubmitClick(playerName);
    }
}