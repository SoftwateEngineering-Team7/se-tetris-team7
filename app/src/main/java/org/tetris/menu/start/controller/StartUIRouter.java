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
        var model = new StartMenuModel(3); // 메뉴 버튼 개수는 3개로 고정 (게임 시작, 설정, 종료)
        var scene = new Scene(view.getRoot(), 800, 600);
        var controller = view.getController(); // FXML에서 로드된 컨트롤러
        
        controller.init(this, model); // Router와 Model 주입
        
        stage.setScene(scene);
        stage.show();
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