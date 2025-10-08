package org.tetris.game.model;

import java.util.Map;
import javafx.scene.paint.Color;

public class GameColor {
    // 전역 색약 모드 상태
    private static boolean isColorBlind = false;

    // 색상 팔레트 (일반)
    private static final Map<BlockType, Color> normalPalette = Map.of(
            BlockType.O, Color.web("#FFFF00"), // Yellow
            BlockType.I, Color.web("#00FFFF"), // Cyan
            BlockType.S, Color.web("#008000"), // Green
            BlockType.Z, Color.web("#FF0000"), // Red
            BlockType.L, Color.web("#FFA500"), // Orange
            BlockType.J, Color.web("#0000FF"), // Blue
            BlockType.T, Color.web("#800080") // Purple
    );

    // 색상 팔레트 (색약 모드)
    private static final Map<BlockType, Color> colorBlindPalette = Map.of(
            BlockType.O, Color.web("#F0E442"),
            BlockType.I, Color.web("#56B4E9"),
            BlockType.S, Color.web("#009E73"),
            BlockType.Z, Color.web("#D55E00"),
            BlockType.L, Color.web("#E69F00"),
            BlockType.J, Color.web("#0072B2"),
            BlockType.T, Color.web("#CC79A7")
    );

    // 색상 가져오기
    public static Color getBlockColor(BlockType t) {
        return isColorBlind ? colorBlindPalette.get(t) : normalPalette.get(t);
    }

    public static Color getTextColor() {
        return isColorBlind ? Color.BLUE : Color.BLACK;
    }

    // 색약 모드 상태 변경
    public static void setColorBlind(boolean active) {
        isColorBlind = active;
    }

    // 현재 색약 모드 확인
    public static boolean isColorBlind() {
        return isColorBlind;
    }
}
