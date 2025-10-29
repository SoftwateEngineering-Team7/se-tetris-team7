package org.tetris.menu.setting.model;

import org.tetris.shared.BaseModel;

public class SettingMenuModel extends BaseModel {
    private final Setting setting;
    private boolean colorBlind;
    private String keyLayout;
    private String screen;
    private String difficulty;

    public SettingMenuModel(Setting setting) {
        this.setting = setting;

        this.colorBlind = setting.isColorBlind();
        this.keyLayout = setting.getKeyLayout();
        this.screen = setting.getScreenPreset();
        this.difficulty = setting.getDifficulty();
    }

    /** Save 시 Setting에 반영(파일 저장은 Setting의 setter에서 수행) */
    public void applyToSetting() {
        setting.setColorBlind(colorBlind);
        setting.setKeyLayout(keyLayout);
        setting.setScreenPreset(screen);
        setting.setDifficulty(difficulty);
    }

    /** Reset 버튼 클릭 시 Setting 객체에 저장된 값으로 모델을 되돌림 */
    public void resetToSetting() {
        setting.setDefaultsAndSave(); // 기본값으로 초기화
        this.colorBlind = setting.isColorBlind();
        this.keyLayout = setting.getKeyLayout();
        this.screen = setting.getScreenPreset();
        this.difficulty = setting.getDifficulty();
    }

    public void updateModelFromSettings() {
        this.colorBlind = setting.isColorBlind();
        this.keyLayout = setting.getKeyLayout();
        this.screen = setting.getScreenPreset();
        this.difficulty = setting.getDifficulty();
    }

    // Getters and Setters
    public boolean isColorBlind() {
        return colorBlind;
    }

    public void setColorBlind(boolean v) {
        this.colorBlind = v;
    }

    public String getKeyLayout() {
        return keyLayout;
    }

    public void setKeyLayout(String v) {
        this.keyLayout = v;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String v) {
        this.screen = v;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String v) {
        this.difficulty = v;
    }

    public Setting getSetting() {
        return setting;
    }
}