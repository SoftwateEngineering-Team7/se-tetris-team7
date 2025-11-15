package org.tetris.menu.setting.model;

import org.tetris.shared.BaseModel;
import org.util.PlayerId;

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
        updateModelFromSettings();
    }

    /** Save 시 Setting에 반영(파일 저장은 Setting의 setter에서 수행) */
    public void applyToSetting() {
        setting.setColorBlind(colorBlind);
        // Player 1
        setting.setKeyLeft(PlayerId.PLAYER1, keyLeft);
        setting.setKeyRight(PlayerId.PLAYER1, keyRight);
        setting.setKeyDown(PlayerId.PLAYER1, keyDown);
        setting.setKeyUp(PlayerId.PLAYER1, keyUp);
        setting.setKeyHardDrop(PlayerId.PLAYER1, keyHardDrop);
        // Player 2
        setting.setKeyLeft(PlayerId.PLAYER2, keyLeft2);
        setting.setKeyRight(PlayerId.PLAYER2, keyRight2);
        setting.setKeyDown(PlayerId.PLAYER2, keyDown2);
        setting.setKeyUp(PlayerId.PLAYER2, keyUp2);
        setting.setKeyHardDrop(PlayerId.PLAYER2, keyHardDrop2);
        
        setting.setScreenPreset(screen);
        setting.setDifficulty(difficulty);
    }

    /** Reset 버튼 클릭 시 Setting 객체에 저장된 값으로 모델을 되돌림 */
    public void resetToSetting() {
        setting.setDefaultsAndSave(); // 기본값으로 초기화
        updateModelFromSettings();
    }

    public void updateModelFromSettings() {
        this.colorBlind = setting.isColorBlind();
        // Player 1
        this.keyLeft = setting.getKeyLeft(PlayerId.PLAYER1);
        this.keyRight = setting.getKeyRight(PlayerId.PLAYER1);
        this.keyDown = setting.getKeyDown(PlayerId.PLAYER1);
        this.keyUp = setting.getKeyUp(PlayerId.PLAYER1);
        this.keyHardDrop = setting.getKeyHardDrop(PlayerId.PLAYER1);
        // Player 2
        this.keyLeft2 = setting.getKeyLeft(PlayerId.PLAYER2);
        this.keyRight2 = setting.getKeyRight(PlayerId.PLAYER2);
        this.keyDown2 = setting.getKeyDown(PlayerId.PLAYER2);
        this.keyUp2 = setting.getKeyUp(PlayerId.PLAYER2);
        this.keyHardDrop2 = setting.getKeyHardDrop(PlayerId.PLAYER2);
        
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

    /**
     * 왼쪽 키 반환
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     */
    public String getKeyLeft(PlayerId id) {
        return (id == PlayerId.PLAYER1) ? keyLeft : keyLeft2;
    }

    /**
     * 왼쪽 키 설정
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     * @param v 키 이름
     */
    public void setKeyLeft(PlayerId id, String v) {
        if (id == PlayerId.PLAYER1) {
            this.keyLeft = v;
        } else {
            this.keyLeft2 = v;
        }
    }

    /**
     * 오른쪽 키 반환
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     */
    public String getKeyRight(PlayerId id) {
        return (id == PlayerId.PLAYER1) ? keyRight : keyRight2;
    }

    /**
     * 오른쪽 키 설정
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     * @param v 키 이름
     */
    public void setKeyRight(PlayerId id, String v) {
        if (id == PlayerId.PLAYER1) {
            this.keyRight = v;
        } else {
            this.keyRight2 = v;
        }
    }

    /**
     * 아래 키 반환
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     */
    public String getKeyDown(PlayerId id) {
        return (id == PlayerId.PLAYER1) ? keyDown : keyDown2;
    }

    /**
     * 아래 키 설정
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     * @param v 키 이름
     */
    public void setKeyDown(PlayerId id, String v) {
        if (id == PlayerId.PLAYER1) {
            this.keyDown = v;
        } else {
            this.keyDown2 = v;
        }
    }

    /**
     * 위 키 반환
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     */
    public String getKeyUp(PlayerId id) {
        return (id == PlayerId.PLAYER1) ? keyUp : keyUp2;
    }

    /**
     * 위 키 설정
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     * @param v 키 이름
     */
    public void setKeyUp(PlayerId id, String v) {
        if (id == PlayerId.PLAYER1) {
            this.keyUp = v;
        } else {
            this.keyUp2 = v;
        }
    }

    /**
     * 하드 드롭 키 반환
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     */
    public String getKeyHardDrop(PlayerId id) {
        return (id == PlayerId.PLAYER1) ? keyHardDrop : keyHardDrop2;
    }

    /**
     * 하드 드롭 키 설정
     * @param id 플레이어 ID (PLAYER1 또는 PLAYER2)
     * @param v 키 이름
     */
    public void setKeyHardDrop(PlayerId id, String v) {
        if (id == PlayerId.PLAYER1) {
            this.keyHardDrop = v;
        } else {
            this.keyHardDrop2 = v;
        }
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