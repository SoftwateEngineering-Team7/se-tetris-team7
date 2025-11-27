package org.tetris.menu.setting.model;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

import org.util.Difficulty;
import org.util.GameColor;
import org.util.KeyLayout;
import org.util.PlayerId;
import org.util.ScreenPreset;

import javafx.scene.input.KeyCode;

public class Setting {
    private static final String DEFAULT_FILE_NAME = "setting.txt";
    private static final String KEY_COLOR_BLIND = "isColorBlind";
    
    // Player 1 키 설정
    private static final String KEY_LEFT_P1 = "keyLeft";
    private static final String KEY_RIGHT_P1 = "keyRight";
    private static final String KEY_DOWN_P1 = "keyDown";
    private static final String KEY_UP_P1 = "keyUp";
    private static final String KEY_HARD_DROP_P1 = "keyHardDrop";
    
    // Player 2 키 설정
    private static final String KEY_LEFT_P2 = "keyLeft2";
    private static final String KEY_RIGHT_P2 = "keyRight2";
    private static final String KEY_DOWN_P2 = "keyDown2";
    private static final String KEY_UP_P2 = "keyUp2";
    private static final String KEY_HARD_DROP_P2 = "keyHardDrop2";
    
    private static final String KEY_SCREEN = "screen";
    private static final String KEY_DIFFICULTY = "difficulty";

    private final Path filePath;
    private final Properties props = new Properties();

    /* --------- 생성/로딩 --------- */

    public Setting() {
        filePath = Paths.get(DEFAULT_FILE_NAME);
        loadOrInit();
    }

    /* --------- Getter --------- */

    public boolean isColorBlind() {
        return Boolean.parseBoolean(props.getProperty(KEY_COLOR_BLIND, "false"));
    }

    /**
     * 왼쪽 키 반환
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     */
    public String getKeyLeft(PlayerId id) {
        if (id == PlayerId.PLAYER1) {
            return props.getProperty(KEY_LEFT_P1, "LEFT");
        } else {
            return props.getProperty(KEY_LEFT_P2, "A");
        }
    }

    /**
     * 오른쪽 키 반환
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     */
    public String getKeyRight(PlayerId id) {
        if (id == PlayerId.PLAYER1) {
            return props.getProperty(KEY_RIGHT_P1, "RIGHT");
        } else {
            return props.getProperty(KEY_RIGHT_P2, "D");
        }
    }

    /**
     * 아래 키 반환
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     */
    public String getKeyDown(PlayerId id) {
        if (id == PlayerId.PLAYER1) {
            return props.getProperty(KEY_DOWN_P1, "DOWN");
        } else {
            return props.getProperty(KEY_DOWN_P2, "S");
        }
    }

    /**
     * 위 키 반환
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     */
    public String getKeyUp(PlayerId id) {
        if (id == PlayerId.PLAYER1) {
            return props.getProperty(KEY_UP_P1, "UP");
        } else {
            return props.getProperty(KEY_UP_P2, "W");
        }
    }

    /**
     * 하드 드롭 키 반환
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     */
    public String getKeyHardDrop(PlayerId id) {
        if (id == PlayerId.PLAYER1) {
            return props.getProperty(KEY_HARD_DROP_P1, "SPACE");
        } else {
            return props.getProperty(KEY_HARD_DROP_P2, "SHIFT");
        }
    }

    public String getScreenPreset() {
        return props.getProperty(KEY_SCREEN, "SMALL");
    }

    public String getDifficulty() {
        return props.getProperty(KEY_DIFFICULTY, "EASY");
    }

    /* --------- Setter (저장 포함) --------- */

    public void setColorBlind(boolean value) {
        props.setProperty(KEY_COLOR_BLIND, Boolean.toString(value));
        saveSettingFile();
    }

    /**
     * 왼쪽 키 설정
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     * @param key 키 이름
     */
    public void setKeyLeft(PlayerId id, String key) {
        if (key == null) 
            return;
        String propertyKey = (id == PlayerId.PLAYER1) ? KEY_LEFT_P1 : KEY_LEFT_P2;
        props.setProperty(propertyKey, key);
        saveSettingFile();
    }

    /**
     * 오른쪽 키 설정
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     * @param key 키 이름
     */
    public void setKeyRight(PlayerId id, String key) {
        if (key == null) 
            return;
        String propertyKey = (id == PlayerId.PLAYER1) ? KEY_RIGHT_P1 : KEY_RIGHT_P2;
        props.setProperty(propertyKey, key);
        saveSettingFile();
    }

    /**
     * 아래 키 설정
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     * @param key 키 이름
     */
    public void setKeyDown(PlayerId id, String key) {
        if (key == null)
            return;
        String propertyKey = (id == PlayerId.PLAYER1) ? KEY_DOWN_P1 : KEY_DOWN_P2;
        props.setProperty(propertyKey, key);
        saveSettingFile();
    }

    /**
     * 위 키 설정
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     * @param key 키 이름
     */
    public void setKeyUp(PlayerId id, String key) {
        if (key == null)
            return;
        String propertyKey = (id == PlayerId.PLAYER1) ? KEY_UP_P1 : KEY_UP_P2;
        props.setProperty(propertyKey, key);
        saveSettingFile();
    }

    /**
     * 하드 드롭 키 설정
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     * @param key 키 이름
     */
    public void setKeyHardDrop(PlayerId id, String key) {
        if (key == null) 
            return;
        String propertyKey = (id == PlayerId.PLAYER1) ? KEY_HARD_DROP_P1 : KEY_HARD_DROP_P2;
        props.setProperty(propertyKey, key);
        saveSettingFile();
    }

    public void setScreenPreset(String preset) {
        if (preset == null)
            return;
        props.setProperty(KEY_SCREEN, preset);
        saveSettingFile();
    }

    public void setDifficulty(String difficulty) {
        if (difficulty == null)
            return;
        props.setProperty(KEY_DIFFICULTY, difficulty);
        saveSettingFile();
    }

    /* --------- 내부 유틸 --------- */

    private void loadOrInit() {
        if (Files.exists(filePath)) {
            try (InputStream in = Files.newInputStream(filePath)) {
                props.load(in);
            } catch (IOException e) {
                setDefaultsAndSave();
                return;
            }
            // 누락 키 보완
            props.putIfAbsent(KEY_COLOR_BLIND, "false");
            // Player 1
            props.putIfAbsent(KEY_LEFT_P1, "LEFT");
            props.putIfAbsent(KEY_RIGHT_P1, "RIGHT");
            props.putIfAbsent(KEY_DOWN_P1, "DOWN");
            props.putIfAbsent(KEY_UP_P1, "UP");
            props.putIfAbsent(KEY_HARD_DROP_P1, "SPACE");
            // Player 2
            props.putIfAbsent(KEY_LEFT_P2, "A");
            props.putIfAbsent(KEY_RIGHT_P2, "D");
            props.putIfAbsent(KEY_DOWN_P2, "S");
            props.putIfAbsent(KEY_UP_P2, "W");
            props.putIfAbsent(KEY_HARD_DROP_P2, "SHIFT");
            
            props.putIfAbsent(KEY_SCREEN, "SMALL");
            props.putIfAbsent(KEY_DIFFICULTY, "EASY");

            saveSettingFile(); // 보완된 기본값/매핑을 반영
        } else {
            setDefaultsAndSave();
        }
    }

    public void setDefaultsAndSave() {
        props.clear();
        props.setProperty(KEY_COLOR_BLIND, "false");
        // Player 1: 방향키 + SPACE
        props.setProperty(KEY_LEFT_P1, "LEFT");
        props.setProperty(KEY_RIGHT_P1, "RIGHT");
        props.setProperty(KEY_DOWN_P1, "DOWN");
        props.setProperty(KEY_UP_P1, "UP");
        props.setProperty(KEY_HARD_DROP_P1, "SPACE");
        // Player 2: WASD + SHIFT
        props.setProperty(KEY_LEFT_P2, "A");
        props.setProperty(KEY_RIGHT_P2, "D");
        props.setProperty(KEY_DOWN_P2, "S");
        props.setProperty(KEY_UP_P2, "W");
        props.setProperty(KEY_HARD_DROP_P2, "SHIFT");
        
        props.setProperty(KEY_SCREEN, "SMALL");
        props.setProperty(KEY_DIFFICULTY, "EASY");

        saveSettingFile();
    }

    private void saveSettingFile() {
        try (OutputStream out = Files.newOutputStream(
                filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(out, "Tetris Settings");
            syncAllSettings();
        } catch (IOException e) {
            System.err.println("[Setting] 저장 실패: " + e.getMessage());
        }
    }

    /* --------- Sync 메서드들 --------- */

    private void syncAllSettings() {
        syncGameColor();
        syncKeyLayout();
        syncScreenPreset();
        syncDifficulty();
    }

    private void syncGameColor() {
        GameColor.setColorBlind(isColorBlind());
    }

    private void syncKeyLayout() {
        // Player 1 키 설정 적용
        try {
            KeyCode left = KeyCode.valueOf(getKeyLeft(PlayerId.PLAYER1));
            KeyCode right = KeyCode.valueOf(getKeyRight(PlayerId.PLAYER1));
            KeyCode down = KeyCode.valueOf(getKeyDown(PlayerId.PLAYER1));
            KeyCode up = KeyCode.valueOf(getKeyUp(PlayerId.PLAYER1));
            KeyCode hardDrop = KeyCode.valueOf(getKeyHardDrop(PlayerId.PLAYER1));
            KeyLayout.setKeys(PlayerId.PLAYER1, left, right, down, up, hardDrop);
        } catch (IllegalArgumentException e) {
            // 잘못된 키 코드가 있으면 기본값으로 초기화
            System.err.println("[Setting] Player 1 잘못된 키 설정 감지, 기본값으로 초기화: " + e.getMessage());
            KeyLayout.resetToDefault(PlayerId.PLAYER1);
        }
        
        // Player 2 키 설정 적용
        try {
            KeyCode left2 = KeyCode.valueOf(getKeyLeft(PlayerId.PLAYER2));
            KeyCode right2 = KeyCode.valueOf(getKeyRight(PlayerId.PLAYER2));
            KeyCode down2 = KeyCode.valueOf(getKeyDown(PlayerId.PLAYER2));
            KeyCode up2 = KeyCode.valueOf(getKeyUp(PlayerId.PLAYER2));
            KeyCode hardDrop2 = KeyCode.valueOf(getKeyHardDrop(PlayerId.PLAYER2));
            KeyLayout.setKeys(PlayerId.PLAYER2, left2, right2, down2, up2, hardDrop2);
        } catch (IllegalArgumentException e) {
            // 잘못된 키 코드가 있으면 기본값으로 초기화
            System.err.println("[Setting] Player 2 잘못된 키 설정 감지, 기본값으로 초기화: " + e.getMessage());
            KeyLayout.resetToDefault(PlayerId.PLAYER2);
        }
    }

    private void syncScreenPreset() {
        ScreenPreset.setCurrentPreset(getScreenPreset());
    }

    private void syncDifficulty() {
        Difficulty.setCurrentDifficulty(getDifficulty());
    }
}
