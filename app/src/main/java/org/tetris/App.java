package org.tetris;


import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        Router nav = new Router(stage);
        nav.showStartMenu(); // 시작 화면 표시
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}