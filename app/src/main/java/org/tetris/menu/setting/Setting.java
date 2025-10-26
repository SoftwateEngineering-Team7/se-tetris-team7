package org.tetris.menu.setting;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

import org.util.Difficulty;
import org.util.GameColor;
import org.util.KeyLayout;
import org.util.ScreenPreset;

import javafx.scene.input.KeyCode;

public class Setting {
    private static final String DEFAULT_FILE_NAME = "setting.txt";
    private static final String KEY_COLOR_BLIND = "isColorBlind";
    private static final String KEY_KEY_LAYOUT = "keyLayout"; // ARROWS | WASD
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

    public String getKeyLayout() {
        return props.getProperty(KEY_KEY_LAYOUT, "ARROWS");
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

    public void setKeyLayout(String layout) {
        if (layout == null)
            layout = "ARROWS";
        props.setProperty(KEY_KEY_LAYOUT, layout);
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
            props.putIfAbsent(KEY_KEY_LAYOUT, "ARROWS");
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
        props.setProperty(KEY_KEY_LAYOUT, "ARROWS");
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
        KeyLayout.setCurrentLayout(getKeyLayout());
    }

    private void syncScreenPreset() {
        ScreenPreset.setCurrentPreset(getScreenPreset());
    }

    private void syncDifficulty() {
        Difficulty.setCurrentDifficulty(getDifficulty());
    }
}
