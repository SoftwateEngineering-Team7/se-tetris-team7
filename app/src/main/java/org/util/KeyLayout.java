package org.util;

import javafx.scene.input.KeyCode;
import java.util.HashMap;
import java.util.Map;

/**
 * 키 바인딩을 관리하는 클래스 (Player 1, Player 2 지원)
 * 각 플레이어별로 독립적인 키 설정을 관리합니다.
 */

public class KeyLayout {
    // Player 1 키 맵

    private static final Map<Integer, KeyCode> player1Keys = new HashMap<>();
    
    // Player 2 키 맵
    private static final Map<Integer, KeyCode> player2Keys = new HashMap<>();
    
    // 키 타입 상수 (Integer로 관리)
    public static final int KEY_LEFT = 0;
    public static final int KEY_RIGHT = 1;
    public static final int KEY_DOWN = 2;
    public static final int KEY_UP = 3;
    public static final int KEY_HARD_DROP = 4;
    
    static {
        // Player 1 기본 키 설정 (화살표 + SPACE)
        player1Keys.put(KEY_LEFT, KeyCode.LEFT);
        player1Keys.put(KEY_RIGHT, KeyCode.RIGHT);
        player1Keys.put(KEY_DOWN, KeyCode.DOWN);
        player1Keys.put(KEY_UP, KeyCode.UP);
        player1Keys.put(KEY_HARD_DROP, KeyCode.SPACE);
        
        // Player 2 기본 키 설정 (WASD + SHIFT)
        player2Keys.put(KEY_LEFT, KeyCode.A);
        player2Keys.put(KEY_RIGHT, KeyCode.D);
        player2Keys.put(KEY_DOWN, KeyCode.S);
        player2Keys.put(KEY_UP, KeyCode.W);
        player2Keys.put(KEY_HARD_DROP, KeyCode.SHIFT);
    }

    public static KeyCode getLeftKey(PlayerId id) {
        return getKey(id, KEY_LEFT);
    }


    public static KeyCode getRightKey(PlayerId id) {
        return getKey(id, KEY_RIGHT);
    }


    public static KeyCode getDownKey(PlayerId id) {
        return getKey(id, KEY_DOWN);
    }


    public static KeyCode getUpKey(PlayerId id) {
        return getKey(id, KEY_UP);
    }
    

    public static KeyCode getHardDropKey(PlayerId id) {
        return getKey(id, KEY_HARD_DROP);
    }

    /**
     * 특정 키 반환 (내부 헬퍼)
     */
    private static KeyCode getKey(PlayerId id, int keyType) {
        Map<Integer, KeyCode> keys = getPlayerKeys(id);
        return keys.get(keyType);
    }

    /**
     * 플레이어별 키 맵 반환
     */
    private static Map<Integer, KeyCode> getPlayerKeys(PlayerId id) {
        if (id == PlayerId.PLAYER1) {
            return player1Keys;
        } else if (id == PlayerId.PLAYER2) {
            return player2Keys;
        } else {
            throw new IllegalArgumentException("Invalid player index: " + id + ". Must be PLAYER1 or PLAYER2.");
        }
    }

    /**
     * 왼쪽 이동 키 설정
     */
    public static void setLeftKey(PlayerId id, KeyCode key) {
        setKey(id, KEY_LEFT, key);
    }

    /**
     * 오른쪽 이동 키 설정
     */
    public static void setRightKey(PlayerId id, KeyCode key) {
        setKey(id, KEY_RIGHT, key);
    }

    /**
     * 아래 이동 키 설정
     */
    public static void setDownKey(PlayerId id, KeyCode key) {
        setKey(id, KEY_DOWN, key);
    }

    /**
     * 위 이동 키 설정
     */
    public static void setUpKey(PlayerId id, KeyCode key) {
        setKey(id, KEY_UP, key);
    }

    /**
     * 하드 드롭 키 설정
     */
    public static void setHardDropKey(PlayerId id, KeyCode key) {
        setKey(id, KEY_HARD_DROP, key);
    }

    /**
     * 특정 키 설정 (내부 헬퍼)
     */
    private static void setKey(PlayerId id, int keyType, KeyCode key) {
        if (key == null) {
            return;
        }
        Map<Integer, KeyCode> keys = getPlayerKeys(id);
        keys.put(keyType, key);
    }

    /**
     * 모든 키를 한 번에 설정
     */
    public static void setKeys(PlayerId id, KeyCode left, KeyCode right, KeyCode down, KeyCode up, KeyCode hardDrop) {
        if (left != null) setLeftKey(id, left);
        if (right != null) setRightKey(id, right);
        if (down != null) setDownKey(id, down);
        if (up != null) setUpKey(id, up);
        if (hardDrop != null) setHardDropKey(id, hardDrop);
    }

    /**
     * 기본 키 설정으로 초기화
     */
    public static void resetToDefault(PlayerId id) {
        if (id == PlayerId.PLAYER1) {
            player1Keys.clear();
            player1Keys.put(KEY_LEFT, KeyCode.LEFT);
            player1Keys.put(KEY_RIGHT, KeyCode.RIGHT);
            player1Keys.put(KEY_DOWN, KeyCode.DOWN);
            player1Keys.put(KEY_UP, KeyCode.UP);
            player1Keys.put(KEY_HARD_DROP, KeyCode.SPACE);
        } else if (id == PlayerId.PLAYER2) {
            player2Keys.clear();
            player2Keys.put(KEY_LEFT, KeyCode.A);
            player2Keys.put(KEY_RIGHT, KeyCode.D);
            player2Keys.put(KEY_DOWN, KeyCode.S);
            player2Keys.put(KEY_UP, KeyCode.W);
            player2Keys.put(KEY_HARD_DROP, KeyCode.SHIFT);
        } else {
            throw new IllegalArgumentException("Invalid player index: " + id + ". Must be PLAYER1 or PLAYER2.");
        }
    }

    /**
     * 모든 플레이어 키 초기화
     */
    public static void resetAllToDefault() {
        resetToDefault(PlayerId.PLAYER1);
        resetToDefault(PlayerId.PLAYER2);
    }
}
