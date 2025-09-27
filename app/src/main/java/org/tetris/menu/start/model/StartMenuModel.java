package org.tetris.menu.start.model;

import org.tetris.game.model.GameColor;

import javafx.geometry.Dimension2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class StartMenuModel {
    private String titleText;
    private String gameStartButtonText;
    private String settingButtonText;
    private String exitButtonText;

    private int titleWidth;
    private int titleHeight;
    private int buttonWidth;
    private int buttonHeight;

    Font titleFont;
    Font buttonFont;
    // 추가적인 상태 변수들...

    public StartMenuModel() {
        this.titleText = "Tetris";
        this.gameStartButtonText = "게임 시작";
        this.settingButtonText = "설정";
        this.exitButtonText = "종료";

        this.titleWidth = 400;
        this.titleHeight = 200;
        this.buttonWidth = 200;
        this.buttonHeight = 50;

        this.titleFont = new Font("Arial", 60);
        this.buttonFont = new Font("Arial", 20);
        // 초기화 코드...
    }

    // Text Getters
    public String getTitleText() {
        return titleText;
    }

    public String getGameStartButtonText() {
        return gameStartButtonText;
    }

    public String getSettingButtonText() {
        return settingButtonText;
    }

    public String getExitButtonText() {
        return exitButtonText;
    }

    // Size Getters
    public Dimension2D getTitleSize() {
        return new Dimension2D(titleWidth, titleHeight);
    }

    public Dimension2D getButtonSize() {
        return new Dimension2D(buttonWidth, buttonHeight);
    }

    // Font & Color Getters
    public Font getTitleFont() {
        return titleFont;
    }

    public Font getButtonFont() {
        return buttonFont;
    }

    public Color getTextColor() {
        return GameColor.getTextColor();
    }
}