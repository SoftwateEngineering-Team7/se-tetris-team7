package org.util;

import javafx.scene.paint.Color;

public class GameColor {

    private static boolean colorBlind = false;
    
    private Color color;
    private Color blindColor;

    private GameColor(Color color, Color blindColor) {
        this.color = color;
        this.blindColor = blindColor;
    }

    /**
     * 색맹 모드일 경우 색맹용 색상을, 아닐 경우 일반 색상을 반환
     * @return 색상(Color)
     */
    public Color getColor() {
        return colorBlind ? blindColor : color;
    }

    /**
     * 색맹 모드 설정
     * @param colorBlind 색맹 모드 여부 (true: 색맹 모드, false: 일반 모드)
     */
    public static void setColorBlind(boolean colorBlind) {
        GameColor.colorBlind = colorBlind;
    }

    // 테스트 용 추가
    public static boolean getColorBlind() {
        return colorBlind;
    }


    // region 색상 정의

    // #FFFF00
    public final static GameColor YELLOW = new GameColor(Color.YELLOW, Color.web("#F0E442"));
    // #00FFFF
    public final static GameColor CYAN = new GameColor(Color.CYAN, Color.web("#56B4E9"));
    // #008000
    public final static GameColor GREEN = new GameColor(Color.GREEN, Color.web("#009E73"));
    // #FF0000
    public final static GameColor RED = new GameColor(Color.RED, Color.web("#D55E00"));
    // #FFA500
    public final static GameColor ORANGE = new GameColor(Color.ORANGE, Color.web("#E69F00"));
    // #0000FF
    public final static GameColor BLUE = new GameColor(Color.BLUE, Color.web("#0072B2"));
    // #800080
    public final static GameColor PURPLE = new GameColor(Color.PURPLE, Color.web("#CC79A7"));

    // endregion
}