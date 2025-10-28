package org.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import javafx.scene.input.KeyCode;

public class KeyLayoutTest {
    
    @Before
    public void setUp() {
        // 각 테스트 전에 기본값으로 초기화
        KeyLayout.setCurrentLayout(KeyLayout.KEY_ARROWS);
    }
    
    @Test
    public void testDefaultLayout() {
        assertEquals("기본 레이아웃은 ARROWS여야 합니다", 
                    KeyLayout.KEY_ARROWS, KeyLayout.getCurrentLayout());
    }
    
    @Test
    public void testSetCurrentLayoutToArrows() {
        KeyLayout.setCurrentLayout(KeyLayout.KEY_ARROWS);
        assertEquals("현재 레이아웃은 ARROWS여야 합니다", 
                    KeyLayout.KEY_ARROWS, KeyLayout.getCurrentLayout());
        assertEquals("LEFT 키는 LEFT여야 합니다", KeyCode.LEFT, KeyLayout.getLeftKey());
        assertEquals("RIGHT 키는 RIGHT여야 합니다", KeyCode.RIGHT, KeyLayout.getRightKey());
        assertEquals("DOWN 키는 DOWN이어야 합니다", KeyCode.DOWN, KeyLayout.getDownKey());
        assertEquals("UP 키는 UP이어야 합니다", KeyCode.UP, KeyLayout.getUpKey());
    }
    
    @Test
    public void testSetCurrentLayoutToWASD() {
        KeyLayout.setCurrentLayout(KeyLayout.KEY_WASD);
        assertEquals("현재 레이아웃은 WASD여야 합니다", 
                    KeyLayout.KEY_WASD, KeyLayout.getCurrentLayout());
        assertEquals("LEFT 키는 A여야 합니다", KeyCode.A, KeyLayout.getLeftKey());
        assertEquals("RIGHT 키는 D여야 합니다", KeyCode.D, KeyLayout.getRightKey());
        assertEquals("DOWN 키는 S여야 합니다", KeyCode.S, KeyLayout.getDownKey());
        assertEquals("UP 키는 W여야 합니다", KeyCode.W, KeyLayout.getUpKey());
    }
}
