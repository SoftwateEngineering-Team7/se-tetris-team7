package org.tetris.menu.start.controller;


import org.tetris.menu.start.model.StartMenuModel;
import org.tetris.menu.start.view.StartMenuView;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class UIRouter {
    private final Stage stage;

    public UIRouter(Stage stage) {
        this.stage = stage;
        stage.setTitle("Tetris");
        stage.setResizable(false);
    }

    public void showStartMenu() {
        StartMenuModel model = new StartMenuModel();
        StartMenuView view = new StartMenuView();

        // 전환 콜백만 컨트롤러에 전달
        StartMenuController controller = new StartMenuController(
                model,
                view,
                this);

        Scene scene = new Scene(view.getRoot(), 800, 600);
        stage.setScene(scene);
        controller.bindInput(scene); // 키 입력 바인딩
    }

    public void showSettings() {
        // SettingsModel model = new SettingsModel(); // 있으면 사용, 없으면 임시
        // SettingsView view = new SettingsView(); // 간단한 설정 화면이라고 가정
        // SettingsController controller = new SettingsController(
        // model, view, this::showStartMenu // 뒤로가기 콜백
        // );

        // Scene scene = new Scene(view.getRoot(), 800, 600);
        // stage.setScene(scene);
        // controller.bindInput(scene);
    }

    // 게임 씬 미구현 → 임시 화면
    public void showGamePlaceholder() {
        // GamePlaceholderView view = new GamePlaceholderView(); // 간단한 VBox/Label로
        // 구성했다고 가정
        // Scene scene = new Scene(view.getRoot(), 800, 600);
        // stage.setScene(scene);
    }

    public void exitGame() {
        stage.close();
    }
}