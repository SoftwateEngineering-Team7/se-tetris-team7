package org.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ScreenPresetTest {
    
    @Before
    public void setUp() {
        // 각 테스트 전에 기본값으로 초기화
        ScreenPreset.setCurrentPreset(ScreenPreset.SMALL_STRING);
    }
    
    @Test
    public void testDefaultPreset() {
        assertEquals("기본 프리셋은 SMALL이어야 합니다", 
                    ScreenPreset.SMALL_STRING, ScreenPreset.getCurrentPreset());
    }
    
    @Test
    public void testSetCurrentPresetToSmall() {
        ScreenPreset.setCurrentPreset(ScreenPreset.SMALL_STRING);
        assertEquals("현재 프리셋은 SMALL이어야 합니다", 
                    ScreenPreset.SMALL_STRING, ScreenPreset.getCurrentPreset());
        assertEquals("SMALL의 너비는 800이어야 합니다", 800, ScreenPreset.getWidth());
        assertEquals("SMALL의 높이는 600이어야 합니다", 600, ScreenPreset.getHeight());
    }
    
    @Test
    public void testSetCurrentPresetToMiddle() {
        ScreenPreset.setCurrentPreset(ScreenPreset.MIDDLE_STRING);
        assertEquals("현재 프리셋은 MIDDLE이어야 합니다", 
                    ScreenPreset.MIDDLE_STRING, ScreenPreset.getCurrentPreset());
        assertEquals("MIDDLE의 너비는 1000이어야 합니다", 1000, ScreenPreset.getWidth());
        assertEquals("MIDDLE의 높이는 700이어야 합니다", 700, ScreenPreset.getHeight());
    }
    
    @Test
    public void testSetCurrentPresetToLarge() {
        ScreenPreset.setCurrentPreset(ScreenPreset.LARGE_STRING);
        assertEquals("현재 프리셋은 LARGE여야 합니다", 
                    ScreenPreset.LARGE_STRING, ScreenPreset.getCurrentPreset());
        assertEquals("LARGE의 너비는 1200이어야 합니다", 1200, ScreenPreset.getWidth());
        assertEquals("LARGE의 높이는 800이어야 합니다", 800, ScreenPreset.getHeight());
    }
}
