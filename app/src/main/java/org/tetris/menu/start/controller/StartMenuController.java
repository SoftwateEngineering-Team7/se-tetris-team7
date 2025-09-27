package org.tetris.menu.start.controller;

import org.tetris.menu.start.model.StartMenuModel;

import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;

public class StartMenuController {
    @FXML
    private AnchorPane root;
    @FXML
    private Label titleLabel;
    @FXML
    private Button gameStartButton, settingButton, exitButton;

    private StartUIRouter router;
    private StartMenuModel model;
    private int focusIndex = 0;
    private ArrayList<Button> buttons = new ArrayList<>();

    // 의존성 주입 포인트
    public void setRouter(StartUIRouter router) {
        this.router = router;
    }

    public void setModel(StartMenuModel model) {
        this.model = model;
    }

    // Router가 주입을 끝낸 뒤 호출
    public void init() {
        titleLabel.setText(model.getTitle());
        titleLabel.setFont(model.getTitleFont());
        titleLabel.setTextFill(model.getTextColor());

        buttons.add(gameStartButton);
        buttons.add(settingButton);
        buttons.add(exitButton);

        gameStartButton.setText(model.getGameStartText());
        settingButton.setText(model.getSettingsText());
        exitButton.setText(model.getExitText());

        for (var btn : buttons) {
            btn.setFont(model.getButtonFont());
            btn.setTextFill(model.getTextColor());
            btn.setFocusTraversable(false);
        }

        buttons.get(focusIndex).getStyleClass().add("selected");
    }

    // 키 입력 바인딩 (Router가 Scene 만든 뒤 호출)
    public void bindInput() {
        // AnchorPane이 키 이벤트를 받을 수 있도록 설정
        root.setFocusTraversable(true);
        root.requestFocus();

        // Scene에서 키 이벤트 처리
        root.getScene().setOnKeyPressed(e -> {
            handleKey(e);
            e.consume(); // 이벤트 소비하여 다른 곳으로 전파 방지
        });
    }

    private void handleKey(KeyEvent e) {
        if (e.getCode() == KeyCode.UP)
            setSelectedButton(-1);
        else if (e.getCode() == KeyCode.DOWN)
            setSelectedButton(+1);
        else if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER)
            fire();
    }

    private void setSelectedButton(int move) {
        // 선택된 버튼에 스타일 클래스 추가
        buttons.get(focusIndex).getStyleClass().remove("selected");
        focusIndex = Math.floorMod(focusIndex + move, buttons.size());
        buttons.get(focusIndex).getStyleClass().add("selected");

        // 해제
    }

    private void fire() {
        buttons.get(focusIndex).fire();
    }

    // FXML onAction
    @FXML
    private void onGameStart() {
        router.showGamePlaceholder();
    }

    @FXML
    private void onSettings() {
        router.showSettings();
    }

    @FXML
    private void onExit() {
        router.exitGame();
    }
}
