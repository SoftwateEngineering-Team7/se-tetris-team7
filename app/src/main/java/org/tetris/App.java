package org.tetris;

import org.tetris.menu.start.controller.StartUIRouter;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        StartUIRouter nav = new StartUIRouter(stage);
        nav.showStartMenu(); // 시작 화면 표시
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}