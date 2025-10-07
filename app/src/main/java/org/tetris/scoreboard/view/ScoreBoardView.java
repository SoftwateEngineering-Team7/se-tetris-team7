package org.tetris.scoreboard.view;

import java.io.IOException;

import org.tetris.scoreboard.controller.ScoreBoardController;
import org.tetris.scoreboard.model.ScoreInfo;

import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class ScoreBoardView implements Initializable{

    private static final String FXML_PATH = "/fxml/scoreBoard.fxml";

    @FXML private TableView<ScoreInfo> scoreTable;
    @FXML private TableColumn<ScoreInfo, String> nameColumn;
    @FXML private TableColumn<ScoreInfo, Integer> scoreColumn;

    @FXML private AnchorPane inputPane;
    @FXML private Text scoreText;
    @FXML private TextField nameField;
    @FXML private Button submitButton;

    private final ScoreBoardController controller = new ScoreBoardController(0);

    private final Scene scene;
    private final Parent root; 

    /**
     * ScoreBoardView 객체를 생성합니다.
     * FXML 파일을 로드하고 Scene을 초기화합니다.
     */
    public ScoreBoardView(){

        root = loadFXML();
        scene = new Scene(root);
        System.out.println("ScoreBoardView constructor called");
    }

    private Parent loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
            loader.setController(this);
            return loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load FXML: " + FXML_PATH, e);
        }
    }

    public Scene getScene() {
        return scene;
    }

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources)
    {
        // name, score <-> ScoreInfo
        nameColumn.setCellValueFactory(cellData 
            -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().name()));
        scoreColumn.setCellValueFactory(cellData 
            -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().score()).asObject());
        
        scoreText.setText(String.valueOf(controller.getFinishScore()));
        
        scoreTable.setItems(controller.getScoreBoard().getScoreList());
    }

    @FXML
    private void onSubmitPressed()
    {
        String playerName = nameField.getText();
        controller.OnSubmitClick(playerName);
    }
    
}
