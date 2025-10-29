package org.tetris.menu.start.model;

import org.tetris.shared.BaseModel;

public class StartMenuModel extends BaseModel {
    private int selectedIndex = 0;
    private final int totalButtonCount; // 메뉴 항목 개수 (게임 시작, 설정, 종료)

    public StartMenuModel(int buttonCount) {
        selectedIndex = 0;
        totalButtonCount = buttonCount;
    }

    public void move(int delta) {
        selectedIndex = Math.floorMod(selectedIndex + delta, totalButtonCount);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    public void setSelectedIndex(int idx) {
        this.selectedIndex = Math.floorMod(idx, totalButtonCount);
    }
}