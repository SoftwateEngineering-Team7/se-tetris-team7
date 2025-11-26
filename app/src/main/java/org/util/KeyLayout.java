package org.util;

import javafx.scene.input.KeyCode;

public class KeyLayout {
    public static final String KEY_ARROWS = "ARROWS";
    public static final String KEY_WASD = "WASD";

    private static String currentLayout = KEY_ARROWS; // 기본값

    private final String name;
    private final KeyCode leftKey;
    private final KeyCode rightKey;
    private final KeyCode downKey;
    private final KeyCode upKey;
    private final KeyCode hardDropKey;

    private KeyLayout(String name, KeyCode left, KeyCode right, KeyCode down, KeyCode up, KeyCode hardDrop) {
        this.name = name;
        this.leftKey = left;
        this.rightKey = right;
        this.downKey = down;
        this.upKey = up;
        this.hardDropKey = hardDrop;
    }


    public KeyCode getHardDrop() {
        return hardDropKey;
    }

    public KeyCode getLeft() {
        return leftKey;
    }

    public KeyCode getRight() {
        return rightKey;
    }

    public KeyCode getDown() {
        return downKey;
    }

    public KeyCode getUp() {
        return upKey;
    }

    /**
     * 현재 설정된 레이아웃의 키 코드를 반환
     */
    public static KeyCode getLeftKey() {
        return currentLayout.equals(KEY_ARROWS) ? ARROWS.leftKey : WASD.leftKey;
    }

    public static KeyCode getRightKey() {
        return currentLayout.equals(KEY_ARROWS) ? ARROWS.rightKey : WASD.rightKey;
    }

    public static KeyCode getDownKey() {
        return currentLayout.equals(KEY_ARROWS) ? ARROWS.downKey : WASD.downKey;
    }

    public static KeyCode getUpKey() {
        return currentLayout.equals(KEY_ARROWS) ? ARROWS.upKey : WASD.upKey;
    }

    public static KeyCode getHardDropKey() {
        return currentLayout.equals(KEY_ARROWS) ? ARROWS.hardDropKey : WASD.hardDropKey;
    }

    /**
     * 레이아웃 설정
     * 
     * @param layout 레이아웃 이름 ("ARROWS" 또는 "WASD")
     */
    public static void setCurrentLayout(String layout) {
        if (layout != null && (layout.equals(KEY_ARROWS) || layout.equals(KEY_WASD))) {
            currentLayout = layout;
        }
    }

    /**
     * 현재 레이아웃 이름 반환
     */
    public static String getCurrentLayout() {
        return currentLayout;
    }

    public String getName() {
        return name;
    }

    // region 레이아웃 정의

    public static final KeyLayout ARROWS = new KeyLayout(KEY_ARROWS, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.DOWN,
            KeyCode.UP, KeyCode.SLASH);
    public static final KeyLayout WASD = new KeyLayout(KEY_WASD, KeyCode.A, KeyCode.D, KeyCode.S, KeyCode.W,
            KeyCode.SPACE);

    // endregion
}
