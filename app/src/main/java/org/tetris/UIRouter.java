package org.tetris;

import org.tetris.menu.start.controller.StartMenuController;
import org.tetris.menu.start.model.StartMenuModel;
import org.tetris.menu.start.view.StartMenuView;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class UIRouter {

    private final Stage stage;
    private StartMenuView startMenuView;
    private StartMenuModel startMenuModel;
    private StartMenuController startMenuController;
    private Scene startMenuScene;

    // 기본 생성자: 기존대로 내부에서 직접 구성
    public UIRouter(Stage stage) {
        this.stage = stage;
        stage.setTitle("Tetris");
        stage.setResizable(true);

        // Start Menu 컴포넌트들을 생성자에서 미리 초기화
        this.startMenuView = new StartMenuView();
        this.startMenuModel = new StartMenuModel(3); // 메뉴 버튼 3개 (게임 시작, 설정, 종료)
        this.startMenuController = startMenuView.getController(); // FXML에서 로드된 컨트롤러
        this.startMenuScene = new Scene(startMenuView.getRoot(), 800, 600);

        // Controller 초기화 (Router와 Model 주입)
        startMenuController.init(this, startMenuModel);
        // startMenuController.bindScoreboard(scoreboardModel.topScoresProperty()); // 필요시 스코어보드 바인딩
    }

    // 테스트/주입 친화 생성자: 외부에서 미리 준비한 인스턴스를 사용
    public UIRouter(Stage stage,
            StartMenuView view,
            StartMenuModel model,
            StartMenuController controller) {
        this.stage = stage;
        stage.setTitle("Tetris");
        stage.setResizable(true);

        this.startMenuView = view;
        this.startMenuModel = model;
        this.startMenuController = controller;
        this.startMenuScene = new Scene(startMenuView.getRoot(), 800, 600);

        startMenuController.init(this, startMenuModel);
    }

    public void showStartMenu() {
        stage.setScene(startMenuScene);
        stage.show();
        startMenuController.bindInput();
    }

    public void showSettings() {
        
    }

    public void showGamePlaceholder() {
        
    }

    public void exitGame() {
        stage.close();
    }
}
