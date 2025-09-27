package org.tetris.menu.start.view;


import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class StartMenuView {
    private final AnchorPane root;
    private Label titleLabel;

    private Button gameStartButton;
    private Button settingButton;
    private Button exitButton;

    private int buttonIndex;

    public StartMenuView() {
        root = new AnchorPane();

        titleLabel = new Label("게임 제목");
        gameStartButton = new Button("게임 시작");
        settingButton = new Button("설정");
        exitButton = new Button("종료");

        // 타이틀은 위쪽 중앙에
        AnchorPane.setTopAnchor(titleLabel, 20.0);
        AnchorPane.setLeftAnchor(titleLabel, 0.0);
        AnchorPane.setRightAnchor(titleLabel, 0.0);
        titleLabel.setAlignment(Pos.CENTER);

        // 버튼 박스는 중앙에
        VBox menuBox = new VBox(40, gameStartButton, settingButton, exitButton);
        menuBox.setAlignment(Pos.CENTER);
        AnchorPane.setTopAnchor(menuBox, 300.0); // 위에서 300px 내려오기
        AnchorPane.setLeftAnchor(menuBox, 0.0);
        AnchorPane.setRightAnchor(menuBox, 0.0);

        root.getChildren().addAll(titleLabel, menuBox);

        buttonIndex = 0;
        gameStartButton.requestFocus();
    }

    public AnchorPane getRoot() {
        return root;
    }

    // Size Setter
    public void setRootSize(int width, int height) {
        root.setPrefSize(width, height);
    }

    public void setTitleSize(int width, int height) {
        titleLabel.setPrefSize(width, height);
    }

    public void setButtonSize(int width, int height) {
        gameStartButton.setPrefSize(width, height);
        settingButton.setPrefSize(width, height);
        exitButton.setPrefSize(width, height);
    }

    // Text Setters
    public void setTitleText(String title) {
        titleLabel.setText(title);
    }

    public void setGameStartButtonText(String text) {
        gameStartButton.setText(text);
    }

    public void setSettingButtonText(String text) {
        settingButton.setText(text);
    }

    public void setExitButtonText(String text) {
        exitButton.setText(text);
    }

    public void setTitleFont(Font font) {
        titleLabel.setFont(font);
    }

    public void setButtonFont(Font font) {
        gameStartButton.setFont(font);
        settingButton.setFont(font);
        exitButton.setFont(font);
    }

    // Text Color Setter

    public void setTextColor(Color color) {
        titleLabel.setTextFill(color);
        gameStartButton.setTextFill(color);
        settingButton.setTextFill(color);
        exitButton.setTextFill(color);
    }

    // Action Handlers

    public void setActionHandlers(Runnable onGameStart, Runnable onSettings, Runnable onExit) {
        gameStartButton.setMouseTransparent(true);
        settingButton.setMouseTransparent(true);
        exitButton.setMouseTransparent(true);

        gameStartButton.setOnAction(e -> onGameStart.run());
        settingButton.setOnAction(e -> onSettings.run());
        exitButton.setOnAction(e -> onExit.run());
    }

    // Button Navigation
    public void setFocusButton(int move) {
        buttonIndex = (buttonIndex + move + 3) % 3;

        switch (buttonIndex) {
            case 0 -> gameStartButton.requestFocus();
            case 1 -> settingButton.requestFocus();
            case 2 -> exitButton.requestFocus();
        }
    }

    public void buttonFire() {
        switch (buttonIndex) {
            case 0 -> gameStartButton.fire();
            case 1 -> settingButton.fire();
            case 2 -> exitButton.fire();
        }
    }
}
