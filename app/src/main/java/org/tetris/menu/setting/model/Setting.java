package org.tetris.menu.setting.model;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

import org.util.Difficulty;
import org.util.GameColor;
import org.util.KeyLayout;
import org.util.ScreenPreset;

public class Setting {
    private static final String DEFAULT_FILE_NAME = "setting.txt";
    private static final String KEY_COLOR_BLIND = "isColorBlind";
    // Player 1 키 설정
    private static final String KEY_LEFT = "keyLeft";         // 왼쪽 키
    private static final String KEY_RIGHT = "keyRight";       // 오른쪽 키
    private static final String KEY_DOWN = "keyDown";         // 아래 키
    private static final String KEY_UP = "keyUp";             // 위 키
    private static final String KEY_HARD_DROP = "keyHardDrop"; // 하드 드롭 키
    // Player 2 키 설정
    private static final String KEY_LEFT2 = "keyLeft2";
    private static final String KEY_RIGHT2 = "keyRight2";
    private static final String KEY_DOWN2 = "keyDown2";
    private static final String KEY_UP2 = "keyUp2";
    private static final String KEY_HARD_DROP2 = "keyHardDrop2";
    
    private static final String KEY_SCREEN = "screen"; // SMALL | MIDDLE | LARGE
    private static final String KEY_DIFFICULTY = "difficulty"; // EASY | NORMAL | HARD

    private final Path filePath;
    private final Properties props = new Properties();

    /* --------- 생성/로딩 --------- */

    // 기본 생성자: 기본 파일명 사용 (싱글 플레이어)
    public Setting() {
        filePath = Paths.get(DEFAULT_FILE_NAME);
        loadOrInit();
    }

    /* --------- Getter --------- */

    public boolean isColorBlind() {
        return Boolean.parseBoolean(props.getProperty(KEY_COLOR_BLIND, "false"));
    }

    public String getKeyLeft() {
        return props.getProperty(KEY_LEFT, "LEFT");
    }

    public String getKeyRight() {
        return props.getProperty(KEY_RIGHT, "RIGHT");
    }

    public String getKeyDown() {
        return props.getProperty(KEY_DOWN, "DOWN");
    }

    public String getKeyUp() {
        return props.getProperty(KEY_UP, "UP");
    }

    public String getKeyHardDrop() {
        return props.getProperty(KEY_HARD_DROP, "SPACE");
    }

    // Player 2 Getters
    public String getKeyLeft2() {
        return props.getProperty(KEY_LEFT2, "A");
    }

    public String getKeyRight2() {
        return props.getProperty(KEY_RIGHT2, "D");
    }

    public String getKeyDown2() {
        return props.getProperty(KEY_DOWN2, "S");
    }

    public String getKeyUp2() {
        return props.getProperty(KEY_UP2, "W");
    }

    public String getKeyHardDrop2() {
        return props.getProperty(KEY_HARD_DROP2, "SHIFT");
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

    public void setKeyLeft(String key) {
        if (key == null) key = "LEFT";
        props.setProperty(KEY_LEFT, key);
        saveSettingFile();
    }

    public void setKeyRight(String key) {
        if (key == null) key = "RIGHT";
        props.setProperty(KEY_RIGHT, key);
        saveSettingFile();
    }

    public void setKeyDown(String key) {
        if (key == null) key = "DOWN";
        props.setProperty(KEY_DOWN, key);
        saveSettingFile();
    }

    public void setKeyUp(String key) {
        if (key == null) key = "UP";
        props.setProperty(KEY_UP, key);
        saveSettingFile();
    }

    public void setKeyHardDrop(String key) {
        if (key == null) key = "SPACE";
        props.setProperty(KEY_HARD_DROP, key);
        saveSettingFile();
    }

    // Player 2 Setters
    public void setKeyLeft2(String key) {
        if (key == null) key = "A";
        props.setProperty(KEY_LEFT2, key);
        saveSettingFile();
    }

    public void setKeyRight2(String key) {
        if (key == null) key = "D";
        props.setProperty(KEY_RIGHT2, key);
        saveSettingFile();
    }

    public void setKeyDown2(String key) {
        if (key == null) key = "S";
        props.setProperty(KEY_DOWN2, key);
        saveSettingFile();
    }

    public void setKeyUp2(String key) {
        if (key == null) key = "W";
        props.setProperty(KEY_UP2, key);
        saveSettingFile();
    }

    public void setKeyHardDrop2(String key) {
        if (key == null) key = "SHIFT";
        props.setProperty(KEY_HARD_DROP2, key);
        saveSettingFile();
    }

    public void setScreenPreset(String preset) {
        if (preset == null)
            preset = "SMALL";
        props.setProperty(KEY_SCREEN, preset);
        saveSettingFile();
    }

    public void setDifficulty(String difficulty) {
        if (difficulty == null)
            difficulty = "EASY";
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
            props.putIfAbsent(KEY_LEFT, "LEFT");
            props.putIfAbsent(KEY_RIGHT, "RIGHT");
            props.putIfAbsent(KEY_DOWN, "DOWN");
            props.putIfAbsent(KEY_UP, "UP");
            props.putIfAbsent(KEY_HARD_DROP, "SPACE");
            // Player 2
            props.putIfAbsent(KEY_LEFT2, "A");
            props.putIfAbsent(KEY_RIGHT2, "D");
            props.putIfAbsent(KEY_DOWN2, "S");
            props.putIfAbsent(KEY_UP2, "W");
            props.putIfAbsent(KEY_HARD_DROP2, "SHIFT");
            
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
        props.setProperty(KEY_LEFT, "LEFT");
        props.setProperty(KEY_RIGHT, "RIGHT");
        props.setProperty(KEY_DOWN, "DOWN");
        props.setProperty(KEY_UP, "UP");
        props.setProperty(KEY_HARD_DROP, "SPACE");
        // Player 2: WASD + SHIFT
        props.setProperty(KEY_LEFT2, "A");
        props.setProperty(KEY_RIGHT2, "D");
        props.setProperty(KEY_DOWN2, "S");
        props.setProperty(KEY_UP2, "W");
        props.setProperty(KEY_HARD_DROP2, "SHIFT");
        
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
        // 개별 키 설정 적용
        try {
            javafx.scene.input.KeyCode left = javafx.scene.input.KeyCode.valueOf(getKeyLeft());
            javafx.scene.input.KeyCode right = javafx.scene.input.KeyCode.valueOf(getKeyRight());
            javafx.scene.input.KeyCode down = javafx.scene.input.KeyCode.valueOf(getKeyDown());
            javafx.scene.input.KeyCode up = javafx.scene.input.KeyCode.valueOf(getKeyUp());
            javafx.scene.input.KeyCode hardDrop = javafx.scene.input.KeyCode.valueOf(getKeyHardDrop());
            KeyLayout.setKeys(left, right, down, up, hardDrop);
        } catch (IllegalArgumentException e) {
            // 잘못된 키 코드가 있으면 기본값으로 초기화
            System.err.println("[Setting] 잘못된 키 설정 감지, 기본값으로 초기화: " + e.getMessage());
            KeyLayout.resetToDefault();
        }
    }

    private void syncScreenPreset() {
        ScreenPreset.setCurrentPreset(getScreenPreset());
    }

    private void syncDifficulty() {
        Difficulty.setCurrentDifficulty(getDifficulty());
    }
}
