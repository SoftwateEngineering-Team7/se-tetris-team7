package org.tetris.menu.setting.model;

import org.tetris.shared.BaseModel;

public class SettingMenuModel extends BaseModel {
    private final Setting setting;
    private boolean colorBlind;
    // Player 1 keys
    private String keyLeft;
    private String keyRight;
    private String keyDown;
    private String keyUp;
    private String keyHardDrop;
    // Player 2 keys
    private String keyLeft2;
    private String keyRight2;
    private String keyDown2;
    private String keyUp2;
    private String keyHardDrop2;
    
    private String screen;
    private String difficulty;

    public SettingMenuModel(Setting setting) {
        this.setting = setting;

        this.colorBlind = setting.isColorBlind();
        // Player 1
        this.keyLeft = setting.getKeyLeft();
        this.keyRight = setting.getKeyRight();
        this.keyDown = setting.getKeyDown();
        this.keyUp = setting.getKeyUp();
        this.keyHardDrop = setting.getKeyHardDrop();
        // Player 2
        this.keyLeft2 = setting.getKeyLeft2();
        this.keyRight2 = setting.getKeyRight2();
        this.keyDown2 = setting.getKeyDown2();
        this.keyUp2 = setting.getKeyUp2();
        this.keyHardDrop2 = setting.getKeyHardDrop2();
        
        this.screen = setting.getScreenPreset();
        this.difficulty = setting.getDifficulty();
    }

    /** Save 시 Setting에 반영(파일 저장은 Setting의 setter에서 수행) */
    public void applyToSetting() {
        setting.setColorBlind(colorBlind);
        // Player 1
        setting.setKeyLeft(keyLeft);
        setting.setKeyRight(keyRight);
        setting.setKeyDown(keyDown);
        setting.setKeyUp(keyUp);
        setting.setKeyHardDrop(keyHardDrop);
        // Player 2
        setting.setKeyLeft2(keyLeft2);
        setting.setKeyRight2(keyRight2);
        setting.setKeyDown2(keyDown2);
        setting.setKeyUp2(keyUp2);
        setting.setKeyHardDrop2(keyHardDrop2);
        
        setting.setScreenPreset(screen);
        setting.setDifficulty(difficulty);
    }

    /** Reset 버튼 클릭 시 Setting 객체에 저장된 값으로 모델을 되돌림 */
    public void resetToSetting() {
        setting.setDefaultsAndSave(); // 기본값으로 초기화
        this.colorBlind = setting.isColorBlind();
        // Player 1
        this.keyLeft = setting.getKeyLeft();
        this.keyRight = setting.getKeyRight();
        this.keyDown = setting.getKeyDown();
        this.keyUp = setting.getKeyUp();
        this.keyHardDrop = setting.getKeyHardDrop();
        // Player 2
        this.keyLeft2 = setting.getKeyLeft2();
        this.keyRight2 = setting.getKeyRight2();
        this.keyDown2 = setting.getKeyDown2();
        this.keyUp2 = setting.getKeyUp2();
        this.keyHardDrop2 = setting.getKeyHardDrop2();
        
        this.screen = setting.getScreenPreset();
        this.difficulty = setting.getDifficulty();
    }

    public void updateModelFromSettings() {
        this.colorBlind = setting.isColorBlind();
        // Player 1
        this.keyLeft = setting.getKeyLeft();
        this.keyRight = setting.getKeyRight();
        this.keyDown = setting.getKeyDown();
        this.keyUp = setting.getKeyUp();
        this.keyHardDrop = setting.getKeyHardDrop();
        // Player 2
        this.keyLeft2 = setting.getKeyLeft2();
        this.keyRight2 = setting.getKeyRight2();
        this.keyDown2 = setting.getKeyDown2();
        this.keyUp2 = setting.getKeyUp2();
        this.keyHardDrop2 = setting.getKeyHardDrop2();
        
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

    public String getKeyLeft() {
        return keyLeft;
    }

    public void setKeyLeft(String v) {
        this.keyLeft = v;
    }

    public String getKeyRight() {
        return keyRight;
    }

    public void setKeyRight(String v) {
        this.keyRight = v;
    }

    public String getKeyDown() {
        return keyDown;
    }

    public void setKeyDown(String v) {
        this.keyDown = v;
    }

    public String getKeyUp() {
        return keyUp;
    }

    public void setKeyUp(String v) {
        this.keyUp = v;
    }

    public String getKeyHardDrop() {
        return keyHardDrop;
    }

    public void setKeyHardDrop(String v) {
        this.keyHardDrop = v;
    }

    // Player 2 Getters/Setters
    public String getKeyLeft2() {
        return keyLeft2;
    }

    public void setKeyLeft2(String v) {
        this.keyLeft2 = v;
    }

    public String getKeyRight2() {
        return keyRight2;
    }

    public void setKeyRight2(String v) {
        this.keyRight2 = v;
    }

    public String getKeyDown2() {
        return keyDown2;
    }

    public void setKeyDown2(String v) {
        this.keyDown2 = v;
    }

    public String getKeyUp2() {
        return keyUp2;
    }

    public void setKeyUp2(String v) {
        this.keyUp2 = v;
    }

    public String getKeyHardDrop2() {
        return keyHardDrop2;
    }

    public void setKeyHardDrop2(String v) {
        this.keyHardDrop2 = v;
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