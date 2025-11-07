package org.util;

import javafx.scene.input.KeyCode;

/**
 * 키 바인딩을 관리하는 클래스
 * 사용자가 각 동작(왼쪽, 오른쪽, 위, 아래, 하드드롭)에 대해 원하는 키를 설정할 수 있습니다.
 */
public class KeyLayout {
    // 현재 설정된 키들
    private static KeyCode leftKey = KeyCode.LEFT;
    private static KeyCode rightKey = KeyCode.RIGHT;
    private static KeyCode downKey = KeyCode.DOWN;
    private static KeyCode upKey = KeyCode.UP;
    private static KeyCode hardDropKey = KeyCode.SPACE;

    /**
     * 왼쪽 이동 키 반환
     */
    public static KeyCode getLeftKey() {
        return leftKey;
    }

    /**
     * 오른쪽 이동 키 반환
     */
    public static KeyCode getRightKey() {
        return rightKey;
    }

    /**
     * 아래 이동 키 반환
     */
    public static KeyCode getDownKey() {
        return downKey;
    }

    /**
     * 위 이동 키 반환
     */
    public static KeyCode getUpKey() {
        return upKey;
    }

    /**
     * 하드 드롭 키 반환
     */
    public static KeyCode getHardDropKey() {
        return hardDropKey;
    }

    /**
     * 왼쪽 이동 키 설정
     */
    public static void setLeftKey(KeyCode key) {
        if (key != null) {
            leftKey = key;
        }
    }

    /**
     * 오른쪽 이동 키 설정
     */
    public static void setRightKey(KeyCode key) {
        if (key != null) {
            rightKey = key;
        }
    }

    /**
     * 아래 이동 키 설정
     */
    public static void setDownKey(KeyCode key) {
        if (key != null) {
            downKey = key;
        }
    }

    /**
     * 위 이동 키 설정
     */
    public static void setUpKey(KeyCode key) {
        if (key != null) {
            upKey = key;
        }
    }

    /**
     * 하드 드롭 키 설정
     */
    public static void setHardDropKey(KeyCode key) {
        if (key != null) {
            hardDropKey = key;
        }
    }

    /**
     * 하드 드롭 포함 모든 키를 한 번에 설정
     */
    public static void setKeys(KeyCode left, KeyCode right, KeyCode down, KeyCode up, KeyCode hardDrop) {
        if (left != null) leftKey = left;
        if (right != null) rightKey = right;
        if (down != null) downKey = down;
        if (up != null) upKey = up;
        if (hardDrop != null) hardDropKey = hardDrop;
    }

    /**
     * 기본 키 설정으로 초기화 (방향키 + SPACE)
     */
    public static void resetToDefault() {
        leftKey = KeyCode.LEFT;
        rightKey = KeyCode.RIGHT;
        downKey = KeyCode.DOWN;
        upKey = KeyCode.UP;
        hardDropKey = KeyCode.SPACE;
    }
}
