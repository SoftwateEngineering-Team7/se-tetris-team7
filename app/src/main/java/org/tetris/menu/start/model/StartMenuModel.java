package org.tetris.menu.start.model;

import org.tetris.shared.BaseModel;

public class StartMenuModel extends BaseModel {
    private int selectedIndex = 0;
    private int totalButtonCount; // 현재 표시 중인 메뉴의 버튼 개수

    public StartMenuModel() {
        totalButtonCount = 0;
    }

    public void move(int delta) {
        if (totalButtonCount == 0) {
            return;
        }
        selectedIndex = Math.floorMod(selectedIndex + delta, totalButtonCount);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    public void setSelectedIndex(int idx) {
        if (totalButtonCount == 0) {
            selectedIndex = 0;
            return;
        }
        this.selectedIndex = Math.floorMod(idx, totalButtonCount);
    }

    public void updateButtonCount(int buttonCount) {
        totalButtonCount = Math.max(0, buttonCount);
        if (totalButtonCount == 0) {
            selectedIndex = 0;
        } else {
            selectedIndex = Math.floorMod(selectedIndex, totalButtonCount);
        }
    }

    public void resetSelection() {
        selectedIndex = 0;
    }
}
