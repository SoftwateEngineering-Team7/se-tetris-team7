package org.tetris.menu.start.controller;


import org.tetris.menu.start.model.StartMenuModel;
import org.tetris.menu.start.view.StartMenuView;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartUIRouter {
    private final Stage stage;

    public StartUIRouter(Stage stage) {
        this.stage = stage;
        stage.setTitle("Tetris");
        stage.setResizable(false);
    }

    public void showStartMenu() {
        var view = new StartMenuView();
        var controller = view.getController(); // FXML에서 로드된 컨트롤러

        controller.setRouter(this);
        controller.setModel(new StartMenuModel());
        controller.init();

        var scene = new Scene(view.getRoot(), 800, 600);
        stage.setScene(scene);
        stage.show();

        // Scene이 설정된 후에 키 입력 바인딩
        controller.bindInput();
    }

    public void showSettings() {
        // System.out.println("설정 화면 (미구현)");
    }

    public void showGamePlaceholder() {
        // System.out.println("게임 화면 (미구현)");
    }

    public void exitGame() {
        stage.close();
    }
}