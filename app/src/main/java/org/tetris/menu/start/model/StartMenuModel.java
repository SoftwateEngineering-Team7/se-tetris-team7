package org.tetris.menu.start.model;

import org.tetris.game.model.GameColor;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class StartMenuModel {
    public String getTitle() {
        return "Tetris";
    }

    public String getGameStartText() {
        return "게임 시작";
    }

    public String getSettingsText() {
        return "설정";
    }

    public String getExitText() {
        return "종료";
    }

    public Font getTitleFont() {
        return new Font("Arial", 60);
    }

    public Font getButtonFont() {
        return new Font("Arial", 20);
    }

    public Color getTextColor() {
        return GameColor.getTextColor();
    }
}