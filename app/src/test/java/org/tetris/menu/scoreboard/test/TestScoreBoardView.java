package org.tetris.menu.scoreboard.test;

import javafx.application.Application;
import javafx.stage.Stage;
import org.tetris.scoreboard.view.ScoreBoardView;

public class TestScoreBoardView extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        System.out.println("Starting ScoreBoard Test Application...");
        
        try {
            // ScoreBoardView 인스턴스 생성 (FXML 자동 로드됨)
            ScoreBoardView scoreBoardView = new ScoreBoardView();
            
            // Scene 설정
            stage.setScene(scoreBoardView.getScene());
            stage.setTitle("ScoreBoard Test Window");
            stage.setResizable(false);
            stage.show();
            
            System.out.println("ScoreBoard window opened successfully!");
            
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        System.out.println("Launching ScoreBoard Test...");
        launch(args);
    }
}