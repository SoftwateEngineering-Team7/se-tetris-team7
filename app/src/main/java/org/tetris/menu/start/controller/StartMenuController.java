package org.tetris.menu.start.controller;

import org.tetris.menu.start.model.StartMenuModel;

import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class StartMenuController {
    @FXML
    private AnchorPane root;
    @FXML
    private Label titleLabel;
    @FXML
    private Button gameStartButton, settingButton, exitButton;
    @FXML
    private Label wrongInputLabel; // 잘못된 키 입력 안내 라벨

    private StartUIRouter router;
    private StartMenuModel model;
    private int focusIndex = 0;
    private ArrayList<Button> buttons = new ArrayList<>();
    private Timeline hideMessageTimeline; // 메시지 숨기기 타이머

    // Router가 주입을 끝낸 뒤 호출
    public void init(StartUIRouter router, StartMenuModel model) {
        this.router = router;
        this.model = model; // model 설정 추가

        buttons.add(gameStartButton);
        buttons.add(settingButton);
        buttons.add(exitButton);

        for (var btn : buttons) {
            btn.setFocusTraversable(false);
        }

        buttons.get(focusIndex).getStyleClass().add("highlighted"); // 첫 버튼 선택 표시
    }

    // 키 입력 바인딩 (Router가 Scene 만든 뒤 호출)
    public void bindInput() {
        // Scene이 null이 아닌지 확인
        if (root.getScene() == null) {
            System.err.println("Warning: Scene is null when trying to bind input");
            return;
        }

        root.setFocusTraversable(true);
        root.requestFocus();

        // Scene 레벨에서 키 이벤트 처리 (더 안정적)
        root.getScene().setOnKeyPressed(e -> {
            handleKey(e);
            e.consume(); // 이벤트 소비하여 다른 곳으로 전파 방지
        }); 
    }

    private void handleKey(KeyEvent e) {
        if (e.getCode() == KeyCode.UP)
            setHightlightedButton(-1);
        else if (e.getCode() == KeyCode.DOWN)
            setHightlightedButton(+1);
        else if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER)
            fire();
        else
            showWrongInputLabel();
    }

    private void showWrongInputLabel() {
        wrongInputLabel.setVisible(true);

        // 기존 타이머가 실행 중이면 중지
        if (hideMessageTimeline != null) {
            hideMessageTimeline.stop();
        }

        // 새로운 타이머 시작 (마지막 잘못된 입력에서 2초)
        hideMessageTimeline = new Timeline(
                new KeyFrame(Duration.seconds(2), e -> wrongInputLabel.setVisible(false)));
        hideMessageTimeline.play();
    }


    private void setHightlightedButton(int move) {
        // 선택된 버튼에 스타일 클래스 추가
        buttons.get(model.getSelectedIndex()).getStyleClass().remove("highlighted");
        model.move(move);
        buttons.get(model.getSelectedIndex()).getStyleClass().add("highlighted");
    }

    private void fire() {
        buttons.get(model.getSelectedIndex()).fire();
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
